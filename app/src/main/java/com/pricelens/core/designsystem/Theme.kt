package com.pricelens.core.designsystem

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.pricelens.core.preferences.UserPreferencesRepository

/**
 * Distinct aesthetic theme accents available in PriceLens.
 */
enum class ThemeAccent(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val characterDescription: String,
    val accentColor: Color,
    val lightAccentColor: Color,
    val glowColor: Color,
    val previewGradient: List<Color>
) {
    EMERALD_MINT(
        id = "mint",
        displayName = "Emerald Mint",
        subtitle = "Brand Signature",
        characterDescription = "Crisp, organic fintech green with emerald luminescence",
        accentColor = Color(0xFF6EE3AD),
        lightAccentColor = Color(0xFF059669),
        glowColor = Color(0x336EE3AD),
        previewGradient = listOf(Color(0xFF6EE3AD), Color(0xFF059669))
    ),
    CYBER_CYAN(
        id = "cyan",
        displayName = "Cyber Cyan",
        subtitle = "Electric Neon",
        characterDescription = "High-voltage cyberpunk cyan inspired by futuristic HUDs",
        accentColor = Color(0xFF38BDF8),
        lightAccentColor = Color(0xFF0284C7),
        glowColor = Color(0x3338BDF8),
        previewGradient = listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
    ),
    COSMIC_INDIGO(
        id = "indigo",
        displayName = "Cosmic Indigo",
        subtitle = "Deep Tech Sapphire",
        characterDescription = "Subtle, deep interstellar cobalt and royal tech sapphire",
        accentColor = Color(0xFF818CF8),
        lightAccentColor = Color(0xFF4F46E5),
        glowColor = Color(0x33818CF8),
        previewGradient = listOf(Color(0xFF818CF8), Color(0xFF4F46E5))
    ),
    NEON_VIOLET(
        id = "violet",
        displayName = "Neon Violet",
        subtitle = "Cyber Amethyst",
        characterDescription = "Hypnotic synthwave purple with vibrant ultraviolet highlights",
        accentColor = Color(0xFFA855F7),
        lightAccentColor = Color(0xFF7C3AED),
        glowColor = Color(0x33A855F7),
        previewGradient = listOf(Color(0xFFA855F7), Color(0xFF7C3AED))
    ),
    SUNSET_AMBER(
        id = "amber",
        displayName = "Sunset Amber",
        subtitle = "Solar Flare Gold",
        characterDescription = "Warm golden hour glow with molten solar brilliance",
        accentColor = Color(0xFFFBBF24),
        lightAccentColor = Color(0xFFD97706),
        glowColor = Color(0x33FBBF24),
        previewGradient = listOf(Color(0xFFFBBF24), Color(0xFFD97706))
    ),
    SOLAR_TANGERINE(
        id = "tangerine",
        displayName = "Solar Tangerine",
        subtitle = "High-Voltage Blaze",
        characterDescription = "Energetic supersonic orange engineered for sharp contrast",
        accentColor = Color(0xFFFB923C),
        lightAccentColor = Color(0xFFEA580C),
        glowColor = Color(0x33FB923C),
        previewGradient = listOf(Color(0xFFFB923C), Color(0xFFEA580C))
    ),
    ROSE_CRIMSON(
        id = "rose",
        displayName = "Rose Crimson",
        subtitle = "Velvet Neon",
        characterDescription = "Bold luxury magenta ruby with sleek neon undertones",
        accentColor = Color(0xFFFB7185),
        lightAccentColor = Color(0xFFE11D48),
        glowColor = Color(0x33FB7185),
        previewGradient = listOf(Color(0xFFFB7185), Color(0xFFE11D48))
    ),
    HYPER_LIME(
        id = "lime",
        displayName = "Hyper Lime",
        subtitle = "Electric Citrus",
        characterDescription = "High-visibility chartreuse citrus with electric radiance",
        accentColor = Color(0xFFA3E635),
        lightAccentColor = Color(0xFF65A30D),
        glowColor = Color(0x33A3E635),
        previewGradient = listOf(Color(0xFFA3E635), Color(0xFF65A30D))
    ),
    GLACIER_FROST(
        id = "frost",
        displayName = "Glacier Frost",
        subtitle = "Arctic Sheen",
        characterDescription = "Crystalline polar ice-blue with cool arctic brilliance",
        accentColor = Color(0xFF7DD3FC),
        lightAccentColor = Color(0xFF0284C7),
        glowColor = Color(0x337DD3FC),
        previewGradient = listOf(Color(0xFF7DD3FC), Color(0xFF0284C7))
    ),
    TITANIUM(
        id = "titanium",
        displayName = "Titanium",
        subtitle = "Minimalist Mono",
        characterDescription = "Stealth architectural monochrome with brushed silver tones",
        accentColor = Color(0xFFE2E8F0),
        lightAccentColor = Color(0xFF334155),
        glowColor = Color(0x33E2E8F0),
        previewGradient = listOf(Color(0xFFF8FAFC), Color(0xFF64748B))
    ),
    CUPERTINO_BLUE(
        id = "cupertino",
        displayName = "Cupertino Blue",
        subtitle = "Apple HIG Classic",
        characterDescription = "Iconic iOS System Blue with frosted visionOS vibrancy and crystalline depth",
        accentColor = AppleSystemBlueDark,
        lightAccentColor = AppleSystemBlueLight,
        glowColor = Color(0x330A84FF),
        previewGradient = listOf(AppleSystemBlueDark, AppleSystemIndigoDark)
    ),
    APPLE_SILICON(
        id = "apple_silicon",
        displayName = "Apple Silicon",
        subtitle = "Titanium & Space Gray",
        characterDescription = "Ultra-refined Cupertino dark titanium and space gray with subtle specular sheen",
        accentColor = Color(0xFFF2F2F7),
        lightAccentColor = Color(0xFF1C1C1E),
        glowColor = Color(0x29F2F2F7),
        previewGradient = listOf(Color(0xFFF2F2F7), Color(0xFF8E8E93))
    ),
    CUPERTINO_PURPLE(
        id = "cupertino_purple",
        displayName = "Cupertino Purple",
        subtitle = "visionOS Violet",
        characterDescription = "Hypnotic visionOS spatial purple with radiant ultraviolet rim reflections",
        accentColor = AppleSystemPurpleDark,
        lightAccentColor = AppleSystemPurpleLight,
        glowColor = Color(0x33BF5AF2),
        previewGradient = listOf(AppleSystemPurpleDark, AppleSystemIndigoDark)
    );

    fun primaryColor(dark: Boolean): Color = if (dark) accentColor else lightAccentColor
    fun secondaryColor(dark: Boolean): Color = if (dark) previewGradient.last() else previewGradient.first()

    companion object {
        fun fromId(id: String): ThemeAccent {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: EMERALD_MINT
        }
    }
}

@Immutable
data class GlassColors(
    val backgroundBrush: Brush,
    val surfaceGlass: Color,
    val surfaceHighlight: Color,
    val specularBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val actionFill: Color,
    val actionLabel: Color,
    val specularBorderBrush: Brush = SolidColor(specularBorder),
    val primaryGradientBrush: Brush = SolidColor(actionFill),
    val ambientGlow: Color = BrandMint,
    val accentGradientBrush: Brush = SolidColor(ambientGlow),
    val ambientHaloBrush: Brush = SolidColor(Color.Transparent),
    val badgeBackground: Color = ambientGlow.copy(alpha = 0.14f),
    val badgeBorder: Color = ambientGlow.copy(alpha = 0.35f),
    val accentSecondary: Color = ambientGlow,
    val glowIntensity: UserPreferencesRepository.GlowIntensity = UserPreferencesRepository.GlowIntensity.VIVID,
    val contrastStyle: UserPreferencesRepository.ContrastStyle = UserPreferencesRepository.ContrastStyle.PITCH_BLACK,
    val chromaticBorderBrush: Brush = specularBorderBrush,
    val specularTopGlintBrush: Brush = SolidColor(Color.White),
    val glassRefraction: UserPreferencesRepository.GlassRefraction = UserPreferencesRepository.GlassRefraction.STANDARD,
    val glassDispersion: UserPreferencesRepository.GlassDispersion = UserPreferencesRepository.GlassDispersion.PRISM
)

private val LightGlassColors = GlassColors(
    backgroundBrush = SolidColor(LightBackgroundStart),
    surfaceGlass = LightGlassSurface,
    surfaceHighlight = LightSurfaceVariant,
    specularBorder = LightGlassBorder,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    actionFill = LightActionFill,
    actionLabel = LightActionLabel,
    specularBorderBrush = SolidColor(LightGlassBorder),
    primaryGradientBrush = SolidColor(LightActionFill),
    ambientGlow = Color(0xFF059669)
)

private val DarkGlassColors = GlassColors(
    backgroundBrush = SolidColor(DarkBackgroundStart),
    surfaceGlass = DarkGlassSurface,
    surfaceHighlight = DarkSurfaceVariant,
    specularBorder = DarkGlassBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    actionFill = DarkActionFill,
    actionLabel = DarkActionLabel,
    specularBorderBrush = SolidColor(DarkGlassBorder),
    primaryGradientBrush = SolidColor(DarkActionFill),
    ambientGlow = BrandMint
)

val LocalGlassColors = compositionLocalOf { DarkGlassColors }
val LocalThemeIsDark = staticCompositionLocalOf { true }
val LocalThemeAccent = staticCompositionLocalOf { ThemeAccent.EMERALD_MINT }
val LocalThemeToggle = staticCompositionLocalOf<() -> Unit> { {} }
val LocalUserPreferences = compositionLocalOf<UserPreferencesRepository?> { null }
val LocalIosSwitch = compositionLocalOf { false }
val LocalAppFont = compositionLocalOf { AppFont.DEFAULT }

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0F172A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1F5F9),
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = Color(0xFF475569),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF0F172A),
    background = LightBackgroundStart,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightHairline,
    outlineVariant = LightHairline,
    error = CoralError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandMint,
    onPrimary = Color(0xFF040404),
    primaryContainer = DarkActionFill,
    onPrimaryContainer = DarkActionLabel,
    secondary = DarkTextSecondary,
    onSecondary = Color(0xFF000000),
    secondaryContainer = DarkSurface,
    onSecondaryContainer = DarkTextPrimary,
    background = DarkBackgroundStart,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkHairline,
    outlineVariant = DarkHairline,
    error = CoralError,
    onError = Color.White
)

@Composable
fun PriceLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accent: ThemeAccent = ThemeAccent.EMERALD_MINT,
    glowIntensity: UserPreferencesRepository.GlowIntensity = UserPreferencesRepository.GlowIntensity.VIVID,
    contrastStyle: UserPreferencesRepository.ContrastStyle = UserPreferencesRepository.ContrastStyle.PITCH_BLACK,
    glassRefraction: UserPreferencesRepository.GlassRefraction = UserPreferencesRepository.GlassRefraction.STANDARD,
    glassDispersion: UserPreferencesRepository.GlassDispersion = UserPreferencesRepository.GlassDispersion.PRISM,
    iosSwitchEnabled: Boolean = false,
    appFont: AppFont = AppFont.DEFAULT,
    userPreferences: UserPreferencesRepository? = null,
    onToggleTheme: () -> Unit = {},
    content: @Composable () -> Unit
) {
    // When iosSwitchEnabled is true, apply the isolated iOS-inspired Liquid Glass theme layer
    val activeContrastStyle = if (iosSwitchEnabled) {
        UserPreferencesRepository.ContrastStyle.APPLE_CUPERTINO
    } else {
        contrastStyle
    }

    val activeAccent = if (iosSwitchEnabled) {
        if (accent == ThemeAccent.APPLE_SILICON || accent == ThemeAccent.CUPERTINO_PURPLE) accent else ThemeAccent.CUPERTINO_BLUE
    } else {
        accent
    }

    val activeGlowIntensity = if (iosSwitchEnabled) {
        UserPreferencesRepository.GlowIntensity.SUBTLE
    } else {
        glowIntensity
    }

    val activeGlassRefraction = if (iosSwitchEnabled) {
        UserPreferencesRepository.GlassRefraction.SUBTLE
    } else {
        glassRefraction
    }

    val activeGlassDispersion = if (iosSwitchEnabled) {
        UserPreferencesRepository.GlassDispersion.DELICATE
    } else {
        glassDispersion
    }

    val targetAccent = activeAccent.primaryColor(darkTheme)
    val targetSecondary = activeAccent.secondaryColor(darkTheme)

    val animatedAccent by animateColorAsState(
        targetValue = targetAccent,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "themeAccentAnim"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = targetSecondary,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "themeSecondaryAnim"
    )

    val backgroundBrush = remember(darkTheme, animatedAccent, animatedSecondary, activeGlowIntensity, activeContrastStyle) {
        if (darkTheme) {
            when (activeContrastStyle) {
                UserPreferencesRepository.ContrastStyle.PITCH_BLACK -> {
                    val canopyAlpha = (0.09f * activeGlowIntensity.multiplier).coerceIn(0.04f, 0.22f)
                    val secondaryAlpha = (0.05f * activeGlowIntensity.multiplier).coerceIn(0.02f, 0.14f)
                    Brush.verticalGradient(
                        0.0f to animatedAccent.copy(alpha = canopyAlpha),
                        0.18f to animatedSecondary.copy(alpha = secondaryAlpha),
                        0.48f to DarkBackgroundStart,
                        1.0f to DarkBackgroundEnd
                    )
                }
                UserPreferencesRepository.ContrastStyle.OBSIDIAN_TINT -> {
                    val canopyAlpha = (0.13f * activeGlowIntensity.multiplier).coerceIn(0.06f, 0.28f)
                    val secondaryAlpha = (0.08f * activeGlowIntensity.multiplier).coerceIn(0.03f, 0.18f)
                    Brush.verticalGradient(
                        0.0f to animatedAccent.copy(alpha = canopyAlpha),
                        0.22f to animatedSecondary.copy(alpha = secondaryAlpha),
                        0.52f to Color(0xFF070C16),
                        1.0f to Color(0xFF030508)
                    )
                }
                UserPreferencesRepository.ContrastStyle.APPLE_CUPERTINO -> {
                    Brush.verticalGradient(
                        0.0f to AppleGroupedBackgroundDark,
                        1.0f to AppleGroupedBackgroundDark
                    )
                }
            }
        } else {
            if (activeContrastStyle == UserPreferencesRepository.ContrastStyle.APPLE_CUPERTINO) {
                Brush.verticalGradient(
                    0.0f to AppleGroupedBackgroundLight,
                    1.0f to AppleGroupedBackgroundLight
                )
            } else {
                val canopyAlpha = (0.08f * activeGlowIntensity.multiplier).coerceIn(0.03f, 0.18f)
                val secondaryAlpha = (0.04f * activeGlowIntensity.multiplier).coerceIn(0.015f, 0.12f)
                Brush.verticalGradient(
                    0.0f to animatedAccent.copy(alpha = canopyAlpha),
                    0.20f to animatedSecondary.copy(alpha = secondaryAlpha),
                    0.55f to LightBackgroundStart,
                    1.0f to LightBackgroundEnd
                )
            }
        }
    }

    val specularBorderBrush = remember(darkTheme, animatedAccent, animatedSecondary, activeGlowIntensity, activeContrastStyle) {
        if (activeContrastStyle == UserPreferencesRepository.ContrastStyle.APPLE_CUPERTINO) {
            if (darkTheme) {
                Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = 0.38f),
                    0.20f to animatedAccent.copy(alpha = 0.22f),
                    0.60f to AppleGlassBorderDark,
                    1.0f to Color(0x18FFFFFF)
                )
            } else {
                Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = 0.85f),
                    0.25f to animatedAccent.copy(alpha = 0.18f),
                    0.70f to AppleGlassBorderLight,
                    1.0f to Color(0x10000000)
                )
            }
        } else {
            val topAlpha = (if (darkTheme) 0.65f else 0.80f) * activeGlowIntensity.multiplier
            val dispersionAlpha = (if (darkTheme) 0.45f else 0.50f) * activeGlowIntensity.multiplier
            val baseHairline = if (darkTheme) DarkHairline else LightHairline
            val dispersionColor = if (darkTheme) DarkGlassDispersionCyan else LightGlassDispersionCyan
            Brush.verticalGradient(
                0.0f to Color.White.copy(alpha = topAlpha.coerceIn(0.35f, 0.95f)),
                0.22f to animatedAccent.copy(alpha = dispersionAlpha.coerceIn(0.20f, 0.70f)),
                0.45f to dispersionColor.copy(alpha = dispersionAlpha.coerceIn(0.15f, 0.60f)),
                0.75f to baseHairline,
                1.0f to (if (darkTheme) DarkGlassSpecularBottom else LightGlassSpecularBottom)
            )
        }
    }

    val chromaticBorderBrush = remember(darkTheme, animatedAccent, animatedSecondary, activeGlowIntensity, activeContrastStyle) {
        if (activeContrastStyle == UserPreferencesRepository.ContrastStyle.APPLE_CUPERTINO) {
            SolidColor(if (darkTheme) AppleGlassBorderDark else AppleGlassBorderLight)
        } else {
            val topAlpha = (if (darkTheme) 0.70f else 0.85f) * activeGlowIntensity.multiplier
            val accentAlpha = (if (darkTheme) 0.50f else 0.55f) * activeGlowIntensity.multiplier
            Brush.sweepGradient(
                0.0f to Color.White.copy(alpha = topAlpha.coerceIn(0.35f, 0.95f)),
                0.20f to animatedAccent.copy(alpha = accentAlpha.coerceIn(0.20f, 0.75f)),
                0.40f to (if (darkTheme) DarkGlassDispersionCyan else LightGlassDispersionCyan),
                0.65f to (if (darkTheme) DarkHairline else LightHairline),
                0.85f to animatedSecondary.copy(alpha = accentAlpha.coerceIn(0.15f, 0.65f)),
                1.0f to Color.White.copy(alpha = topAlpha.coerceIn(0.35f, 0.95f))
            )
        }
    }

    val specularTopGlintBrush = remember(darkTheme, animatedAccent, activeContrastStyle) {
        if (activeContrastStyle == UserPreferencesRepository.ContrastStyle.APPLE_CUPERTINO) {
            Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = if (darkTheme) 0.15f else 0.35f),
                    Color.Transparent
                )
            )
        } else {
            Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = if (darkTheme) 0.45f else 0.75f),
                    animatedAccent.copy(alpha = if (darkTheme) 0.40f else 0.55f),
                    Color.White.copy(alpha = if (darkTheme) 0.45f else 0.75f),
                    Color.Transparent
                )
            )
        }
    }

    val accentGradientBrush = remember(animatedAccent, animatedSecondary) {
        Brush.horizontalGradient(
            listOf(animatedAccent, animatedSecondary)
        )
    }

    val primaryGradientBrush = remember(darkTheme, animatedAccent, activeContrastStyle) {
        if (activeContrastStyle == UserPreferencesRepository.ContrastStyle.APPLE_CUPERTINO) {
            Brush.verticalGradient(
                listOf(
                    animatedAccent,
                    animatedAccent.copy(alpha = 0.92f)
                )
            )
        } else if (darkTheme) {
            Brush.verticalGradient(
                listOf(
                    DarkActionFill,
                    DarkActionFill.copy(alpha = 0.90f),
                    DarkActionFill
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    LightActionFill,
                    LightActionFill.copy(alpha = 0.92f),
                    LightActionFill
                )
            )
        }
    }

    val ambientHaloBrush = remember(animatedAccent, activeGlowIntensity) {
        Brush.radialGradient(
            colors = listOf(
                animatedAccent.copy(alpha = (0.24f * activeGlowIntensity.multiplier).coerceIn(0.08f, 0.55f)),
                Color.Transparent
            ),
            radius = 600f * activeGlowIntensity.multiplier
        )
    }

    val baseColors = if (darkTheme) DarkGlassColors else LightGlassColors
    val isCupertino = activeContrastStyle == UserPreferencesRepository.ContrastStyle.APPLE_CUPERTINO || iosSwitchEnabled
    val glassColors = baseColors.copy(
        backgroundBrush = backgroundBrush,
        surfaceGlass = if (isCupertino) {
            if (darkTheme) AppleGlassSurfaceDark else AppleGlassSurfaceLight
        } else {
            if (darkTheme) DarkGlassSurface else LightGlassSurface
        },
        surfaceHighlight = if (isCupertino) {
            if (darkTheme) AppleSecondaryGroupedDark else AppleSecondaryGroupedLight
        } else {
            if (darkTheme) DarkSurfaceVariant else LightSurfaceVariant
        },
        specularBorder = if (isCupertino) {
            if (darkTheme) AppleGlassBorderDark else AppleGlassBorderLight
        } else {
            if (darkTheme) DarkGlassBorder else LightGlassBorder
        },
        actionFill = if (isCupertino) animatedAccent else (if (darkTheme) DarkActionFill else LightActionFill),
        actionLabel = if (isCupertino) Color.White else (if (darkTheme) DarkActionLabel else LightActionLabel),
        ambientGlow = animatedAccent,
        specularBorderBrush = specularBorderBrush,
        chromaticBorderBrush = chromaticBorderBrush,
        specularTopGlintBrush = specularTopGlintBrush,
        primaryGradientBrush = primaryGradientBrush,
        accentGradientBrush = accentGradientBrush,
        ambientHaloBrush = ambientHaloBrush,
        badgeBackground = if (isCupertino) Color(0xFFFF3B30) else animatedAccent.copy(alpha = ((if (darkTheme) 0.18f else 0.15f) * activeGlowIntensity.multiplier).coerceIn(0.08f, 0.40f)),
        badgeBorder = if (isCupertino) Color.Transparent else animatedAccent.copy(alpha = ((if (darkTheme) 0.42f else 0.32f) * activeGlowIntensity.multiplier).coerceIn(0.18f, 0.75f)),
        accentSecondary = animatedSecondary,
        glowIntensity = activeGlowIntensity,
        contrastStyle = activeContrastStyle,
        glassRefraction = activeGlassRefraction,
        glassDispersion = activeGlassDispersion
    )

    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val colorScheme = baseScheme.copy(
        primary = animatedAccent,
        primaryContainer = if (darkTheme) DarkActionFill else LightActionFill
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val effectiveFont = if (iosSwitchEnabled && appFont == AppFont.DEFAULT) {
        AppFont.INTER
    } else {
        appFont
    }
    val typography = remember(effectiveFont) {
        createPriceLensTypography(effectiveFont)
    }

    CompositionLocalProvider(
        LocalGlassColors provides glassColors,
        LocalThemeIsDark provides darkTheme,
        LocalThemeAccent provides activeAccent,
        LocalIosSwitch provides iosSwitchEnabled,
        LocalAppFont provides effectiveFont,
        LocalUserPreferences provides userPreferences,
        LocalThemeToggle provides onToggleTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

object PriceLensTheme {
    val glassColors: GlassColors
        @Composable
        get() = LocalGlassColors.current

    val isDark: Boolean
        @Composable
        get() = LocalThemeIsDark.current

    val isIosSwitch: Boolean
        @Composable
        get() = LocalIosSwitch.current

    val font: AppFont
        @Composable
        get() = LocalAppFont.current

    val isCupertino: Boolean
        @Composable
        get() = LocalGlassColors.current.contrastStyle == UserPreferencesRepository.ContrastStyle.APPLE_CUPERTINO || LocalIosSwitch.current

    val accent: ThemeAccent
        @Composable
        get() = LocalThemeAccent.current

    val glowIntensity: UserPreferencesRepository.GlowIntensity
        @Composable
        get() = LocalGlassColors.current.glowIntensity

    val contrastStyle: UserPreferencesRepository.ContrastStyle
        @Composable
        get() = LocalGlassColors.current.contrastStyle

    val accentColor: Color
        @Composable
        get() = LocalGlassColors.current.ambientGlow

    val accentGradient: Brush
        @Composable
        get() = LocalGlassColors.current.accentGradientBrush

    val badgeBackground: Color
        @Composable
        get() = LocalGlassColors.current.badgeBackground

    val badgeBorder: Color
        @Composable
        get() = LocalGlassColors.current.badgeBorder

    val chromaticBorder: Brush
        @Composable
        get() = LocalGlassColors.current.chromaticBorderBrush

    val specularTopGlint: Brush
        @Composable
        get() = LocalGlassColors.current.specularTopGlintBrush

    val glassRefraction: UserPreferencesRepository.GlassRefraction
        @Composable
        get() = LocalGlassColors.current.glassRefraction

    val glassDispersion: UserPreferencesRepository.GlassDispersion
        @Composable
        get() = LocalGlassColors.current.glassDispersion
}
