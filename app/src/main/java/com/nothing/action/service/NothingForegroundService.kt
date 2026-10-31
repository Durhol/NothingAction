package com.nothing.action.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nothing.action.MainActivity
import com.nothing.action.R
import com.nothing.action.core.data.ActionPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NothingForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "nothing_action_channel_id"
        const val NOTIFICATION_ID = 2026
        const val ACTION_TOGGLE_STATE = "com.nothing.action.ACTION_TOGGLE_STATE"
        const val ACTION_START = "com.nothing.action.ACTION_START"
        const val ACTION_STOP = "com.nothing.action.ACTION_STOP"

        fun start(context: Context) {
            val intent = Intent(context, NothingForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, NothingForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val repository by lazy { ActionPreferencesRepository(this) }
    private var isEngineActive = true

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE_STATE -> {
                serviceScope.launch {
                    val currentSettings = repository.settingsFlow.first()
                    val newState = !currentSettings.isServiceEnabled
                    repository.setServiceEnabled(newState)
                    isEngineActive = newState
                    updateNotification()
                }
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                serviceScope.launch {
                    isEngineActive = repository.settingsFlow.first().isServiceEnabled
                    startForeground(NOTIFICATION_ID, buildNotification())
                }
            }
        }
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val toggleIntent = Intent(this, NothingForegroundService::class.java).apply {
            action = ACTION_TOGGLE_STATE
        }
        val pendingToggle = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val statusLabel = if (isEngineActive) "● ACTIVE // LISTENING" else "○ DISABLED // PAUSED"
        val actionButtonLabel = if (isEngineActive) "PAUSE" else "RESUME"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NOTHING (ACTION)")
            .setContentText(statusLabel)
            .setSubText("ESSENTIAL KEY")
            .setSmallIcon(R.drawable.ic_nothing_logo_dots)
            .setColor(Color.BLACK)
            .setOngoing(true)
            .setContentIntent(pendingOpen)
            .addAction(R.drawable.ic_toggle_dot, actionButtonLabel, pendingToggle)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nothing Action Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Nothing Action key listener active with persistent low-power status"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
