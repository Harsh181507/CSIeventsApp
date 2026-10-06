package com.example.csievent.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val NAME_KEY = stringPreferencesKey("user_name")
        private val EMAIL_KEY = stringPreferencesKey("user_email")
    }

    // In-memory copy so the network interceptor doesn't read DataStore on every request
    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var tokenLoaded = false

    suspend fun saveToken(token: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[ROLE_KEY] = role
        }
        cachedToken = token
        tokenLoaded = true
    }

    suspend fun saveProfile(name: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[NAME_KEY] = name
            prefs[EMAIL_KEY] = email
        }
    }

    suspend fun saveRole(role: String) {
        context.dataStore.edit { prefs -> prefs[ROLE_KEY] = role }
    }

    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }
    }

    /** Current token for OkHttp interceptors (blocking, reads disk only once). */
    fun currentTokenBlocking(): String? {
        if (!tokenLoaded) {
            cachedToken = runBlocking { getToken().first() }
            tokenLoaded = true
        }
        return cachedToken
    }

    fun getRole(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[ROLE_KEY]
        }
    }

    fun getName(): Flow<String?> = context.dataStore.data.map { it[NAME_KEY] }

    fun getEmail(): Flow<String?> = context.dataStore.data.map { it[EMAIL_KEY] }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
        cachedToken = null
        tokenLoaded = true
    }
}
