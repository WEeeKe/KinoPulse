package com.example.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kinopulse_prefs")

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.NIGHT,
    val amoledNightMode: Boolean = false,
    val offlineModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val userDisplayName: String = "Кинозритель",
    val deviceId: String = "",
    val lastSyncTimestamp: Long = 0L
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AMOLED_NIGHT = booleanPreferencesKey("amoled_night")
        val OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
        val DEVICE_ID = stringPreferencesKey("device_id")
        val LAST_SYNC_TS = longPreferencesKey("last_sync_ts")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.NIGHT.name
        val themeMode = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.NIGHT)
        UserPreferences(
            themeMode = themeMode,
            amoledNightMode = prefs[Keys.AMOLED_NIGHT] ?: false,
            offlineModeEnabled = prefs[Keys.OFFLINE_MODE] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            userDisplayName = prefs[Keys.USER_DISPLAY_NAME] ?: "Кинозритель",
            deviceId = prefs[Keys.DEVICE_ID] ?: "KP-${UUID.randomUUID().toString().take(6).uppercase()}",
            lastSyncTimestamp = prefs[Keys.LAST_SYNC_TS] ?: 0L
        )
    }

    suspend fun ensureDeviceId(): String {
        val current = context.dataStore.data.first()[Keys.DEVICE_ID]
        if (!current.isNullOrBlank()) return current
        val generated = "KP-${UUID.randomUUID().toString().take(6).uppercase()}"
        context.dataStore.edit { it[Keys.DEVICE_ID] = generated }
        return generated
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun toggleNightMode() {
        context.dataStore.edit { prefs ->
            val current = runCatching {
                ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.NIGHT.name)
            }.getOrDefault(ThemeMode.NIGHT)
            val next = if (current == ThemeMode.NIGHT) ThemeMode.DAY else ThemeMode.NIGHT
            prefs[Keys.THEME_MODE] = next.name
        }
    }

    suspend fun setAmoledNightMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AMOLED_NIGHT] = enabled }
    }

    suspend fun setOfflineMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.OFFLINE_MODE] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setUserDisplayName(name: String) {
        val clean = name.trim().ifEmpty { "Кинозритель" }
        context.dataStore.edit { it[Keys.USER_DISPLAY_NAME] = clean }
    }

    suspend fun updateLastSyncTimestamp(timestamp: Long = System.currentTimeMillis()) {
        context.dataStore.edit { it[Keys.LAST_SYNC_TS] = timestamp }
    }
}
