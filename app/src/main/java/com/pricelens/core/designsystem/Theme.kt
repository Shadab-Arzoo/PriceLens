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
    val contrastStyle: UserPreferencesRepository.ContrastStyle = UserPreferencesRepository.ContrastStyle.PITCH_BLACK
)

private val LightGlassColors = GlassColors(
    backgroundBrush = SolidColor(LightBackgroundStart),
    surfaceGlass = LightSurface,
    surfaceHighlight = LightSurfaceVariant,
    specularBorder = LightHairline,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    actionFill = LightActionFill,
    actionLabel = LightActionLabel,
    specularBorderBrush = SolidColor(LightHairline),
    primaryGradientBrush = SolidColor(LightActionFill),
    ambientGlow = Color(0xFF059669)
)

private val DarkGlassColors = GlassColors(
    backgroundBrush = SolidColor(DarkBackgroundStart),
    surfaceGlass = DarkSurface,
    surfaceHighlight = DarkSurfaceVariant,
    specularBorder = DarkHairline,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    actionFill = DarkActionFill,
    actionLabel = DarkActionLabel,
    specularBorderBrush = SolidColor(DarkHairline),
    primaryGradientBrush = SolidColor(DarkActionFill),
    ambientGlow = BrandMint
)

val LocalGlassColors = compositionLocalOf { DarkGlassColors }
val LocalThemeIsDark = staticCompositionLocalOf { true }
val LocalThemeAccent = staticCompositionLocalOf { ThemeAccent.EMERALD_MINT }
val LocalThemeToggle = staticCompositionLocalOf<() -> Unit> { {} }
val LocalUserPreferences = compositionLocalOf<UserPreferencesRepository?> { null }

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
    userPreferences: UserPreferencesRepository? = null,
    onToggleTheme: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val targetAccent = accent.primaryColor(darkTheme)
    val targetSecondary = accent.secondaryColor(darkTheme)

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

    val backgroundBrush = remember(darkTheme, animatedAccent, glowIntensity, contrastStyle) {
        if (darkTheme) {
            when (contrastStyle) {
                UserPreferencesRepository.ContrastStyle.PITCH_BLACK -> {
                    val canopyAlpha = (0.048f * glowIntensity.multiplier).coerceIn(0.012f, 0.12f)
                    Brush.verticalGradient(
                        0.0f to animatedAccent.copy(alpha = canopyAlpha),
                        0.22f to Color(0xFF000000),
                        1.0f to Color(0xFF000000)
                    )
                }
                UserPreferencesRepository.ContrastStyle.OBSIDIAN_TINT -> {
                    val canopyAlpha = (0.082f * glowIntensity.multiplier).coerceIn(0.025f, 0.18f)
                    Brush.verticalGradient(
                        0.0f to animatedAccent.copy(alpha = canopyAlpha),
                        0.28f to Color(0xFF090D16),
                        1.0f to Color(0xFF030508)
                    )
                }
            }
        } else {
            val canopyAlpha = (0.045f * glowIntensity.multiplier).coerceIn(0.015f, 0.10f)
            Brush.verticalGradient(
                0.0f to animatedAccent.copy(alpha = canopyAlpha),
                0.22f to LightBackgroundStart,
                1.0f to LightBackgroundEnd
            )
        }
    }

    val specularBorderBrush = remember(darkTheme, animatedAccent, glowIntensity) {
        val baseBorder = if (darkTheme) DarkHairline else LightHairline
        val topAlpha = (if (darkTheme) 0.28f else 0.22f) * glowIntensity.multiplier
        Brush.verticalGradient(
            0.0f to animatedAccent.copy(alpha = topAlpha.coerceIn(0.06f, 0.65f)),
            0.45f to baseBorder,
            1.0f to baseBorder
        )
    }

    val accentGradientBrush = remember(animatedAccent, animatedSecondary) {
        Brush.horizontalGradient(
            listOf(animatedAccent, animatedSecondary)
        )
    }

    val ambientHaloBrush = remember(animatedAccent, glowIntensity) {
        Brush.radialGradient(
            colors = listOf(
                animatedAccent.copy(alpha = (0.18f * glowIntensity.multiplier).coerceIn(0.04f, 0.45f)),
                Color.Transparent
            ),
            radius = 500f * glowIntensity.multiplier
        )
    }

    val baseColors = if (darkTheme) DarkGlassColors else LightGlassColors
    val glassColors = baseColors.copy(
        backgroundBrush = backgroundBrush,
        ambientGlow = animatedAccent,
        specularBorderBrush = specularBorderBrush,
        primaryGradientBrush = if (darkTheme) SolidColor(DarkActionFill) else SolidColor(LightActionFill),
        accentGradientBrush = accentGradientBrush,
        ambientHaloBrush = ambientHaloBrush,
        badgeBackground = animatedAccent.copy(alpha = ((if (darkTheme) 0.15f else 0.12f) * glowIntensity.multiplier).coerceIn(0.06f, 0.35f)),
        badgeBorder = animatedAccent.copy(alpha = ((if (darkTheme) 0.38f else 0.28f) * glowIntensity.multiplier).coerceIn(0.15f, 0.70f)),
        accentSecondary = animatedSecondary,
        glowIntensity = glowIntensity,
        contrastStyle = contrastStyle
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

    CompositionLocalProvider(
        LocalGlassColors provides glassColors,
        LocalThemeIsDark provides darkTheme,
        LocalThemeAccent provides accent,
        LocalUserPreferences provides userPreferences,
        LocalThemeToggle provides onToggleTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PriceLensTypography,
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
}
