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
    val specularBorderBrush: Brush = Brush.verticalGradient(
        listOf(LightGlassSpecularTop, LightGlassSpecularBottom)
    ),
    val primaryGradientBrush: Brush = Brush.horizontalGradient(
        listOf(ElectricBlue, Color(0xFF4F46E5))
    ),
    val ambientGlow: Color = CyanAccent
)

private val LightGlassColors = GlassColors(
    backgroundBrush = Brush.verticalGradient(listOf(LightBackgroundStart, LightBackgroundEnd)),
    surfaceGlass = LightGlassSurface,
    surfaceHighlight = LightGlassHighlight,
    specularBorder = LightGlassBorder,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    specularBorderBrush = Brush.verticalGradient(
        listOf(LightGlassSpecularTop, LightGlassSpecularBottom)
    ),
    primaryGradientBrush = Brush.horizontalGradient(
        listOf(ElectricBlue, Color(0xFF4F46E5))
    ),
    ambientGlow = ElectricBlueGlow
)

private val DarkGlassColors = GlassColors(
    backgroundBrush = Brush.verticalGradient(listOf(DarkBackgroundStart, DarkBackgroundEnd)),
    surfaceGlass = DarkGlassSurface,
    surfaceHighlight = DarkGlassHighlight,
    specularBorder = DarkGlassBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    specularBorderBrush = Brush.verticalGradient(
        listOf(DarkGlassSpecularTop, DarkGlassSpecularBottom)
    ),
    primaryGradientBrush = Brush.horizontalGradient(
        listOf(ElectricBlue, Color(0xFF6366F1))
    ),
    ambientGlow = CyanGlow
)

val LocalGlassColors = staticCompositionLocalOf { LightGlassColors }

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Color(0xFF475569),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF0F172A),
    background = LightBackgroundStart,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    error = CoralError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlueLight,
    onPrimary = DeepIndigo,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E2638),
    onSecondaryContainer = Color(0xFFF8FAFC),
    background = DarkBackgroundStart,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    error = CoralError,
    onError = Color.White
)

@Composable
fun PriceLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    CompositionLocalProvider(LocalGlassColors provides glassColors) {
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
}
