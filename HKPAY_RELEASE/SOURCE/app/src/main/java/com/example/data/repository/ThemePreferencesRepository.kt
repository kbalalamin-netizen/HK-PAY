package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.hkPayDataStore by preferencesDataStore(name = "hkpay_preferences")

class ThemePreferencesRepository(private val context: Context) {
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val biometricEnabledKey = booleanPreferencesKey("biometric_enabled")
    private val tutorialCompletedKey = booleanPreferencesKey("tutorial_completed")

    val themeMode: Flow<ThemeMode> = context.hkPayDataStore.data.map { prefs ->
        val raw = prefs[themeModeKey] ?: ThemeMode.DARK.name
        runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.DARK)
    }

    val biometricEnabled: Flow<Boolean> = context.hkPayDataStore.data.map { prefs ->
        prefs[biometricEnabledKey] ?: false
    }

    val tutorialCompleted: Flow<Boolean> = context.hkPayDataStore.data.map { prefs ->
        prefs[tutorialCompletedKey] ?: false
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.hkPayDataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.hkPayDataStore.edit { prefs ->
            prefs[biometricEnabledKey] = enabled
        }
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        context.hkPayDataStore.edit { prefs ->
            prefs[tutorialCompletedKey] = completed
        }
    }
}
