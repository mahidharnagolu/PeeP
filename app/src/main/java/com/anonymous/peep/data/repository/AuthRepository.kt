package com.anonymous.peep.data.repository

import com.anonymous.peep.data.SupabaseClient
import com.anonymous.peep.data.model.Profile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val client: SupabaseClient,
) {
    suspend fun initialize(): Boolean = client.loadSession()

    val isLoggedIn: Boolean
        get() = client.cachedAccessToken != null

    val currentUserId: String?
        get() = client.cachedUserId

    suspend fun signUp(email: String, password: String, username: String): Result<String> {
        // Check if username is taken
        val checkResult = client.get("profiles", "username=eq.$username&select=username")
        if (checkResult.isSuccess) {
            val existing = client.json.decodeFromString<List<Profile>>(checkResult.getOrThrow())
            if (existing.isNotEmpty()) {
                return Result.failure(Exception("Username already taken"))
            }
        }

        val result = client.signUp(email, password, username)
        return result.map { it.user.id }
    }

    suspend fun signIn(email: String, password: String): Result<String> {
        val result = client.signIn(email, password)
        return result.map { it.user.id }
    }

    suspend fun signOut() {
        client.signOut()
    }

    suspend fun fetchProfile(): Profile? {
        val userId = client.cachedUserId ?: return null
        val result = client.get("profiles", "id=eq.$userId&select=*")
        if (result.isSuccess) {
            val profiles = client.json.decodeFromString<List<Profile>>(result.getOrThrow())
            return profiles.firstOrNull()
        }
        return null
    }

    suspend fun updateFcmToken(token: String) {
        val userId = client.cachedUserId ?: return
        val body = """{"fcm_token":"$token"}"""
        client.patch("profiles", "id=eq.$userId", body)
    }

    suspend fun clearFcmToken() {
        val userId = client.cachedUserId ?: return
        val body = """{"fcm_token":null}"""
        client.patch("profiles", "id=eq.$userId", body)
    }
}
