package com.pricelens.core.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user preference state such as theme mode and onboarding status.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    enum class ThemeMode {
        SYSTEM, LIGHT, DARK
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeModeFlow = MutableStateFlow(getThemeMode())
    val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()

    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var hasRequestedCameraPermission: Boolean
        get() = prefs.getBoolean(KEY_REQUESTED_CAMERA, false)
        set(value) = prefs.edit().putBoolean(KEY_REQUESTED_CAMERA, value).apply()

    var hasRequestedNotificationPermission: Boolean
        get() = prefs.getBoolean(KEY_REQUESTED_NOTIFICATIONS, false)
        set(value) = prefs.edit().putBoolean(KEY_REQUESTED_NOTIFICATIONS, value).apply()

    fun getThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name) ?: ThemeMode.DARK.name
        return try {
            ThemeMode.valueOf(name)
        } catch (_: Exception) {
            ThemeMode.DARK
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeModeFlow.value = mode
    }

    fun toggleTheme(currentIsDark: Boolean) {
        val next = if (currentIsDark) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(next)
    }

    fun resetAll() {
        prefs.edit().clear().apply()
        _themeModeFlow.value = ThemeMode.DARK
    }

    companion object {
        private const val PREFS_NAME = "pricelens_user_preferences"
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        private const val KEY_REQUESTED_CAMERA = "key_requested_camera"
        private const val KEY_REQUESTED_NOTIFICATIONS = "key_requested_notifications"
        private const val KEY_THEME_MODE = "key_theme_mode"
    }
}
