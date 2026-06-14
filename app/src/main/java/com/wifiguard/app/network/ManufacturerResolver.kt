package com.wifiguard.app.network

object ManufacturerResolver {
    private val prefixes = mapOf(
        "F4:F5:D8" to "Google / Nest",
        "BC:52:B7" to "Apple",
        "3C:5A:B4" to "Google",
        "28:6C:07" to "Samsung",
        "44:65:0D" to "Amazon",
        "D8:0D:17" to "TP-Link",
        "C0:25:E9" to "TP-Link",
        "A4:2B:B0" to "Huawei",
        "E8:65:D4" to "Xiaomi"
    )

    fun resolve(mac: String): String {
        val oui = mac.uppercase().split(":").take(3).joinToString(":")
        return prefixes[oui] ?: "Unknown manufacturer"
    }
}
