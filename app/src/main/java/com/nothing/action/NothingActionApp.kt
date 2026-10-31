package com.nothing.action

import android.app.Application
import com.nothing.action.core.data.ActionPreferencesRepository
import com.nothing.action.service.NothingForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NothingActionApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Start Foreground Service if enabled in preferences
        val repository = ActionPreferencesRepository(this)
        CoroutineScope(Dispatchers.Main).launch {
            val settings = repository.settingsFlow.first()
            if (settings.isServiceEnabled) {
                NothingForegroundService.start(this@NothingActionApp)
            }
        }
    }
}
