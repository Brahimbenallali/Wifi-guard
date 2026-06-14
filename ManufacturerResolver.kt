package com.wifiguard.app

import android.app.Application
import com.wifiguard.app.data.AppContainer
import com.wifiguard.app.worker.ScanScheduler

class WiFiGuardApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ScanScheduler.schedule(this, 15)
    }
}
