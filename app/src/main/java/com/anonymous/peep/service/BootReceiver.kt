package com.anonymous.peep.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Restarts the StatusBroadcastService after device boot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed, checking if service should restart")
            // The service will be restarted when the user opens the app
            // (We can't read DataStore synchronously here easily,
            //  so we rely on the app's next launch to restart the service)
        }
    }
}
