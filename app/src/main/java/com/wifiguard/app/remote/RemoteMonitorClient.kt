package com.wifiguard.app.remote

import com.wifiguard.app.model.ConnectionEventType
import com.wifiguard.app.model.DeviceEntity
import com.wifiguard.app.model.DeviceStatus
import com.wifiguard.app.model.HistoryEntity
import com.wifiguard.app.model.WifiInfoSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class RemoteMonitorClient {
    suspend fun upload(config: RemoteConfig, snapshot: RemoteSnapshot) = withContext(Dispatchers.IO) {
        val connection = open(config, "PUT")
        connection.outputStream.use { it.write(snapshot.toJson().toString().toByteArray(Charsets.UTF_8)) }
        require(connection.responseCode in 200..299) { "Remote upload failed: ${connection.responseCode}" }
    }

    suspend fun download(config: RemoteConfig): RemoteSnapshot = withContext(Dispatchers.IO) {
        val connection = open(config, "GET")
        require(connection.responseCode in 200..299) { "Remote refresh failed: ${connection.responseCode}" }
        val text = connection.inputStream.bufferedReader().use { it.readText() }
        JSONObject(text).toSnapshot()
    }

    private fun open(config: RemoteConfig, method: String): HttpURLConnection {
        val base = config.endpoint.trimEnd('/')
        val site = config.siteId.encodePath()
        return (URL("$base/sites/$site/snapshot").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10_000
            readTimeout = 10_000
            doInput = true
            doOutput = method == "PUT"
            setRequestProperty("Authorization", "Bearer ${config.token}")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }
    }

    private fun RemoteSnapshot.toJson() = JSONObject()
        .put("siteId", siteId)
        .put("updatedAt", updatedAt)
        .put("wifi", JSONObject().put("ssid", wifi.ssid).put("routerIp", wifi.routerIp).put("isConnected", wifi.isConnected))
        .put("devices", JSONArray(devices.map { it.toJson() }))
        .put("history", JSONArray(history.take(500).map { it.toJson() }))

    private fun DeviceEntity.toJson() = JSONObject()
        .put("macAddress", macAddress)
        .put("name", name)
        .put("manufacturer", manufacturer)
        .put("ipAddress", ipAddress)
        .put("status", status.name)
        .put("firstSeen", firstSeen)
        .put("lastSeen", lastSeen)
        .put("connectionCount", connectionCount)
        .put("isOnline", isOnline)

    private fun HistoryEntity.toJson() = JSONObject()
        .put("macAddress", macAddress)
        .put("deviceName", deviceName)
        .put("ipAddress", ipAddress)
        .put("eventType", eventType.name)
        .put("timestamp", timestamp)

    private fun JSONObject.toSnapshot(): RemoteSnapshot {
        val wifiJson = getJSONObject("wifi")
        return RemoteSnapshot(
            siteId = getString("siteId"),
            updatedAt = getLong("updatedAt"),
            wifi = WifiInfoSummary(
                ssid = wifiJson.optString("ssid", "Remote WiFi"),
                routerIp = wifiJson.optString("routerIp", "0.0.0.0"),
                isConnected = wifiJson.optBoolean("isConnected", false)
            ),
            devices = getJSONArray("devices").objects().map { it.toDevice() },
            history = optJSONArray("history")?.objects()?.map { it.toHistory() } ?: emptyList()
        )
    }

    private fun JSONObject.toDevice() = DeviceEntity(
        macAddress = getString("macAddress"),
        name = optString("name", "Remote device"),
        manufacturer = optString("manufacturer", "Unknown manufacturer"),
        ipAddress = optString("ipAddress", "0.0.0.0"),
        status = DeviceStatus.valueOf(optString("status", DeviceStatus.UNKNOWN.name)),
        firstSeen = optLong("firstSeen", System.currentTimeMillis()),
        lastSeen = optLong("lastSeen", System.currentTimeMillis()),
        connectionCount = optInt("connectionCount", 1),
        isOnline = optBoolean("isOnline", false)
    )

    private fun JSONObject.toHistory() = HistoryEntity(
        macAddress = getString("macAddress"),
        deviceName = optString("deviceName", "Remote device"),
        ipAddress = optString("ipAddress", "0.0.0.0"),
        eventType = ConnectionEventType.valueOf(optString("eventType", ConnectionEventType.CONNECTED.name)),
        timestamp = optLong("timestamp", System.currentTimeMillis())
    )

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
    private fun String.encodePath(): String = replace(" ", "%20").replace("/", "%2F")
}
