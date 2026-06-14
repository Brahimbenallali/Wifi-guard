package com.wifiguard.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore("wifi_guard_settings")

class SettingsRepository(private val context: Context) {
    private val scanIntervalKey = intPreferencesKey("scan_interval")
    private val notificationsKey = booleanPreferencesKey("notifications")
    private val autoScanKey = booleanPreferencesKey("auto_scan")
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val remoteEnabledKey = booleanPreferencesKey("remote_enabled")
    private val remoteEndpointKey = stringPreferencesKey("remote_endpoint")
    private val remoteSiteIdKey = stringPreferencesKey("remote_site_id")
    private val remoteTokenKey = stringPreferencesKey("remote_token")

    val settings = context.settingsStore.data.map {
        AppSettings(
            scanIntervalMinutes = it[scanIntervalKey] ?: 15,
            notificationsEnabled = it[notificationsKey] ?: true,
            autoScanOnStartup = it[autoScanKey] ?: true,
            darkMode = it[darkModeKey] ?: true,
            remoteEnabled = it[remoteEnabledKey] ?: false,
            remoteEndpoint = it[remoteEndpointKey] ?: "",
            remoteSiteId = it[remoteSiteIdKey] ?: "home",
            remoteToken = it[remoteTokenKey] ?: ""
        )
    }

    suspend fun setScanInterval(minutes: Int) = context.settingsStore.edit { it[scanIntervalKey] = minutes }
    suspend fun setNotifications(enabled: Boolean) = context.settingsStore.edit { it[notificationsKey] = enabled }
    suspend fun setAutoScan(enabled: Boolean) = context.settingsStore.edit { it[autoScanKey] = enabled }
    suspend fun setDarkMode(enabled: Boolean) = context.settingsStore.edit { it[darkModeKey] = enabled }
    suspend fun setRemoteEnabled(enabled: Boolean) = context.settingsStore.edit { it[remoteEnabledKey] = enabled }
    suspend fun setRemoteEndpoint(endpoint: String) = context.settingsStore.edit { it[remoteEndpointKey] = endpoint }
    suspend fun setRemoteSiteId(siteId: String) = context.settingsStore.edit { it[remoteSiteIdKey] = siteId }
    suspend fun setRemoteToken(token: String) = context.settingsStore.edit { it[remoteTokenKey] = token }
}

data class AppSettings(
    val scanIntervalMinutes: Int,
    val notificationsEnabled: Boolean,
    val autoScanOnStartup: Boolean,
    val darkMode: Boolean,
    val remoteEnabled: Boolean,
    val remoteEndpoint: String,
    val remoteSiteId: String,
    val remoteToken: String
)
