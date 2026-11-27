package com.pricelens

import android.content.Context
import android.content.SharedPreferences
import com.pricelens.core.designsystem.ThemeAccent
import com.pricelens.core.preferences.UserPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserPreferencesRepositoryTest {

    private class InMemorySharedPreferences : SharedPreferences {
        private val map = mutableMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = map.toMutableMap()
        override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
            @Suppress("UNCHECKED_CAST") (map[key] as? MutableSet<String> ?: defValues)
        override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
        override fun contains(key: String?): Boolean = map.containsKey(key)
        override fun edit(): SharedPreferences.Editor = InMemoryEditor(map)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    }

    private class InMemoryEditor(private val map: MutableMap<String, Any?>) : SharedPreferences.Editor {
        private val staging = mutableMapOf<String, Any?>()
        private var clear = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor { staging[key ?: ""] = value; return this }
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor { staging[key ?: ""] = values; return this }
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor { staging[key ?: ""] = value; return this }
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor { staging[key ?: ""] = value; return this }
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor { staging[key ?: ""] = value; return this }
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor { staging[key ?: ""] = value; return this }
        override fun remove(key: String?): SharedPreferences.Editor { staging[key ?: ""] = null; return this }
        override fun clear(): SharedPreferences.Editor { clear = true; return this }
        override fun commit(): Boolean { apply(); return true }
        override fun apply() {
            if (clear) map.clear()
            staging.forEach { (k, v) -> if (v == null) map.remove(k) else map[k] = v }
        }
    }

    private class TestContext(private val sharedPreferences: SharedPreferences) : android.content.ContextWrapper(null) {
        override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences = sharedPreferences
        override fun getApplicationContext(): Context = this
    }

    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var fakePrefs: InMemorySharedPreferences

    @Before
    fun setUp() {
        fakePrefs = InMemorySharedPreferences()
        preferencesRepository = UserPreferencesRepository(TestContext(fakePrefs))
    }

    @Test
    fun onboardingCompleted_defaultIsFalse() {
        assertFalse(preferencesRepository.hasCompletedOnboarding)
    }

    @Test
    fun setOnboardingCompleted_persistsTrue() {
        preferencesRepository.hasCompletedOnboarding = true
        assertTrue(preferencesRepository.hasCompletedOnboarding)
    }

    @Test
    fun permissionsRequested_defaultIsFalse_andCanBeUpdated() {
        assertFalse(preferencesRepository.hasRequestedCameraPermission)
        assertFalse(preferencesRepository.hasRequestedNotificationPermission)

        preferencesRepository.hasRequestedCameraPermission = true
        preferencesRepository.hasRequestedNotificationPermission = true

        assertTrue(preferencesRepository.hasRequestedCameraPermission)
        assertTrue(preferencesRepository.hasRequestedNotificationPermission)
    }

    @Test
    fun themeMode_defaultIsDark_andCanToggle() {
        assertEquals(UserPreferencesRepository.ThemeMode.DARK, preferencesRepository.getThemeMode())

        preferencesRepository.toggleTheme(currentIsDark = true)
        assertEquals(UserPreferencesRepository.ThemeMode.LIGHT, preferencesRepository.getThemeMode())

        preferencesRepository.toggleTheme(currentIsDark = false)
        assertEquals(UserPreferencesRepository.ThemeMode.DARK, preferencesRepository.getThemeMode())
    }

    @Test
    fun themeAccent_defaultIsEmeraldMint_andCanUpdate() {
        assertEquals(ThemeAccent.EMERALD_MINT, preferencesRepository.getThemeAccent())

        preferencesRepository.setThemeAccent(ThemeAccent.CYBER_CYAN)
        assertEquals(ThemeAccent.CYBER_CYAN, preferencesRepository.getThemeAccent())

        preferencesRepository.setThemeAccent(ThemeAccent.COSMIC_INDIGO)
        assertEquals(ThemeAccent.COSMIC_INDIGO, preferencesRepository.getThemeAccent())

        preferencesRepository.setThemeAccent(ThemeAccent.NEON_VIOLET)
        assertEquals(ThemeAccent.NEON_VIOLET, preferencesRepository.getThemeAccent())

        preferencesRepository.setThemeAccent(ThemeAccent.SUNSET_AMBER)
        assertEquals(ThemeAccent.SUNSET_AMBER, preferencesRepository.getThemeAccent())

        preferencesRepository.setThemeAccent(ThemeAccent.SOLAR_TANGERINE)
        assertEquals(ThemeAccent.SOLAR_TANGERINE, preferencesRepository.getThemeAccent())

        preferencesRepository.setThemeAccent(ThemeAccent.ROSE_CRIMSON)
        assertEquals(ThemeAccent.ROSE_CRIMSON, preferencesRepository.getThemeAccent())

        preferencesRepository.setThemeAccent(ThemeAccent.HYPER_LIME)
        assertEquals(ThemeAccent.HYPER_LIME, preferencesRepository.getThemeAccent())

        preferencesRepository.setThemeAccent(ThemeAccent.GLACIER_FROST)
        assertEquals(ThemeAccent.GLACIER_FROST, preferencesRepository.getThemeAccent())

        preferencesRepository.setThemeAccent(ThemeAccent.TITANIUM)
        assertEquals(ThemeAccent.TITANIUM, preferencesRepository.getThemeAccent())

        preferencesRepository.setThemeAccent(ThemeAccent.CUPERTINO_BLUE)
        assertEquals(ThemeAccent.CUPERTINO_BLUE, preferencesRepository.getThemeAccent())

        preferencesRepository.setThemeAccent(ThemeAccent.APPLE_SILICON)
        assertEquals(ThemeAccent.APPLE_SILICON, preferencesRepository.getThemeAccent())

        preferencesRepository.setThemeAccent(ThemeAccent.CUPERTINO_PURPLE)
        assertEquals(ThemeAccent.CUPERTINO_PURPLE, preferencesRepository.getThemeAccent())

        assertEquals(13, ThemeAccent.entries.size)
    }

    @Test
    fun glowIntensity_defaultIsVivid_andCanUpdate() {
        assertEquals(UserPreferencesRepository.GlowIntensity.VIVID, preferencesRepository.getGlowIntensity())
        assertEquals(UserPreferencesRepository.GlowIntensity.VIVID, preferencesRepository.glowIntensityFlow.value)

        preferencesRepository.setGlowIntensity(UserPreferencesRepository.GlowIntensity.SUBTLE)
        assertEquals(UserPreferencesRepository.GlowIntensity.SUBTLE, preferencesRepository.getGlowIntensity())
        assertEquals(UserPreferencesRepository.GlowIntensity.SUBTLE, preferencesRepository.glowIntensityFlow.value)

        preferencesRepository.setGlowIntensity(UserPreferencesRepository.GlowIntensity.ULTRA)
        assertEquals(UserPreferencesRepository.GlowIntensity.ULTRA, preferencesRepository.getGlowIntensity())
        assertEquals(UserPreferencesRepository.GlowIntensity.ULTRA, preferencesRepository.glowIntensityFlow.value)
    }

    @Test
    fun contrastStyle_defaultIsPitchBlack_andCanUpdate() {
        assertEquals(UserPreferencesRepository.ContrastStyle.PITCH_BLACK, preferencesRepository.getContrastStyle())
        assertEquals(UserPreferencesRepository.ContrastStyle.PITCH_BLACK, preferencesRepository.contrastStyleFlow.value)

        preferencesRepository.setContrastStyle(UserPreferencesRepository.ContrastStyle.OBSIDIAN_TINT)
        assertEquals(UserPreferencesRepository.ContrastStyle.OBSIDIAN_TINT, preferencesRepository.getContrastStyle())
        assertEquals(UserPreferencesRepository.ContrastStyle.OBSIDIAN_TINT, preferencesRepository.contrastStyleFlow.value)

        preferencesRepository.setContrastStyle(UserPreferencesRepository.ContrastStyle.APPLE_CUPERTINO)
        assertEquals(UserPreferencesRepository.ContrastStyle.APPLE_CUPERTINO, preferencesRepository.getContrastStyle())
        assertEquals(UserPreferencesRepository.ContrastStyle.APPLE_CUPERTINO, preferencesRepository.contrastStyleFlow.value)

        preferencesRepository.setContrastStyle(UserPreferencesRepository.ContrastStyle.PITCH_BLACK)
        assertEquals(UserPreferencesRepository.ContrastStyle.PITCH_BLACK, preferencesRepository.getContrastStyle())
        assertEquals(UserPreferencesRepository.ContrastStyle.PITCH_BLACK, preferencesRepository.contrastStyleFlow.value)
    }

    @Test
    fun resetAll_clearsAllPreferences() {
        preferencesRepository.hasCompletedOnboarding = true
        preferencesRepository.hasRequestedCameraPermission = true
        preferencesRepository.setGlowIntensity(UserPreferencesRepository.GlowIntensity.ULTRA)
        preferencesRepository.setContrastStyle(UserPreferencesRepository.ContrastStyle.OBSIDIAN_TINT)
        preferencesRepository.setGlassRefraction(UserPreferencesRepository.GlassRefraction.INTENSE)
        preferencesRepository.setGlassDispersion(UserPreferencesRepository.GlassDispersion.SPECTRUM)
        preferencesRepository.setExperimentalMasterEnabled(false)
        preferencesRepository.setThemeAccent(ThemeAccent.GLACIER_FROST)

        preferencesRepository.resetAll()

        assertFalse(preferencesRepository.hasCompletedOnboarding)
        assertFalse(preferencesRepository.hasRequestedCameraPermission)
        assertEquals(UserPreferencesRepository.GlowIntensity.VIVID, preferencesRepository.getGlowIntensity())
        assertEquals(UserPreferencesRepository.ContrastStyle.PITCH_BLACK, preferencesRepository.getContrastStyle())
        assertEquals(UserPreferencesRepository.GlassRefraction.STANDARD, preferencesRepository.glassRefractionFlow.value)
        assertEquals(UserPreferencesRepository.GlassDispersion.PRISM, preferencesRepository.glassDispersionFlow.value)
        assertTrue(preferencesRepository.experimentalFlagsFlow.value.isMasterEnabled)
        assertEquals(ThemeAccent.EMERALD_MINT, preferencesRepository.getThemeAccent())
    }

    @Test
    fun experimentalFlags_defaultAndTogglesWorkCorrectly() {
        val initialFlags = preferencesRepository.getExperimentalFlags()
        assertTrue(initialFlags.isMasterEnabled)
        assertTrue(initialFlags.enableDealAuthenticity)
        assertTrue(initialFlags.enableBuyNowVerdict)
        assertTrue(initialFlags.enableBankCardCalculator)
        assertTrue(initialFlags.enableAlertPresets)
        assertTrue(initialFlags.enableDynamicSearch)

        // Toggle master flag off
        preferencesRepository.setExperimentalMasterEnabled(false)
        assertFalse(preferencesRepository.getExperimentalFlags().isMasterEnabled)
        assertFalse(preferencesRepository.experimentalFlagsFlow.value.isMasterEnabled)

        // Toggle specific feature flag off
        preferencesRepository.setExperimentalFlag(UserPreferencesRepository.KEY_EXP_BANK_CARD, false)
        assertFalse(preferencesRepository.getExperimentalFlags().enableBankCardCalculator)
        assertFalse(preferencesRepository.experimentalFlagsFlow.value.enableBankCardCalculator)
    }

    @Test
    fun iosSwitch_defaultIsFalse_andCanUpdateAndPersist() {
        assertFalse(preferencesRepository.isIosSwitchEnabled())
        assertFalse(preferencesRepository.iosSwitchFlow.value)

        preferencesRepository.setIosSwitchEnabled(true)
        assertTrue(preferencesRepository.isIosSwitchEnabled())
        assertTrue(preferencesRepository.iosSwitchFlow.value)

        preferencesRepository.setIosSwitchEnabled(false)
        assertFalse(preferencesRepository.isIosSwitchEnabled())
        assertFalse(preferencesRepository.iosSwitchFlow.value)

        // Test resetAll resets iOS Switch to false
        preferencesRepository.setIosSwitchEnabled(true)
        assertTrue(preferencesRepository.isIosSwitchEnabled())
        preferencesRepository.resetAll()
        assertFalse(preferencesRepository.isIosSwitchEnabled())
        assertFalse(preferencesRepository.iosSwitchFlow.value)
    }

    @Test
    fun iosGlassIntensity_defaultIsOne_andClampsBetweenPointTwoAndTwo() {
        assertEquals(1.0f, preferencesRepository.getIosGlassIntensity(), 0.001f)
        assertEquals(1.0f, preferencesRepository.iosGlassIntensityFlow.value, 0.001f)

        // Set valid value
        preferencesRepository.setIosGlassIntensity(1.5f)
        assertEquals(1.5f, preferencesRepository.getIosGlassIntensity(), 0.001f)
        assertEquals(1.5f, preferencesRepository.iosGlassIntensityFlow.value, 0.001f)

        // Set value below minimum (0.2f) -> clamps to 0.2f
        preferencesRepository.setIosGlassIntensity(0.05f)
        assertEquals(0.2f, preferencesRepository.getIosGlassIntensity(), 0.001f)
        assertEquals(0.2f, preferencesRepository.iosGlassIntensityFlow.value, 0.001f)

        // Set value above maximum (2.0f) -> clamps to 2.0f
        preferencesRepository.setIosGlassIntensity(3.5f)
        assertEquals(2.0f, preferencesRepository.getIosGlassIntensity(), 0.001f)
        assertEquals(2.0f, preferencesRepository.iosGlassIntensityFlow.value, 0.001f)

        // ResetAll resets to 1.0f
        preferencesRepository.resetAll()
        assertEquals(1.0f, preferencesRepository.getIosGlassIntensity(), 0.001f)
        assertEquals(1.0f, preferencesRepository.iosGlassIntensityFlow.value, 0.001f)
    }
}
