package com.pontifex.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.pontifex.app.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TerminalForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Terminal session active"
                startForeground(NOTIFICATION_ID, createNotification(message))
            }
            ACTION_UPDATE -> {
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Terminal session active"
                updateNotification(message)
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Terminal Sessions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps terminal sessions alive in the background"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Pontifex")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_send)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Pontifex")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_send)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        }
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification(text))
    }

    companion object {
        const val CHANNEL_ID = "pontifex_terminal"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.pontifex.app.START"
        const val ACTION_UPDATE = "com.pontifex.app.UPDATE"
        const val ACTION_STOP = "com.pontifex.app.STOP"
        const val EXTRA_MESSAGE = "message"

        fun start(context: Context, message: String = "Terminal session active") {
            val intent = Intent(context, TerminalForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_MESSAGE, message)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun update(context: Context, message: String) {
            val intent = Intent(context, TerminalForegroundService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_MESSAGE, message)
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, TerminalForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
