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
    private val balanceVisibleKey = booleanPreferencesKey("balance_visible")

    val themeMode: Flow<ThemeMode> = context.hkPayDataStore.data.map { prefs ->
        val raw = prefs[themeModeKey] ?: ThemeMode.NAVY.name
        when (raw) {
            "SYSTEM" -> ThemeMode.NAVY
            else -> runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.NAVY)
        }
    }

    val biometricEnabled: Flow<Boolean> = context.hkPayDataStore.data.map { prefs ->
        prefs[biometricEnabledKey] ?: false
    }

    val tutorialCompleted: Flow<Boolean> = context.hkPayDataStore.data.map { prefs ->
        prefs[tutorialCompletedKey] ?: false
    }

    val balanceVisible: Flow<Boolean> = context.hkPayDataStore.data.map { prefs ->
        prefs[balanceVisibleKey] ?: true
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

    suspend fun setBalanceVisible(visible: Boolean) {
        context.hkPayDataStore.edit { prefs ->
            prefs[balanceVisibleKey] = visible
        }
    }
}
