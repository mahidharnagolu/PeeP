package com.anonymous.peep.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val username: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("daily_peeps_remaining") val dailyPeepsRemaining: Int = 5,
    @SerialName("last_peep_reset") val lastPeepReset: String? = null,
    @SerialName("fcm_token") val fcmToken: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class Friend(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("friend_id") val friendId: String,
    val status: String, // "pending" | "accepted"
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class Peep(
    val id: String,
    @SerialName("from_user_id") val fromUserId: String,
    @SerialName("to_user_id") val toUserId: String,
    @SerialName("detected_app") val detectedApp: String? = null,
    @SerialName("friendly_name") val friendlyName: String? = null,
    @SerialName("created_at") val createdAt: String = "",
)

@Serializable
data class UserStatus(
    @SerialName("user_id") val userId: String,
    @SerialName("current_app") val currentApp: String? = null,
    @SerialName("friendly_name") val friendlyName: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

// Auth response types
@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Long = 0,
    @SerialName("token_type") val tokenType: String = "bearer",
    val user: AuthUser,
)

@Serializable
data class AuthUser(
    val id: String,
    val email: String? = null,
    @SerialName("user_metadata") val userMetadata: UserMetadata? = null,
)

@Serializable
data class UserMetadata(
    val username: String? = null,
)

@Serializable
data class AuthSignUpRequest(
    val email: String,
    val password: String,
    val data: UserMetadata? = null,
)

@Serializable
data class AuthSignInRequest(
    val email: String,
    val password: String,
)

@Serializable
data class AuthRefreshRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

// PostgREST nested join models
@Serializable
data class FriendWithProfile(
    val id: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val friend: Profile? = null,
)

@Serializable
data class FriendRequestWithProfile(
    val id: String,
    @SerialName("created_at") val createdAt: String? = null,
    val user: Profile? = null,
)

@Serializable
data class PeepWithProfile(
    val id: String,
    @SerialName("friendly_name") val friendlyName: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("from_user") val fromUser: ProfileUsername? = null,
)

@Serializable
data class ProfileUsername(
    val username: String? = null,
)
