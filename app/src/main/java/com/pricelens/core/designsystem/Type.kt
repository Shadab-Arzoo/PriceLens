package com.pricelens.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pricelens.R

@OptIn(ExperimentalTextApi::class)
val InterFontFamily = FontFamily(
    Font(
        resId = R.font.inter,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resId = R.font.inter,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = R.font.inter,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        resId = R.font.inter,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    )
)

@OptIn(ExperimentalTextApi::class)
val PlusJakartaFontFamily = FontFamily(
    Font(
        resId = R.font.plus_jakarta_sans,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resId = R.font.plus_jakarta_sans,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = R.font.plus_jakarta_sans,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        resId = R.font.plus_jakarta_sans,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    )
)

/** Metric-compatible Times New Roman typeface family (Regular, Italic, Bold, Bold Italic). */
val TimesNewRomanFontFamily = FontFamily(
    Font(R.font.times_new_roman_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.times_new_roman_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.times_new_roman_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.times_new_roman_bold_italic, FontWeight.Bold, FontStyle.Italic)
)

/** Editorial humanist proportional sans-serif typeface family. */
val EditorialQuattroFontFamily = FontFamily(
    Font(R.font.quattro_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.quattro_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.quattro_bold, FontWeight.Bold, FontStyle.Normal)
)

/** Crisp tabular monospace typeface family for numbers and specs. */
val PrecisionMonoFontFamily = FontFamily(
    Font(R.font.precision_mono_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.precision_mono_bold, FontWeight.Bold, FontStyle.Normal)
)

enum class AppFont(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val samplePreview: String
) {
    DEFAULT("default", "Default Font", "System Android Roboto", "Aa ₹1,999"),
    TIMES_NEW_ROMAN("times_new_roman", "Times New Roman", "Classic Roman serif", "Aa ₹1,999"),
    INTER("inter", "Inter Pro", "Apple & Fintech clean sans", "Aa ₹1,999"),
    PLUS_JAKARTA("plus_jakarta", "Plus Jakarta", "Geometric rounded modern", "Aa ₹1,999"),
    EDITORIAL_QUATTRO("editorial_quattro", "Editorial Quattro", "Refined reading sans", "Aa ₹1,999"),
    SERIF("serif", "System Serif", "Built-in Noto serif", "Aa ₹1,999"),
    MONOSPACE("monospace", "Precision Mono", "Crisp tabular monospace", "Aa ₹1,999");

    fun getFontFamily(): FontFamily = when (this) {
        DEFAULT -> FontFamily.Default
        TIMES_NEW_ROMAN -> TimesNewRomanFontFamily
        INTER -> InterFontFamily
        PLUS_JAKARTA -> PlusJakartaFontFamily
        EDITORIAL_QUATTRO -> EditorialQuattroFontFamily
        SERIF -> FontFamily.Serif
        MONOSPACE -> PrecisionMonoFontFamily
    }

    companion object {
        fun fromId(id: String?): AppFont =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
    }
}

fun createPriceLensTypography(appFont: AppFont = AppFont.DEFAULT): Typography {
    val family = appFont.getFontFamily()
    return Typography(
        displayLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.6).sp
        ),
        displayMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 32.sp,
            letterSpacing = (-0.4).sp
        ),
        displaySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            letterSpacing = (-0.3).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = (-0.3).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.2).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.15).sp
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.1).sp
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        titleSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 18.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 19.sp
        ),
        bodySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 18.sp
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.2.sp
        )
    )
}

val PriceLensTypography = createPriceLensTypography(AppFont.DEFAULT)
