package com.example.financetracker.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.financetracker.BuildConfig
import com.example.financetracker.core.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    companion object {
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        val KEY_USER_ID = longPreferencesKey("user_id")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_LAST_SYNC = stringPreferencesKey("last_sync_timestamp")
        val KEY_BIOMETRICS_ENABLED = booleanPreferencesKey("biometrics_enabled")

        val DEFAULT_SERVER_URL: String get() = BuildConfig.API_BASE_URL
    }

    val authToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTH_TOKEN]?.let { CryptoManager.decrypt(it) }
    }
    val userId: Flow<Long?> = context.dataStore.data.map { it[KEY_USER_ID] }
    val userName: Flow<String?> = context.dataStore.data.map { it[KEY_USER_NAME] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[KEY_USER_EMAIL] }
    val serverUrl: Flow<String> = flowOf(BuildConfig.API_BASE_URL)
    val lastSyncTimestamp: Flow<String?> = context.dataStore.data.map { it[KEY_LAST_SYNC] }
    val isBiometricsEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_BIOMETRICS_ENABLED] ?: false }

    suspend fun saveAuthSession(token: String, id: Long, name: String, email: String) {
        val encryptedToken = CryptoManager.encrypt(token)
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTH_TOKEN] = encryptedToken
            prefs[KEY_USER_ID] = id
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_EMAIL] = email
        }
    }

    suspend fun saveLastSync(timestamp: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_SYNC] = timestamp
        }
    }

    suspend fun setBiometricsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BIOMETRICS_ENABLED] = enabled
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_AUTH_TOKEN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_NAME)
            prefs.remove(KEY_USER_EMAIL)
            prefs.remove(KEY_LAST_SYNC)
        }
    }
}
