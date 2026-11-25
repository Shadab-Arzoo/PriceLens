package com.pricelens.core.preferences

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pricelens.core.designsystem.AppFont
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
@Stable
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
        OBSIDIAN_TINT("Deep Obsidian", "Tinted ambient aura canvas"),
        APPLE_CUPERTINO("Apple Cupertino", "iOS 18 & visionOS frosted canvas");

        companion object {
            fun fromName(name: String?): ContrastStyle {
                return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: PITCH_BLACK
            }
        }
    }

    enum class GlassRefraction(val displayName: String, val subtitle: String, val multiplier: Float) {
        SUBTLE("Subtle Lens", "Gentle optical curve", 0.5f),
        STANDARD("Standard Lens", "Balanced liquid refraction", 1.0f),
        INTENSE("Ultra Liquid", "Deep convex meniscus", 1.6f);

        companion object {
            fun fromName(name: String?): GlassRefraction {
                return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: STANDARD
            }
        }
    }

    enum class GlassDispersion(val displayName: String, val subtitle: String, val dispersionFactor: Float) {
        DELICATE("Delicate", "Faint chromatic edge", 0.15f),
        PRISM("Prism ROYGCBP", "Physically-accurate dispersion", 0.35f),
        SPECTRUM("Vibrant Spectrum", "High-contrast iridescent fringe", 0.65f);

        companion object {
            fun fromName(name: String?): GlassDispersion {
                return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: PRISM
            }
        }
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Immutable
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

    private val _glassRefractionFlow = MutableStateFlow(getGlassRefraction())
    val glassRefractionFlow: StateFlow<GlassRefraction> = _glassRefractionFlow.asStateFlow()

    private val _glassDispersionFlow = MutableStateFlow(getGlassDispersion())
    val glassDispersionFlow: StateFlow<GlassDispersion> = _glassDispersionFlow.asStateFlow()

    private val _iosSwitchFlow = MutableStateFlow(isIosSwitchEnabled())
    val iosSwitchFlow: StateFlow<Boolean> = _iosSwitchFlow.asStateFlow()

    private val _iosGlassIntensityFlow = MutableStateFlow(getIosGlassIntensity())
    val iosGlassIntensityFlow: StateFlow<Float> = _iosGlassIntensityFlow.asStateFlow()

    private val _appFontFlow = MutableStateFlow(getAppFont())
    val appFontFlow: StateFlow<AppFont> = _appFontFlow.asStateFlow()

    private val _experimentalFlagsFlow = MutableStateFlow(getExperimentalFlags())
    val experimentalFlagsFlow: StateFlow<ExperimentalFlags> = _experimentalFlagsFlow.asStateFlow()

    private val preferenceChangeListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            KEY_THEME_MODE -> _themeModeFlow.value = getThemeMode()
            KEY_THEME_ACCENT -> _themeAccentFlow.value = getThemeAccent()
            KEY_THEME_GLOW_INTENSITY -> _glowIntensityFlow.value = getGlowIntensity()
            KEY_THEME_CONTRAST_STYLE -> _contrastStyleFlow.value = getContrastStyle()
            KEY_THEME_GLASS_REFRACTION -> _glassRefractionFlow.value = getGlassRefraction()
            KEY_THEME_GLASS_DISPERSION -> _glassDispersionFlow.value = getGlassDispersion()
            KEY_IOS_SWITCH -> _iosSwitchFlow.value = isIosSwitchEnabled()
            KEY_IOS_GLASS_INTENSITY -> _iosGlassIntensityFlow.value = getIosGlassIntensity()
            KEY_APP_FONT -> _appFontFlow.value = getAppFont()
            KEY_EXP_MASTER,
            KEY_EXP_DEAL_AUTHENTICITY,
            KEY_EXP_BUY_NOW_VERDICT,
            KEY_EXP_BANK_CARD,
            KEY_EXP_ALERT_PRESETS,
            KEY_EXP_DYNAMIC_SEARCH,
            KEY_EXP_PRICE_DROP_FORECAST,
            KEY_EXP_COUPON_FINDER -> _experimentalFlagsFlow.value = getExperimentalFlags()
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

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

    fun getGlassRefraction(): GlassRefraction {
        val name = prefs.getString(KEY_THEME_GLASS_REFRACTION, GlassRefraction.STANDARD.name) ?: GlassRefraction.STANDARD.name
        return GlassRefraction.fromName(name)
    }

    fun setGlassRefraction(refraction: GlassRefraction) {
        prefs.edit().putString(KEY_THEME_GLASS_REFRACTION, refraction.name).apply()
        _glassRefractionFlow.value = refraction
    }

    fun getGlassDispersion(): GlassDispersion {
        val name = prefs.getString(KEY_THEME_GLASS_DISPERSION, GlassDispersion.PRISM.name) ?: GlassDispersion.PRISM.name
        return GlassDispersion.fromName(name)
    }

    fun setGlassDispersion(dispersion: GlassDispersion) {
        prefs.edit().putString(KEY_THEME_GLASS_DISPERSION, dispersion.name).apply()
        _glassDispersionFlow.value = dispersion
    }

    fun isIosSwitchEnabled(): Boolean {
        return prefs.getBoolean(KEY_IOS_SWITCH, false)
    }

    fun setIosSwitchEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IOS_SWITCH, enabled).apply()
        _iosSwitchFlow.value = enabled
    }

    fun getIosGlassIntensity(): Float {
        return prefs.getFloat(KEY_IOS_GLASS_INTENSITY, 1.0f)
    }

    fun setIosGlassIntensity(intensity: Float) {
        val clamped = intensity.coerceIn(0.2f, 2.0f)
        prefs.edit().putFloat(KEY_IOS_GLASS_INTENSITY, clamped).apply()
        _iosGlassIntensityFlow.value = clamped
    }

    fun getAppFont(): AppFont {
        val id = prefs.getString(KEY_APP_FONT, AppFont.DEFAULT.id)
        return AppFont.fromId(id)
    }

    fun setAppFont(font: AppFont) {
        prefs.edit().putString(KEY_APP_FONT, font.id).apply()
        _appFontFlow.value = font
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
        _iosSwitchFlow.value = false
        _iosGlassIntensityFlow.value = 1.0f
        _appFontFlow.value = AppFont.DEFAULT
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
        const val KEY_THEME_GLASS_REFRACTION = "key_theme_glass_refraction"
        const val KEY_THEME_GLASS_DISPERSION = "key_theme_glass_dispersion"
        const val KEY_IOS_SWITCH = "key_ios_switch"
        const val KEY_IOS_GLASS_INTENSITY = "key_ios_glass_intensity"
        const val KEY_APP_FONT = "key_app_font"

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
