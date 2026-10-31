package com.nothing.action.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nothing.action.core.data.ActionPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Automatically starts the NothingForegroundService when the device finishes booting
 * or when the application is updated.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val repository = ActionPreferencesRepository(context)
            CoroutineScope(Dispatchers.IO).launch {
                val settings = repository.settingsFlow.first()
                if (settings.isServiceEnabled) {
                    NothingForegroundService.start(context)
                }
            }
        }
    }
}
