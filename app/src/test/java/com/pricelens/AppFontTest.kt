package com.pricelens

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.text.font.FontFamily
import com.pricelens.core.designsystem.AppFont
import com.pricelens.core.designsystem.createPriceLensTypography
import com.pricelens.core.preferences.UserPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class AppFontTest {

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
        val testContext = TestContext(fakePrefs)
        preferencesRepository = UserPreferencesRepository(testContext)
    }

    @Test
    fun appFont_defaultsToSystemDefault() {
        assertEquals(AppFont.DEFAULT, preferencesRepository.getAppFont())
        assertEquals(AppFont.DEFAULT, preferencesRepository.appFontFlow.value)
    }

    @Test
    fun appFont_persistsSelectedOption_includingTimesNewRoman() {
        preferencesRepository.setAppFont(AppFont.TIMES_NEW_ROMAN)
        assertEquals(AppFont.TIMES_NEW_ROMAN, preferencesRepository.getAppFont())
        assertEquals(AppFont.TIMES_NEW_ROMAN, preferencesRepository.appFontFlow.value)

        preferencesRepository.setAppFont(AppFont.INTER)
        assertEquals(AppFont.INTER, preferencesRepository.getAppFont())
        assertEquals(AppFont.INTER, preferencesRepository.appFontFlow.value)

        preferencesRepository.setAppFont(AppFont.PLUS_JAKARTA)
        assertEquals(AppFont.PLUS_JAKARTA, preferencesRepository.getAppFont())

        preferencesRepository.setAppFont(AppFont.EDITORIAL_QUATTRO)
        assertEquals(AppFont.EDITORIAL_QUATTRO, preferencesRepository.getAppFont())

        preferencesRepository.setAppFont(AppFont.SERIF)
        assertEquals(AppFont.SERIF, preferencesRepository.getAppFont())

        preferencesRepository.setAppFont(AppFont.MONOSPACE)
        assertEquals(AppFont.MONOSPACE, preferencesRepository.getAppFont())

        // Reset restores Default Font
        preferencesRepository.resetAll()
        assertEquals(AppFont.DEFAULT, preferencesRepository.getAppFont())
        assertEquals(AppFont.DEFAULT, preferencesRepository.appFontFlow.value)
    }

    @Test
    fun appFont_fromId_handlesAllOptionsAndUnknown() {
        assertEquals(AppFont.TIMES_NEW_ROMAN, AppFont.fromId("times_new_roman"))
        assertEquals(AppFont.TIMES_NEW_ROMAN, AppFont.fromId("TIMES_NEW_ROMAN"))
        assertEquals(AppFont.INTER, AppFont.fromId("inter"))
        assertEquals(AppFont.PLUS_JAKARTA, AppFont.fromId("plus_jakarta"))
        assertEquals(AppFont.EDITORIAL_QUATTRO, AppFont.fromId("editorial_quattro"))
        assertEquals(AppFont.SERIF, AppFont.fromId("serif"))
        assertEquals(AppFont.MONOSPACE, AppFont.fromId("monospace"))
        assertEquals(AppFont.DEFAULT, AppFont.fromId("unknown_font_id"))
        assertEquals(AppFont.DEFAULT, AppFont.fromId(null))
    }

    @Test
    fun appFont_familyAndTypographySlotsResolveCorrectly() {
        assertEquals(FontFamily.Default, AppFont.DEFAULT.getFontFamily())
        assertEquals(FontFamily.Serif, AppFont.SERIF.getFontFamily())
        assertNotNull(AppFont.TIMES_NEW_ROMAN.getFontFamily())
        assertNotNull(AppFont.INTER.getFontFamily())
        assertNotNull(AppFont.PLUS_JAKARTA.getFontFamily())
        assertNotNull(AppFont.EDITORIAL_QUATTRO.getFontFamily())
        assertNotNull(AppFont.MONOSPACE.getFontFamily())

        val romanType = createPriceLensTypography(AppFont.TIMES_NEW_ROMAN)
        val expectedFamily = AppFont.TIMES_NEW_ROMAN.getFontFamily()
        assertEquals(expectedFamily, romanType.displayLarge.fontFamily)
        assertEquals(expectedFamily, romanType.displaySmall.fontFamily)
        assertEquals(expectedFamily, romanType.headlineSmall.fontFamily)
        assertEquals(expectedFamily, romanType.bodyLarge.fontFamily)
        assertEquals(expectedFamily, romanType.labelSmall.fontFamily)
    }
}
