package com.nexussync.datausage.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nexussync.datausage.DataSyncApplication

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val isBootOrUpdate = action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"

        if (!isBootOrUpdate) return

        val app = context.applicationContext as? DataSyncApplication ?: return
        val prefs = app.container.preferencesRepository.currentPreferences

        if (prefs.autoStartOnBoot && prefs.monitoringActive && prefs.persistentNotification) {
            app.container.serviceController.startMonitoringService()
        }
    }
}
