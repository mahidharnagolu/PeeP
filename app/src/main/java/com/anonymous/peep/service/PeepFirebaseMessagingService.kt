package com.anonymous.peep.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging service for push notifications.
 * Handles incoming peep notifications and token refresh.
 */
class PeepFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "PeepFCM"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        // Token will be saved by the AuthViewModel when user is logged in
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received: ${message.data}")

        // If it's a silent push to wake up the device, broadcast status
        val type = message.data["type"]
        if (type == "silent_peep" || type == "peep") {
            // Trigger an immediate status broadcast
            try {
                val currentApp = UsageStatsHelper.getForegroundApp(this)
                Log.d(TAG, "Woke up from peep, current app: $currentApp")
                // The status will be picked up by the StatusBroadcastService
            } catch (e: Exception) {
                Log.e(TAG, "Error getting foreground app: ${e.message}")
            }
        }
    }
}
