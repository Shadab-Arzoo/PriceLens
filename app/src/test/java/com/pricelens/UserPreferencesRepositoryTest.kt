package com.pricelens

import android.content.Context
import android.content.SharedPreferences
import com.pricelens.core.preferences.UserPreferencesRepository
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
    fun resetAll_clearsAllPreferences() {
        preferencesRepository.hasCompletedOnboarding = true
        preferencesRepository.hasRequestedCameraPermission = true

        preferencesRepository.resetAll()

        assertFalse(preferencesRepository.hasCompletedOnboarding)
        assertFalse(preferencesRepository.hasRequestedCameraPermission)
    }
}
