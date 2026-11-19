package com.pricelens.core.preferences

import android.content.Context
import com.pricelens.core.designsystem.ThemeAccent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user preference state such as theme mode, theme accents, and onboarding status.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    enum class ThemeMode {
        SYSTEM, LIGHT, DARK
    }

    enum class GlowIntensity(val displayName: String, val subtitle: String, val multiplier: Float) {
        SUBTLE("Subtle Pro", "Minimalist sleek glow", 0.65f),
        VIVID("Vivid Neon", "Dynamic signature glow", 1.0f),
        ULTRA("Ultra Radiant", "High-contrast glowing aura", 1.4f);

        companion object {
            fun fromName(name: String?): GlowIntensity {
                return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: VIVID
            }
        }
    }

    enum class ContrastStyle(val displayName: String, val subtitle: String) {
        PITCH_BLACK("AMOLED Pitch", "Pure #000000 canvas"),
        OBSIDIAN_TINT("Deep Obsidian", "Tinted ambient aura canvas");

        companion object {
            fun fromName(name: String?): ContrastStyle {
                return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: PITCH_BLACK
            }
        }
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class ExperimentalFlags(
        val isMasterEnabled: Boolean = true,
        val enableDealAuthenticity: Boolean = true,
        val enableBuyNowVerdict: Boolean = true,
        val enableBankCardCalculator: Boolean = true,
        val enableAlertPresets: Boolean = true,
        val enableDynamicSearch: Boolean = true,
        val enablePriceDropForecast: Boolean = true,
        val enableCouponFinder: Boolean = true
    )

    private val _themeModeFlow = MutableStateFlow(getThemeMode())
    val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()

    private val _themeAccentFlow = MutableStateFlow(getThemeAccent())
    val themeAccentFlow: StateFlow<ThemeAccent> = _themeAccentFlow.asStateFlow()

    private val _glowIntensityFlow = MutableStateFlow(getGlowIntensity())
    val glowIntensityFlow: StateFlow<GlowIntensity> = _glowIntensityFlow.asStateFlow()

    private val _contrastStyleFlow = MutableStateFlow(getContrastStyle())
    val contrastStyleFlow: StateFlow<ContrastStyle> = _contrastStyleFlow.asStateFlow()

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
            enablePriceDropForecast = prefs.getBoolean(KEY_EXP_PRICE_DROP_FORECAST, true),
            enableCouponFinder = prefs.getBoolean(KEY_EXP_COUPON_FINDER, true)
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

    fun getThemeAccent(): ThemeAccent {
        val name = prefs.getString(KEY_THEME_ACCENT, ThemeAccent.EMERALD_MINT.name) ?: ThemeAccent.EMERALD_MINT.name
        return try {
            ThemeAccent.valueOf(name)
        } catch (_: Exception) {
            ThemeAccent.EMERALD_MINT
        }
    }

    fun setThemeAccent(accent: ThemeAccent) {
        prefs.edit().putString(KEY_THEME_ACCENT, accent.name).apply()
        _themeAccentFlow.value = accent
    }

    fun getGlowIntensity(): GlowIntensity {
        val name = prefs.getString(KEY_THEME_GLOW_INTENSITY, GlowIntensity.VIVID.name) ?: GlowIntensity.VIVID.name
        return GlowIntensity.fromName(name)
    }

    fun setGlowIntensity(intensity: GlowIntensity) {
        prefs.edit().putString(KEY_THEME_GLOW_INTENSITY, intensity.name).apply()
        _glowIntensityFlow.value = intensity
    }

    fun getContrastStyle(): ContrastStyle {
        val name = prefs.getString(KEY_THEME_CONTRAST_STYLE, ContrastStyle.PITCH_BLACK.name) ?: ContrastStyle.PITCH_BLACK.name
        return ContrastStyle.fromName(name)
    }

    fun setContrastStyle(style: ContrastStyle) {
        prefs.edit().putString(KEY_THEME_CONTRAST_STYLE, style.name).apply()
        _contrastStyleFlow.value = style
    }

    fun toggleTheme(currentIsDark: Boolean) {
        val next = if (currentIsDark) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(next)
    }

    fun resetAll() {
        prefs.edit().clear().apply()
        _themeModeFlow.value = ThemeMode.DARK
        _themeAccentFlow.value = ThemeAccent.EMERALD_MINT
        _glowIntensityFlow.value = GlowIntensity.VIVID
        _contrastStyleFlow.value = ContrastStyle.PITCH_BLACK
    }

    companion object {
        private const val PREFS_NAME = "pricelens_user_preferences"
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        private const val KEY_REQUESTED_CAMERA = "key_requested_camera"
        private const val KEY_REQUESTED_NOTIFICATIONS = "key_requested_notifications"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_THEME_ACCENT = "key_theme_accent"
        const val KEY_THEME_GLOW_INTENSITY = "key_theme_glow_intensity"
        const val KEY_THEME_CONTRAST_STYLE = "key_theme_contrast_style"

        const val KEY_EXP_MASTER = "key_exp_master"
        const val KEY_EXP_DEAL_AUTHENTICITY = "key_exp_deal_authenticity"
        const val KEY_EXP_BUY_NOW_VERDICT = "key_exp_buy_now_verdict"
        const val KEY_EXP_BANK_CARD = "key_exp_bank_card"
        const val KEY_EXP_ALERT_PRESETS = "key_exp_alert_presets"
        const val KEY_EXP_DYNAMIC_SEARCH = "key_exp_dynamic_search"
        const val KEY_EXP_PRICE_DROP_FORECAST = "key_exp_price_drop_forecast"
        const val KEY_EXP_COUPON_FINDER = "key_exp_coupon_finder"
    }
}
