package com.pricelens.core.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user preference state such as onboarding completion status.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var hasRequestedCameraPermission: Boolean
        get() = prefs.getBoolean(KEY_REQUESTED_CAMERA, false)
        set(value) = prefs.edit().putBoolean(KEY_REQUESTED_CAMERA, value).apply()

    var hasRequestedNotificationPermission: Boolean
        get() = prefs.getBoolean(KEY_REQUESTED_NOTIFICATIONS, false)
        set(value) = prefs.edit().putBoolean(KEY_REQUESTED_NOTIFICATIONS, value).apply()

    fun resetAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "pricelens_user_preferences"
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        private const val KEY_REQUESTED_CAMERA = "key_requested_camera"
        private const val KEY_REQUESTED_NOTIFICATIONS = "key_requested_notifications"
    }
}
