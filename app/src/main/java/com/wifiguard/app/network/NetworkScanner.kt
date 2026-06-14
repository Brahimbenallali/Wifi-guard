package com.wifiguard.app.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.wifiguard.app.model.ScanResultDevice
import com.wifiguard.app.model.WifiInfoSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.net.InetAddress

class NetworkScanner(private val context: Context) {
    private val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    @SuppressLint("MissingPermission")
    fun wifiSummary(): WifiInfoSummary {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val connected = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val ssid = wifiManager.connectionInfo?.ssid?.trim('"')?.takeIf { it.isNotBlank() } ?: "Unknown WiFi"
        return WifiInfoSummary(
            ssid = ssid,
            routerIp = intToIp(wifiManager.dhcpInfo.gateway),
            isConnected = connected
        )
    }

    suspend fun scanLocalNetwork(): List<ScanResultDevice> = withContext(Dispatchers.IO) {
        val gateway = wifiManager.dhcpInfo.gateway
        if (gateway == 0) return@withContext emptyList()
        val base = intToIp(gateway).substringBeforeLast(".")
        val arpBefore = readArpTable()
        val reachable = coroutineScope {
            (1..254).map { host ->
                async {
                    val ip = "$base.$host"
                    runCatching {
                        val address = InetAddress.getByName(ip)
                        if (address.isReachable(250)) ip else null
                    }.getOrNull()
                }
            }.awaitAll().filterNotNull()
        }
        val arpAfter = readArpTable()
        val arp = arpBefore + arpAfter
        reachable.mapNotNull { ip ->
            val mac = arp[ip] ?: return@mapNotNull null
            ScanResultDevice(
                name = "Device ${ip.substringAfterLast('.')}",
                manufacturer = ManufacturerResolver.resolve(mac),
                ipAddress = ip,
                macAddress = mac.uppercase()
            )
        }.distinctBy { it.macAddress }
    }

    private fun readArpTable(): Map<String, String> {
        val file = File("/proc/net/arp")
        if (!file.canRead()) return emptyMap()
        return file.readLines()
            .drop(1)
            .mapNotNull { line ->
                val parts = line.split(Regex("\\s+"))
                val ip = parts.getOrNull(0)
                val mac = parts.getOrNull(3)
                if (ip != null && mac != null && mac != "00:00:00:00:00:00") ip to mac else null
            }.toMap()
    }

    private fun intToIp(value: Int): String {
        return listOf(
            value and 0xff,
            value shr 8 and 0xff,
            value shr 16 and 0xff,
            value shr 24 and 0xff
        ).joinToString(".")
    }
}
