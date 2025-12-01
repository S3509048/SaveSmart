package uk.ac.tees.mad.savesmart.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property to create DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // Define preference keys
    private object PreferenceKeys {
        val CURRENCY = stringPreferencesKey("currency")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val USER_NAME = stringPreferencesKey("user_name")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    // ===== CURRENCY =====
    suspend fun saveCurrency(currency: String) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.CURRENCY] = currency
        }
    }

    val currencyFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.CURRENCY] ?: "GBP"
    }

    // ===== THEME =====
    suspend fun saveTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_MODE] = theme
        }
    }

    val themeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.THEME_MODE] ?: "light"
    }

    // ===== NOTIFICATIONS =====
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    val notificationsEnabledFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    // ===== BIOMETRIC =====
    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.BIOMETRIC_ENABLED] = enabled
        }
    }

    val biometricEnabledFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.BIOMETRIC_ENABLED] ?: false
    }

    // ===== USER NAME (CACHE) =====
    suspend fun saveUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.USER_NAME] = name
        }
    }

    val userNameFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.USER_NAME] ?: "User"
    }

    // ===== LAST SYNC =====
    suspend fun saveLastSyncTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.LAST_SYNC_TIME] = timestamp
        }
    }

    val lastSyncTimeFlow: Flow<Long> = dataStore.data.map { preferences ->
        preferences[PreferenceKeys.LAST_SYNC_TIME] ?: 0L
    }

    // ===== CLEAR ALL (FOR LOGOUT) =====
    suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}