package com.wifiguard.app.remote

import com.wifiguard.app.model.DeviceEntity
import com.wifiguard.app.model.HistoryEntity
import com.wifiguard.app.model.WifiInfoSummary

data class RemoteConfig(
    val enabled: Boolean,
    val endpoint: String,
    val siteId: String,
    val token: String
) {
    val isReady: Boolean
        get() = enabled && endpoint.startsWith("https://") && siteId.isNotBlank() && token.isNotBlank()
}

data class RemoteSnapshot(
    val siteId: String,
    val wifi: WifiInfoSummary,
    val devices: List<DeviceEntity>,
    val history: List<HistoryEntity>,
    val updatedAt: Long
)
