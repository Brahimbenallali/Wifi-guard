package com.wifiguard.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wifiguard.app.WiFiGuardApplication
import com.wifiguard.app.remote.RemoteConfig
import kotlinx.coroutines.flow.first

class ScanWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as WiFiGuardApplication
        return runCatching {
            val settings = app.container.settingsRepository.settings.first()
            app.container.deviceRepository.scanNow()
            app.container.deviceRepository.uploadRemote(
                RemoteConfig(settings.remoteEnabled, settings.remoteEndpoint, settings.remoteSiteId, settings.remoteToken)
            )
            Result.success()
        }.getOrElse { Result.retry() }
    }
}
