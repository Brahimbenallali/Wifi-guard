package com.wifiguard.app.data.repository

import com.wifiguard.app.data.local.dao.AlertDao
import com.wifiguard.app.data.local.dao.DeviceDao
import com.wifiguard.app.data.local.dao.HistoryDao
import com.wifiguard.app.model.AlertEntity
import com.wifiguard.app.model.AlertType
import com.wifiguard.app.model.ConnectionEventType
import com.wifiguard.app.model.DashboardStats
import com.wifiguard.app.model.DeviceEntity
import com.wifiguard.app.model.DeviceStatus
import com.wifiguard.app.model.HistoryEntity
import com.wifiguard.app.network.NetworkScanner
import com.wifiguard.app.notification.AlertNotifier
import com.wifiguard.app.remote.RemoteConfig
import com.wifiguard.app.remote.RemoteMonitorClient
import com.wifiguard.app.remote.RemoteSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DeviceRepository(
    private val deviceDao: DeviceDao,
    private val historyDao: HistoryDao,
    private val alertDao: AlertDao,
    private val scanner: NetworkScanner,
    private val notifier: AlertNotifier,
    private val remoteClient: RemoteMonitorClient
) {
    val devices = deviceDao.observeDevices()
    val history = historyDao.observeHistory()
    val alerts = alertDao.observeAlerts()
    val wifiSummary = scanner::wifiSummary

    val dashboardStats: Flow<DashboardStats> = combine(devices, historyDao.observeConnectionCount()) { devices, total ->
        DashboardStats(
            totalDevices = devices.count { it.isOnline },
            unknownDevices = devices.count { it.isOnline && it.status == DeviceStatus.UNKNOWN },
            trustedDevices = devices.count { it.status == DeviceStatus.TRUSTED },
            totalConnections = total,
            newDevices = devices.count { System.currentTimeMillis() - it.firstSeen < DAY_MS },
            lastDevice = devices.maxByOrNull { it.lastSeen }
        )
    }

    suspend fun scanNow() {
        val now = System.currentTimeMillis()
        val found = scanner.scanLocalNetwork()
        val onlineMacs = found.map { it.macAddress }

        found.forEach { result ->
            val existing = deviceDao.getDevice(result.macAddress)
            val status = existing?.status ?: DeviceStatus.UNKNOWN
            val device = DeviceEntity(
                macAddress = result.macAddress,
                name = existing?.name ?: result.name,
                manufacturer = result.manufacturer,
                ipAddress = result.ipAddress,
                status = status,
                firstSeen = existing?.firstSeen ?: now,
                lastSeen = now,
                connectionCount = (existing?.connectionCount ?: 0) + if (existing?.isOnline == true) 0 else 1,
                isOnline = status != DeviceStatus.BLOCKED
            )
            deviceDao.upsert(device)
            if (existing == null || existing.isOnline.not()) {
                saveEvent(device, ConnectionEventType.CONNECTED, now)
            }
            if ((existing == null || existing.isOnline.not()) && status == DeviceStatus.UNKNOWN) {
                saveAlert("Unknown device detected", "${device.name} connected at ${device.ipAddress}", AlertType.UNKNOWN_DEVICE, now)
            }
        }

        val before = deviceDao.getAll()
        before.filter { it.isOnline && it.macAddress !in onlineMacs }.forEach { offline ->
            saveEvent(offline, ConnectionEventType.DISCONNECTED, now)
            saveAlert("Device disconnected", "${offline.name} left the network", AlertType.DEVICE_DISCONNECTED, now)
        }
        if (onlineMacs.isNotEmpty()) deviceDao.markMissingOffline(onlineMacs) else deviceDao.markAllOffline()
    }

    suspend fun uploadRemote(config: RemoteConfig) {
        if (!config.isReady) return
        remoteClient.upload(
            config,
            RemoteSnapshot(
                siteId = config.siteId,
                wifi = scanner.wifiSummary(),
                devices = deviceDao.getAll(),
                history = historyDao.getAll(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun refreshFromRemote(config: RemoteConfig) {
        if (!config.isReady) return
        val snapshot = remoteClient.download(config)
        snapshot.devices.forEach { deviceDao.upsert(it) }
        snapshot.history.forEach { event ->
            if (historyDao.countMatching(event.macAddress, event.timestamp, event.eventType) == 0) {
                historyDao.insert(event)
            }
        }
    }

    fun observeDevice(mac: String) = deviceDao.observeDevice(mac)
    fun observeDeviceHistory(mac: String) = historyDao.observeForDevice(mac)

    suspend fun setStatus(mac: String, status: DeviceStatus) = deviceDao.updateStatus(mac, status)
    suspend fun rename(mac: String, name: String) = deviceDao.rename(mac, name)
    suspend fun deleteHistory(mac: String) = historyDao.deleteForDevice(mac)
    suspend fun deleteDevice(mac: String) = deviceDao.delete(mac)
    suspend fun markAllAlertsRead() = alertDao.markAllRead()

    private suspend fun saveEvent(device: DeviceEntity, type: ConnectionEventType, now: Long) {
        historyDao.insert(
            HistoryEntity(
                macAddress = device.macAddress,
                deviceName = device.name,
                ipAddress = device.ipAddress,
                eventType = type,
                timestamp = now
            )
        )
    }

    private suspend fun saveAlert(title: String, message: String, type: AlertType, now: Long) {
        alertDao.insert(AlertEntity(title = title, message = message, type = type, timestamp = now))
        notifier.send(title, message)
    }

    companion object {
        private const val DAY_MS = 24 * 60 * 60 * 1000L
    }
}
