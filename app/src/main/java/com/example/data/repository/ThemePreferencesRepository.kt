package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppLanguage
import com.example.data.model.ThemeMode
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.hkPayDataStore by preferencesDataStore(name = "hkpay_preferences")

class ThemePreferencesRepository(private val context: Context) {
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val appLanguageKey = stringPreferencesKey("app_language")
    private val biometricEnabledKey = booleanPreferencesKey("biometric_enabled")
    private val tutorialCompletedKey = booleanPreferencesKey("tutorial_completed")
    private val balanceVisibleKey = booleanPreferencesKey("balance_visible")
    private val pinLockEnabledKey = booleanPreferencesKey("pin_lock_enabled")
    private val pinHashKey = stringPreferencesKey("pin_hash")
    private val pinSaltKey = stringPreferencesKey("pin_salt")
    private val pinUidKey = stringPreferencesKey("pin_uid")
    private val pinEmailKey = stringPreferencesKey("pin_email")
    private val twoFactorEnabledKey = booleanPreferencesKey("two_factor_enabled")
    private val twoFactorSecretKey = stringPreferencesKey("two_factor_secret")
    private val permissionsWizardDoneKey = booleanPreferencesKey("permissions_wizard_done")

    val themeMode: Flow<ThemeMode> = context.hkPayDataStore.data.map { prefs ->
        val raw = prefs[themeModeKey] ?: ThemeMode.NAVY.name
        when (raw) {
            "SYSTEM" -> ThemeMode.NAVY
            else -> runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.NAVY)
        }
    }

    val appLanguage: Flow<AppLanguage> = context.hkPayDataStore.data.map { prefs ->
        val raw = prefs[appLanguageKey] ?: AppLanguage.ENGLISH.name
        runCatching { AppLanguage.valueOf(raw) }.getOrDefault(AppLanguage.ENGLISH)
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

    val pinLockEnabled: Flow<Boolean> = context.hkPayDataStore.data.map { prefs ->
        (prefs[pinLockEnabledKey] ?: false) && !prefs[pinHashKey].isNullOrBlank()
    }

    val twoFactorEnabled: Flow<Boolean> = context.hkPayDataStore.data.map { prefs ->
        prefs[twoFactorEnabledKey] ?: false
    }

    val twoFactorSecret: Flow<String> = context.hkPayDataStore.data.map { prefs ->
        prefs[twoFactorSecretKey].orEmpty()
    }

    val permissionsWizardDone: Flow<Boolean> = context.hkPayDataStore.data.map { prefs ->
        prefs[permissionsWizardDoneKey] ?: false
    }

    val savedPinEmail: Flow<String> = context.hkPayDataStore.data.map { prefs ->
        prefs[pinEmailKey].orEmpty()
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.hkPayDataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        context.hkPayDataStore.edit { prefs ->
            prefs[appLanguageKey] = language.name
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

    suspend fun setPinCode(uid: String, email: String, pin: String) {
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPin(uid = salt, pin = pin)
        context.hkPayDataStore.edit { prefs ->
            prefs[pinLockEnabledKey] = true
            prefs[pinSaltKey] = salt
            prefs[pinHashKey] = hash
            if (uid.isNotBlank()) prefs[pinUidKey] = uid
            if (email.isNotBlank()) prefs[pinEmailKey] = email.trim()
        }
    }

    suspend fun verifyPinCode(pin: String): Boolean {
        val prefs = context.hkPayDataStore.data.first()
        val storedHash = prefs[pinHashKey].orEmpty()
        val salt = prefs[pinSaltKey].orEmpty()
        if (storedHash.isBlank() || salt.isBlank()) return false
        return SecurityUtils.verifyPin(uid = salt, pin = pin, expectedHash = storedHash)
    }

    suspend fun hasPinConfiguredForEmail(email: String): Boolean {
        val prefs = context.hkPayDataStore.data.first()
        val enabled = prefs[pinLockEnabledKey] ?: false
        val savedEmail = prefs[pinEmailKey].orEmpty()
        val hash = prefs[pinHashKey].orEmpty()
        return enabled && hash.isNotBlank() && savedEmail.equals(email.trim(), ignoreCase = true)
    }

    suspend fun disablePinLock() {
        context.hkPayDataStore.edit { prefs ->
            prefs[pinLockEnabledKey] = false
            prefs.remove(pinHashKey)
            prefs.remove(pinSaltKey)
        }
    }

    suspend fun setTwoFactorConfig(enabled: Boolean, secret: String) {
        context.hkPayDataStore.edit { prefs ->
            prefs[twoFactorEnabledKey] = enabled
            if (secret.isNotBlank()) {
                prefs[twoFactorSecretKey] = secret
            } else if (!enabled) {
                prefs.remove(twoFactorSecretKey)
            }
        }
    }

    suspend fun setPermissionsWizardDone(done: Boolean) {
        context.hkPayDataStore.edit { prefs ->
            prefs[permissionsWizardDoneKey] = done
        }
    }
}
