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

    data class ExperimentalFlags(
        val isMasterEnabled: Boolean = true,
        val enableDealAuthenticity: Boolean = true,
        val enableBuyNowVerdict: Boolean = true,
        val enableBankCardCalculator: Boolean = true,
        val enableAlertPresets: Boolean = true,
        val enableDynamicSearch: Boolean = true,
        val enablePriceDropForecast: Boolean = true
    )

    private val _themeModeFlow = MutableStateFlow(getThemeMode())
    val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()

    private val _experimentalFlagsFlow = MutableStateFlow(getExperimentalFlags())
    val experimentalFlagsFlow: StateFlow<ExperimentalFlags> = _experimentalFlagsFlow.asStateFlow()

    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var hasRequestedCameraPermission: Boolean
        get() = prefs.getBoolean(KEY_REQUESTED_CAMERA, false)
        set(value) = prefs.edit().putBoolean(KEY_REQUESTED_CAMERA, value).apply()

    var hasRequestedNotificationPermission: Boolean
        get() = prefs.getBoolean(KEY_REQUESTED_NOTIFICATIONS, false)
        set(value) = prefs.edit().putBoolean(KEY_REQUESTED_NOTIFICATIONS, value).apply()

    fun getExperimentalFlags(): ExperimentalFlags {
        return ExperimentalFlags(
            isMasterEnabled = prefs.getBoolean(KEY_EXP_MASTER, true),
            enableDealAuthenticity = prefs.getBoolean(KEY_EXP_DEAL_AUTHENTICITY, true),
            enableBuyNowVerdict = prefs.getBoolean(KEY_EXP_BUY_NOW_VERDICT, true),
            enableBankCardCalculator = prefs.getBoolean(KEY_EXP_BANK_CARD, true),
            enableAlertPresets = prefs.getBoolean(KEY_EXP_ALERT_PRESETS, true),
            enableDynamicSearch = prefs.getBoolean(KEY_EXP_DYNAMIC_SEARCH, true),
            enablePriceDropForecast = prefs.getBoolean(KEY_EXP_PRICE_DROP_FORECAST, true)
        )
    }

    fun setExperimentalMasterEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_EXP_MASTER, enabled).apply()
        _experimentalFlagsFlow.value = getExperimentalFlags()
    }

    fun setExperimentalFlag(key: String, enabled: Boolean) {
        prefs.edit().putBoolean(key, enabled).apply()
        _experimentalFlagsFlow.value = getExperimentalFlags()
    }

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

        const val KEY_EXP_MASTER = "key_exp_master"
        const val KEY_EXP_DEAL_AUTHENTICITY = "key_exp_deal_authenticity"
        const val KEY_EXP_BUY_NOW_VERDICT = "key_exp_buy_now_verdict"
        const val KEY_EXP_BANK_CARD = "key_exp_bank_card"
        const val KEY_EXP_ALERT_PRESETS = "key_exp_alert_presets"
        const val KEY_EXP_DYNAMIC_SEARCH = "key_exp_dynamic_search"
        const val KEY_EXP_PRICE_DROP_FORECAST = "key_exp_price_drop_forecast"
    }
}
