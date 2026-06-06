package com.anonymous.peep.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.anonymous.peep.MainActivity
import com.anonymous.peep.data.SupabaseClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that broadcasts the user's current foreground app
 * to Supabase every 30 seconds. Runs even when the app is backgrounded.
 */
@AndroidEntryPoint
class StatusBroadcastService : Service() {

    @Inject
    lateinit var supabaseClient: SupabaseClient

    companion object {
        private const val TAG = "StatusBroadcastService"
        private const val CHANNEL_ID = "peep_status_channel"
        private const val NOTIFICATION_ID = 1001
        private const val BROADCAST_INTERVAL = 30_000L // 30 seconds

        fun start(context: Context, userId: String, accessToken: String) {
            val intent = Intent(context, StatusBroadcastService::class.java).apply {
                putExtra("userId", userId)
                putExtra("accessToken", accessToken)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, StatusBroadcastService::class.java))
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var isRunning = false
    private var broadcastCycleCount = 0
    private val REFRESH_EVERY_N_CYCLES = 100 // ~50 min at 30s intervals

    private val broadcastRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                broadcastStatus()
                handler.postDelayed(this, BROADCAST_INTERVAL)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service starting...")

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        isRunning = true
        handler.post(broadcastRunnable)

        Log.d(TAG, "Service started")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "Service stopping...")
        isRunning = false
        handler.removeCallbacks(broadcastRunnable)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Peep Status",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Broadcasts your app activity to friends"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PeeP")
            .setContentText("Sharing your activity with friends")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(0xFF000000.toInt())
            .build()
    }

    private fun broadcastStatus() {
        serviceScope.launch {
            try {
                // Proactive token refresh every ~50 minutes
                broadcastCycleCount++
                if (broadcastCycleCount % REFRESH_EVERY_N_CYCLES == 0) {
                    Log.d(TAG, "Proactive token refresh (cycle $broadcastCycleCount)")
                    supabaseClient.refreshSessionIfNeeded()
                }

                val currentApp = UsageStatsHelper.getForegroundApp(this@StatusBroadcastService)
                if (currentApp != null) {
                    val friendlyName = AppNameMapper.getFriendlyAppName(currentApp)
                    val userId = supabaseClient.cachedUserId ?: return@launch
                    
                    val jsonBody = """
                        {
                            "user_id": "$userId",
                            "current_app": "$currentApp",
                            "friendly_name": "$friendlyName",
                            "updated_at": "${java.time.Instant.now()}"
                        }
                    """.trimIndent()

                    val result = supabaseClient.upsert("user_status", jsonBody, "user_id")
                    if (result.isSuccess) {
                        Log.d(TAG, "Broadcasted: $friendlyName")
                    } else {
                        Log.e(TAG, "Supabase error: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Broadcast error: ${e.message}")
            }
        }
    }
}
