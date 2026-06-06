package com.anonymous.peep.data.repository

import com.anonymous.peep.data.SupabaseClient
import com.anonymous.peep.service.AppNameMapper
import kotlinx.serialization.encodeToString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusRepository @Inject constructor(
    private val client: SupabaseClient,
) {
    suspend fun broadcastStatus(userId: String, currentApp: String?) {
        val friendlyName = AppNameMapper.getFriendlyAppName(currentApp ?: "")
        val body = client.json.encodeToString(mapOf(
            "user_id" to userId,
            "current_app" to (currentApp ?: ""),
            "friendly_name" to friendlyName,
            "updated_at" to java.time.Instant.now().toString(),
        ))
        client.upsert("user_status", body, "user_id")
    }

    suspend fun recordPeep(fromUserId: String, toUserId: String, detectedApp: String?, friendlyName: String) {
        val body = client.json.encodeToString(mapOf(
            "from_user_id" to fromUserId,
            "to_user_id" to toUserId,
            "detected_app" to (detectedApp ?: ""),
            "friendly_name" to friendlyName,
        ))
        client.post("peeps", body)
    }

    suspend fun sendPeepNotification(fromUserId: String, toUserId: String, friendlyName: String) {
        val body = client.json.encodeToString(mapOf(
            "from_user_id" to fromUserId,
            "to_user_id" to toUserId,
            "friendly_name" to friendlyName,
        ))
        try {
            client.invokeFunction("send-peep-notification", body)
        } catch (_: Exception) { }
    }

    suspend fun triggerSilentPeep(targetUserId: String) {
        val body = client.json.encodeToString(mapOf("targetUserId" to targetUserId))
        try {
            client.invokeFunction("send-peep", body)
        } catch (_: Exception) { }
    }
}
