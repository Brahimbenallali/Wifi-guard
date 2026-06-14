package com.wifiguard.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wifiguard.app.data.AppContainer
import com.wifiguard.app.data.repository.AppSettings
import com.wifiguard.app.model.AlertEntity
import com.wifiguard.app.model.DashboardStats
import com.wifiguard.app.model.DeviceEntity
import com.wifiguard.app.model.DeviceStatus
import com.wifiguard.app.model.HistoryEntity
import com.wifiguard.app.model.WifiInfoSummary
import com.wifiguard.app.remote.RemoteConfig
import com.wifiguard.app.worker.ScanScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val wifi: WifiInfoSummary = WifiInfoSummary(),
    val stats: DashboardStats = DashboardStats(),
    val isScanning: Boolean = false,
    val isRemoteRefreshing: Boolean = false,
    val remoteEnabled: Boolean = false
)

class DashboardViewModel(private val container: AppContainer) : ViewModel() {
    private val scanning = MutableStateFlow(false)
    private val remoteRefreshing = MutableStateFlow(false)
    val uiState: StateFlow<DashboardUiState> = combine(
        container.deviceRepository.dashboardStats,
        scanning,
        remoteRefreshing,
        container.settingsRepository.settings
    ) { stats, isScanning, isRemoteRefreshing, settings ->
        DashboardUiState(
            wifi = container.deviceRepository.wifiSummary(),
            stats = stats,
            isScanning = isScanning,
            isRemoteRefreshing = isRemoteRefreshing,
            remoteEnabled = settings.remoteEnabled
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun scan() = viewModelScope.launch {
        scanning.value = true
        try {
            container.deviceRepository.scanNow()
            val settings = container.settingsRepository.settings.first()
            runCatching { container.deviceRepository.uploadRemote(settings.toRemoteConfig()) }
        } finally {
            scanning.value = false
        }
    }

    fun refreshRemote() = viewModelScope.launch {
        remoteRefreshing.value = true
        try {
            val settings = container.settingsRepository.settings.first()
            runCatching { container.deviceRepository.refreshFromRemote(settings.toRemoteConfig()) }
        } finally {
            remoteRefreshing.value = false
        }
    }
}

class DevicesViewModel(private val container: AppContainer) : ViewModel() {
    val devices = container.deviceRepository.devices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class HistoryViewModel(private val container: AppContainer) : ViewModel() {
    val history = container.deviceRepository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun exportPdf(items: List<HistoryEntity>) = container.reportExporter.exportPdf(items)
    fun exportExcel(items: List<HistoryEntity>) = container.reportExporter.exportExcel(items)
}

class DeviceDetailsViewModel(private val container: AppContainer, private val mac: String) : ViewModel() {
    val device = container.deviceRepository.observeDevice(mac)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val history = container.deviceRepository.observeDeviceHistory(mac)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun trust() = viewModelScope.launch { container.deviceRepository.setStatus(mac, DeviceStatus.TRUSTED) }
    fun block() = viewModelScope.launch { container.deviceRepository.setStatus(mac, DeviceStatus.BLOCKED) }
    fun rename(name: String) = viewModelScope.launch { container.deviceRepository.rename(mac, name) }
    fun deleteHistory() = viewModelScope.launch { container.deviceRepository.deleteHistory(mac) }
}

class AlertsViewModel(private val container: AppContainer) : ViewModel() {
    val alerts: StateFlow<List<AlertEntity>> = container.deviceRepository.alerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun markRead() = viewModelScope.launch { container.deviceRepository.markAllAlertsRead() }
}

class StatisticsViewModel(private val container: AppContainer) : ViewModel() {
    val devices: StateFlow<List<DeviceEntity>> = container.deviceRepository.devices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val history: StateFlow<List<HistoryEntity>> = container.deviceRepository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class SettingsViewModel(private val container: AppContainer) : ViewModel() {
    val settings: StateFlow<AppSettings> = container.settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings(15, true, true, true, false, "", "home", ""))

    fun setInterval(minutes: Int) = viewModelScope.launch {
        container.settingsRepository.setScanInterval(minutes)
        ScanScheduler.schedule(container.appContext, minutes)
    }

    fun setNotifications(value: Boolean) = viewModelScope.launch { container.settingsRepository.setNotifications(value) }
    fun setAutoScan(value: Boolean) = viewModelScope.launch { container.settingsRepository.setAutoScan(value) }
    fun setDarkMode(value: Boolean) = viewModelScope.launch { container.settingsRepository.setDarkMode(value) }
    fun setRemoteEnabled(value: Boolean) = viewModelScope.launch { container.settingsRepository.setRemoteEnabled(value) }
    fun setRemoteEndpoint(value: String) = viewModelScope.launch { container.settingsRepository.setRemoteEndpoint(value) }
    fun setRemoteSiteId(value: String) = viewModelScope.launch { container.settingsRepository.setRemoteSiteId(value) }
    fun setRemoteToken(value: String) = viewModelScope.launch { container.settingsRepository.setRemoteToken(value) }
    fun backupDatabase() = container.backupManager.backup()
    fun restoreDatabase() = container.backupManager.restoreLatest()
}

private fun AppSettings.toRemoteConfig() = RemoteConfig(
    enabled = remoteEnabled,
    endpoint = remoteEndpoint,
    siteId = remoteSiteId,
    token = remoteToken
)

@Suppress("UNCHECKED_CAST")
class WiFiGuardViewModelFactory(
    private val container: AppContainer,
    private val mac: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            DashboardViewModel::class.java -> DashboardViewModel(container)
            DevicesViewModel::class.java -> DevicesViewModel(container)
            HistoryViewModel::class.java -> HistoryViewModel(container)
            AlertsViewModel::class.java -> AlertsViewModel(container)
            StatisticsViewModel::class.java -> StatisticsViewModel(container)
            SettingsViewModel::class.java -> SettingsViewModel(container)
            DeviceDetailsViewModel::class.java -> DeviceDetailsViewModel(container, requireNotNull(mac))
            else -> error("Unknown ViewModel ${modelClass.name}")
        } as T
    }
}
