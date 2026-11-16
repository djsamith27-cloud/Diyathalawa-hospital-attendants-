package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "attendance_prefs")

class ThemePreferences(private val context: Context) {
    private object Keys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val INITIAL_SEED_DONE = booleanPreferencesKey("initial_seed_done")
        val APP_PASSWORD_HASH = stringPreferencesKey("app_password_hash")
        val WELCOME_SOUND_ENABLED = booleanPreferencesKey("welcome_sound_enabled")
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_DARK_THEME] ?: false
    }

    val isPasswordEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        !prefs[Keys.APP_PASSWORD_HASH].isNullOrEmpty()
    }

    val isWelcomeSoundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.WELCOME_SOUND_ENABLED] ?: true
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_DARK_THEME] = isDark
        }
    }

    suspend fun setWelcomeSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WELCOME_SOUND_ENABLED] = enabled
        }
    }

    suspend fun setPassword(rawPassword: String) {
        val hashed = hashPassword(rawPassword)
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_PASSWORD_HASH] = hashed
        }
    }

    suspend fun verifyPassword(rawPassword: String): Boolean {
        val storedHash = context.dataStore.data.first()[Keys.APP_PASSWORD_HASH]
        if (storedHash.isNullOrEmpty()) return true
        return storedHash == hashPassword(rawPassword)
    }

    suspend fun clearPassword() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.APP_PASSWORD_HASH)
        }
    }

    suspend fun isInitialSeedDone(): Boolean {
        return context.dataStore.data.first()[Keys.INITIAL_SEED_DONE] ?: false
    }

    suspend fun markInitialSeedDone() {
        context.dataStore.edit { prefs ->
            prefs[Keys.INITIAL_SEED_DONE] = true
        }
    }

    private fun hashPassword(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }
}
