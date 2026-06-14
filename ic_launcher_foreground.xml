package com.wifiguard.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DeviceStatus { TRUSTED, UNKNOWN, BLOCKED }
enum class ConnectionEventType { CONNECTED, DISCONNECTED }
enum class AlertType { NEW_DEVICE, DEVICE_DISCONNECTED, UNKNOWN_DEVICE, SECURITY_WARNING }

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val macAddress: String,
    val name: String,
    val manufacturer: String,
    val ipAddress: String,
    val status: DeviceStatus,
    val firstSeen: Long,
    val lastSeen: Long,
    val connectionCount: Int,
    val isOnline: Boolean
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val macAddress: String,
    val deviceName: String,
    val ipAddress: String,
    val eventType: ConnectionEventType,
    val timestamp: Long
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: AlertType,
    val timestamp: Long,
    val isRead: Boolean = false
)

data class WifiInfoSummary(
    val ssid: String = "Unknown WiFi",
    val routerIp: String = "0.0.0.0",
    val isConnected: Boolean = false
)

data class ScanResultDevice(
    val name: String,
    val manufacturer: String,
    val ipAddress: String,
    val macAddress: String
)

data class DashboardStats(
    val totalDevices: Int = 0,
    val unknownDevices: Int = 0,
    val trustedDevices: Int = 0,
    val totalConnections: Int = 0,
    val newDevices: Int = 0,
    val lastDevice: DeviceEntity? = null
)
