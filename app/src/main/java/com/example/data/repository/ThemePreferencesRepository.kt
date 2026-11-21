package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.hkPayDataStore by preferencesDataStore(name = "hkpay_preferences")

class ThemePreferencesRepository(private val context: Context) {
    private val themeModeKey = stringPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> = context.hkPayDataStore.data.map { prefs ->
        val raw = prefs[themeModeKey] ?: ThemeMode.DARK.name
        runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.DARK)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.hkPayDataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }
}
