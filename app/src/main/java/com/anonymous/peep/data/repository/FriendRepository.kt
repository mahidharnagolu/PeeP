package com.anonymous.peep.data.repository

import com.anonymous.peep.data.SupabaseClient
import com.anonymous.peep.data.model.*
import kotlinx.serialization.encodeToString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRepository @Inject constructor(
    private val client: SupabaseClient,
) {
    private val json get() = client.json

    suspend fun fetchFriends(userId: String): List<Profile> {
        val friends = mutableListOf<Profile>()

        // Friends where I am user_id
        val sent = client.get(
            "friends",
            "user_id=eq.$userId&status=eq.accepted&select=friend:profiles!friends_friend_id_fkey(*)"
        )
        if (sent.isSuccess) {
            val data = json.decodeFromString<List<FriendWithProfile>>(sent.getOrThrow())
            data.mapNotNull { it.friend }.let { friends.addAll(it) }
        }

        // Friends where I am friend_id (they added me)
        val received = client.get(
            "friends",
            "friend_id=eq.$userId&status=eq.accepted&select=friend:profiles!friends_user_id_fkey(*)"
        )
        if (received.isSuccess) {
            val data = json.decodeFromString<List<FriendWithProfile>>(received.getOrThrow())
            data.mapNotNull { it.friend }.let { friends.addAll(it) }
        }

        return friends
    }

    suspend fun fetchPendingRequests(userId: String): List<FriendRequestWithProfile> {
        val result = client.get(
            "friends",
            "friend_id=eq.$userId&status=eq.pending&select=id,created_at,user:profiles!friends_user_id_fkey(*)"
        )
        if (result.isSuccess) {
            return json.decodeFromString(result.getOrThrow())
        }
        return emptyList()
    }

    suspend fun searchUsers(userId: String, query: String, friendIds: List<String>): List<Profile> {
        if (query.length < 1) return emptyList()
        val result = client.get(
            "profiles",
            "username=ilike.*$query*&id=neq.$userId&select=*&limit=10"
        )
        if (result.isSuccess) {
            val profiles = json.decodeFromString<List<Profile>>(result.getOrThrow())
            return profiles.filter { it.id !in friendIds }
        }
        return emptyList()
    }

    suspend fun sendFriendRequest(userId: String, username: String): Result<Unit> {
        // Find user by username
        val findResult = client.get("profiles", "username=eq.$username&select=*")
        if (findResult.isFailure) return Result.failure(Exception("User not found"))

        val profiles = json.decodeFromString<List<Profile>>(findResult.getOrThrow())
        val friend = profiles.firstOrNull() ?: return Result.failure(Exception("User not found"))

        if (friend.id == userId) return Result.failure(Exception("You can't add yourself as a friend"))

        // Check if already friends or pending
        val checkResult = client.get(
            "friends",
            "or=(and(user_id.eq.$userId,friend_id.eq.${friend.id}),and(user_id.eq.${friend.id},friend_id.eq.$userId))&select=*"
        )
        if (checkResult.isSuccess) {
            val existing = json.decodeFromString<List<Friend>>(checkResult.getOrThrow())
            if (existing.isNotEmpty()) {
                val status = existing.first().status
                return if (status == "accepted") {
                    Result.failure(Exception("Already friends!"))
                } else {
                    Result.failure(Exception("Friend request already pending"))
                }
            }
        }

        // Send request
        val body = json.encodeToString(mapOf(
            "user_id" to userId,
            "friend_id" to friend.id,
            "status" to "pending",
        ))
        val result = client.post("friends", body)
        return if (result.isSuccess) Result.success(Unit)
        else Result.failure(Exception(result.exceptionOrNull()?.message ?: "Failed to send request"))
    }

    suspend fun acceptFriendRequest(requestId: String): Boolean {
        val body = """{"status":"accepted"}"""
        val result = client.patch("friends", "id=eq.$requestId", body)
        return result.isSuccess
    }

    suspend fun rejectFriendRequest(requestId: String): Boolean {
        val result = client.delete("friends", "id=eq.$requestId")
        return result.isSuccess
    }

    suspend fun getFriendStatus(friendId: String): UserStatus? {
        val result = client.get("user_status", "user_id=eq.$friendId&select=*")
        if (result.isSuccess) {
            val statuses = json.decodeFromString<List<UserStatus>>(result.getOrThrow())
            val status = statuses.firstOrNull() ?: return null

            // Staleness check: if updated_at is older than 2 minutes, treat as offline
            val updatedAt = status.updatedAt ?: return null
            try {
                val instant = java.time.Instant.parse(updatedAt)
                val twoMinutesAgo = java.time.Instant.now().minusSeconds(120)
                if (instant.isBefore(twoMinutesAgo)) return null
            } catch (_: Exception) { }

            return status
        }
        return null
    }

    suspend fun fetchNotifications(userId: String): List<PeepWithProfile> {
        val result = client.get(
            "peeps",
            "to_user_id=eq.$userId&select=id,friendly_name,created_at,from_user:profiles!peeps_from_user_id_fkey(username)&order=created_at.desc&limit=50"
        )
        if (result.isSuccess) {
            return json.decodeFromString(result.getOrThrow())
        }
        return emptyList()
    }
}
