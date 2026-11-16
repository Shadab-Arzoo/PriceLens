package com.pricelens.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    val ambientGlow: Color = BrandMint
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

val LocalGlassColors = staticCompositionLocalOf { DarkGlassColors }
val LocalThemeIsDark = staticCompositionLocalOf { true }
val LocalThemeToggle = staticCompositionLocalOf<() -> Unit> { {} }

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
    onToggleTheme: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val glassColors = if (darkTheme) DarkGlassColors else LightGlassColors

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
}
