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

class IosSwitchThemeTest {

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
    fun iosSwitch_initialState_isOff() {
        assertFalse("iOS Switch must be OFF by default", preferencesRepository.isIosSwitchEnabled())
        assertFalse("iOS Switch Flow must emit false initially", preferencesRepository.iosSwitchFlow.value)
    }

    @Test
    fun iosSwitch_toggleOn_persistsStateAndEmitsUpdate() {
        preferencesRepository.setIosSwitchEnabled(true)
        assertTrue("iOS Switch must be ON after setting true", preferencesRepository.isIosSwitchEnabled())
        assertTrue("iOS Switch Flow must emit true", preferencesRepository.iosSwitchFlow.value)
    }

    @Test
    fun iosSwitch_toggleOff_retainsOriginalThemeSettings() {
        // Configure custom original theme preferences
        preferencesRepository.setThemeAccent(ThemeAccent.CYBER_CYAN)
        preferencesRepository.setContrastStyle(UserPreferencesRepository.ContrastStyle.OBSIDIAN_TINT)
        preferencesRepository.setGlowIntensity(UserPreferencesRepository.GlowIntensity.ULTRA)

        // Turn iOS Switch ON
        preferencesRepository.setIosSwitchEnabled(true)
        assertTrue(preferencesRepository.isIosSwitchEnabled())

        // Turn iOS Switch OFF
        preferencesRepository.setIosSwitchEnabled(false)
        assertFalse(preferencesRepository.isIosSwitchEnabled())

        // Ensure original theme preferences remain intact
        assertEquals(ThemeAccent.CYBER_CYAN, preferencesRepository.getThemeAccent())
        assertEquals(UserPreferencesRepository.ContrastStyle.OBSIDIAN_TINT, preferencesRepository.getContrastStyle())
        assertEquals(UserPreferencesRepository.GlowIntensity.ULTRA, preferencesRepository.getGlowIntensity())
    }

    @Test
    fun iosSwitch_persistsAcrossReinitialization() {
        preferencesRepository.setIosSwitchEnabled(true)

        // Simulate closing and reopening app by initializing a new repository with the same SharedPreferences backing
        val reloadedRepository = UserPreferencesRepository(TestContext(fakePrefs))
        assertTrue("Setting must persist after closing and reopening the app", reloadedRepository.isIosSwitchEnabled())
        assertTrue(reloadedRepository.iosSwitchFlow.value)
    }
}
