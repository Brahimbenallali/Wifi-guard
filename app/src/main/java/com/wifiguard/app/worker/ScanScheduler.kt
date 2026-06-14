package com.wifiguard.app.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ScanScheduler {
    fun schedule(context: Context, minutes: Int) {
        val interval = minutes.coerceAtLeast(15)
        val request = PeriodicWorkRequestBuilder<ScanWorker>(interval.toLong(), TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "wifi_guard_scan",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
