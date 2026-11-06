package com.nexussync.datausage

import android.app.Application
import com.nexussync.datausage.di.AppContainer

class DataSyncApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.notificationHelper.ensureChannelsCreated()
    }
}
