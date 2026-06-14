package com.wifiguard.app.data

import android.content.Context
import com.wifiguard.app.data.local.WiFiGuardDatabase
import com.wifiguard.app.data.repository.DeviceRepository
import com.wifiguard.app.data.repository.SettingsRepository
import com.wifiguard.app.network.NetworkScanner
import com.wifiguard.app.notification.AlertNotifier
import com.wifiguard.app.remote.RemoteMonitorClient
import com.wifiguard.app.report.DatabaseBackupManager
import com.wifiguard.app.report.ReportExporter

class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext
    private val database = WiFiGuardDatabase.create(appContext)
    val settingsRepository = SettingsRepository(appContext)
    val notifier = AlertNotifier(appContext)
    val scanner = NetworkScanner(appContext)
    val reportExporter = ReportExporter(appContext)
    val backupManager = DatabaseBackupManager(appContext) { database.close() }
    val remoteClient = RemoteMonitorClient()
    val deviceRepository = DeviceRepository(
        deviceDao = database.deviceDao(),
        historyDao = database.historyDao(),
        alertDao = database.alertDao(),
        scanner = scanner,
        notifier = notifier,
        remoteClient = remoteClient
    )
}
