package com.anonymous.peep.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anonymous.peep.BuildConfig
import com.anonymous.peep.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionStore: DataStore<Preferences> by preferencesDataStore(name = "peep_session")

@Singleton
class SupabaseClient @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val baseUrl = BuildConfig.SUPABASE_URL
    private val anonKey = BuildConfig.SUPABASE_ANON_KEY

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val refreshMutex = Mutex()

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
                .header("apikey", anonKey)
                .header("Content-Type", "application/json")
            // Add auth token if available (synchronous read from cache)
            cachedAccessToken?.let {
                builder.header("Authorization", "Bearer $it")
            }
            chain.proceed(builder.build())
        }
        .authenticator { _, response ->
            // Only retry once to avoid infinite loops
            if (response.request.header("X-Retry-After-Refresh") != null) {
                Log.w(TAG, "Token refresh already attempted, giving up")
                return@authenticator null
            }

            Log.d(TAG, "Got 401 on ${response.request.url.encodedPath}, refreshing token...")

            // Refresh the token synchronously (OkHttp authenticator runs on OkHttp thread)
            val newToken = runBlocking {
                try {
                    refreshTokenAndGet()
                } catch (e: Exception) {
                    Log.e(TAG, "Token refresh failed: ${e.message}")
                    null
                }
            } ?: return@authenticator null

            Log.d(TAG, "Token refreshed successfully, retrying request")
            response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .header("X-Retry-After-Refresh", "true")
                .build()
        }
        .build()

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    // Session keys
    private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val KEY_USER_ID = stringPreferencesKey("user_id")

    // In-memory cache for interceptor
    @Volatile
    var cachedAccessToken: String? = null
        private set

    @Volatile
    var cachedUserId: String? = null
        private set

    // ──────────────── Session Management ────────────────

    suspend fun loadSession(): Boolean {
        val prefs = context.sessionStore.data.first()
        cachedAccessToken = prefs[KEY_ACCESS_TOKEN]
        cachedUserId = prefs[KEY_USER_ID]
        val refreshToken = prefs[KEY_REFRESH_TOKEN]

        if (cachedAccessToken != null && refreshToken != null) {
            // Try refreshing the token
            return try {
                refreshSession(refreshToken)
                true
            } catch (e: Exception) {
                clearSession()
                false
            }
        }
        return false
    }

    suspend fun saveSession(accessToken: String, refreshToken: String, userId: String) {
        cachedAccessToken = accessToken
        cachedUserId = userId
        context.sessionStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_ID] = userId
        }
    }

    suspend fun clearSession() {
        cachedAccessToken = null
        cachedUserId = null
        context.sessionStore.edit { it.clear() }
    }

    private suspend fun refreshSession(refreshToken: String) {
        refreshMutex.withLock {
            val body = json.encodeToString(AuthRefreshRequest(refreshToken))
            // Use a separate client without the authenticator to avoid recursion
            val refreshClient = OkHttpClient()
            val request = Request.Builder()
                .url("$baseUrl/auth/v1/token?grant_type=refresh_token")
                .post(body.toRequestBody(JSON_MEDIA))
                .header("apikey", anonKey)
                .header("Content-Type", "application/json")
                .build()

            val response = refreshClient.newCall(request).executeSuspend()
            if (response.isSuccessful) {
                val authResponse = json.decodeFromString<AuthResponse>(response.body!!.string())
                saveSession(authResponse.accessToken, authResponse.refreshToken, authResponse.user.id)
                Log.d(TAG, "Session refreshed successfully for user ${authResponse.user.id}")
            } else {
                val errorBody = response.body?.string() ?: "no body"
                Log.e(TAG, "Token refresh failed: ${response.code} $errorBody")
                throw IOException("Token refresh failed: ${response.code}")
            }
        }
    }

    /**
     * Called by the OkHttp Authenticator on 401.
     * Reads the refresh token from DataStore and refreshes the session.
     * Returns the new access token, or null if refresh failed.
     */
    private suspend fun refreshTokenAndGet(): String? {
        val prefs = context.sessionStore.data.first()
        val refreshToken = prefs[KEY_REFRESH_TOKEN] ?: return null
        refreshSession(refreshToken)
        return cachedAccessToken
    }

    /**
     * Proactive refresh — call periodically from the foreground service
     * to avoid hitting 401 in the first place.
     */
    suspend fun refreshSessionIfNeeded() {
        val prefs = context.sessionStore.data.first()
        val refreshToken = prefs[KEY_REFRESH_TOKEN] ?: return
        try {
            refreshSession(refreshToken)
        } catch (e: Exception) {
            Log.e(TAG, "Proactive refresh failed: ${e.message}")
        }
    }

    // ──────────────── Auth Endpoints ────────────────

    suspend fun signUp(email: String, password: String, username: String): Result<AuthResponse> {
        val signUpRequest = AuthSignUpRequest(email, password, UserMetadata(username))
        val body = json.encodeToString(signUpRequest)
        val request = Request.Builder()
            .url("$baseUrl/auth/v1/signup")
            .post(body.toRequestBody(JSON_MEDIA))
            .header("apikey", anonKey)
            .header("Content-Type", "application/json")
            .build()

        return try {
            val response = httpClient.newCall(request).executeSuspend()
            val responseBody = response.body!!.string()
            if (response.isSuccessful) {
                val authResponse = json.decodeFromString<AuthResponse>(responseBody)
                saveSession(authResponse.accessToken, authResponse.refreshToken, authResponse.user.id)
                Result.success(authResponse)
            } else {
                val errorMsg = try {
                    json.decodeFromString<Map<String, String>>(responseBody)["msg"]
                        ?: json.decodeFromString<Map<String, String>>(responseBody)["error_description"]
                        ?: "Sign up failed"
                } catch (_: Exception) { "Sign up failed: $responseBody" }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<AuthResponse> {
        val body = json.encodeToString(AuthSignInRequest(email, password))
        val request = Request.Builder()
            .url("$baseUrl/auth/v1/token?grant_type=password")
            .post(body.toRequestBody(JSON_MEDIA))
            .header("apikey", anonKey)
            .header("Content-Type", "application/json")
            .build()

        return try {
            val response = httpClient.newCall(request).executeSuspend()
            val responseBody = response.body!!.string()
            if (response.isSuccessful) {
                val authResponse = json.decodeFromString<AuthResponse>(responseBody)
                saveSession(authResponse.accessToken, authResponse.refreshToken, authResponse.user.id)
                Result.success(authResponse)
            } else {
                val errorMsg = try {
                    json.decodeFromString<Map<String, String>>(responseBody)["error_description"]
                        ?: "Sign in failed"
                } catch (_: Exception) { "Sign in failed" }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            val request = Request.Builder()
                .url("$baseUrl/auth/v1/logout")
                .post("".toRequestBody(JSON_MEDIA))
                .build()
            httpClient.newCall(request).executeSuspend()
        } catch (_: Exception) { }
        clearSession()
    }

    // ──────────────── PostgREST Endpoints ────────────────

    suspend fun get(table: String, query: String = ""): Result<String> {
        val url = "$baseUrl/rest/v1/$table${if (query.isNotEmpty()) "?$query" else ""}"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return try {
            val response = httpClient.newCall(request).executeSuspend()
            val body = response.body!!.string()
            if (response.isSuccessful) Result.success(body)
            else Result.failure(Exception("GET $table failed: ${response.code} $body"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun post(table: String, body: String, prefer: String = ""): Result<String> {
        val url = "$baseUrl/rest/v1/$table"
        val builder = Request.Builder()
            .url(url)
            .post(body.toRequestBody(JSON_MEDIA))
        if (prefer.isNotEmpty()) builder.header("Prefer", prefer)
        builder.header("Prefer", "return=representation")

        return try {
            val response = httpClient.newCall(builder.build()).executeSuspend()
            val responseBody = response.body!!.string()
            if (response.isSuccessful) Result.success(responseBody)
            else Result.failure(Exception("POST $table failed: ${response.code} $responseBody"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsert(table: String, body: String, onConflict: String): Result<String> {
        val url = "$baseUrl/rest/v1/$table?on_conflict=$onConflict"
        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody(JSON_MEDIA))
            .header("Prefer", "resolution=merge-duplicates,return=representation")
            .build()

        return try {
            val response = httpClient.newCall(request).executeSuspend()
            val responseBody = response.body!!.string()
            if (response.isSuccessful) Result.success(responseBody)
            else Result.failure(Exception("UPSERT $table failed: ${response.code} $responseBody"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun patch(table: String, query: String, body: String): Result<String> {
        val url = "$baseUrl/rest/v1/$table?$query"
        val request = Request.Builder()
            .url(url)
            .patch(body.toRequestBody(JSON_MEDIA))
            .header("Prefer", "return=representation")
            .build()

        return try {
            val response = httpClient.newCall(request).executeSuspend()
            val responseBody = response.body!!.string()
            if (response.isSuccessful) Result.success(responseBody)
            else Result.failure(Exception("PATCH $table failed: ${response.code} $responseBody"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(table: String, query: String): Result<String> {
        val url = "$baseUrl/rest/v1/$table?$query"
        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        return try {
            val response = httpClient.newCall(request).executeSuspend()
            val responseBody = response.body?.string() ?: ""
            if (response.isSuccessful) Result.success(responseBody)
            else Result.failure(Exception("DELETE $table failed: ${response.code} $responseBody"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ──────────────── Edge Functions ────────────────

    suspend fun invokeFunction(name: String, body: String): Result<String> {
        val url = "$baseUrl/functions/v1/$name"
        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody(JSON_MEDIA))
            .build()

        return try {
            val response = httpClient.newCall(request).executeSuspend()
            val responseBody = response.body?.string() ?: ""
            if (response.isSuccessful) Result.success(responseBody)
            else Result.failure(Exception("Function $name failed: ${response.code} $responseBody"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Expose base URL and key for background service
    fun getBaseUrl() = baseUrl
    fun getAnonKey() = anonKey
    fun getAccessToken() = cachedAccessToken

    companion object {
        private const val TAG = "SupabaseClient"
    }
}

// Extension to make OkHttp calls suspendable
suspend fun Call.executeSuspend(): Response {
    return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        cont.invokeOnCancellation { cancel() }
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isActive) cont.resumeWith(Result.failure(e))
            }
            override fun onResponse(call: Call, response: Response) {
                cont.resumeWith(Result.success(response))
            }
        })
    }
}
