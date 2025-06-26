package com.apexclearcache.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class CacheSchedulerService : Service() {
    companion object {
        private const val TAG = "CacheSchedulerService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "cache_scheduler_channel"
        
        fun startService(context: Context) {
            val intent = Intent(context, CacheSchedulerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, CacheSchedulerService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate() called")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand() called")
        
        val status = ScheduleManager.getScheduleStatus(this)
        val notification = createNotification(status)
        
        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "Foreground service started")
        
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy() called")
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Cache Scheduler",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows cache scheduler status"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(status: ScheduleStatus): Notification {
        val nextRunText = status.nextRunTime ?: "Not scheduled"
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ATAK Cache Manager")
            .setContentText("Active • Next: $nextRunText")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
} 