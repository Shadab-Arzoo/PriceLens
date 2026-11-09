package com.pricelens.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
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
        listOf(ElectricBlue, ElectricBlueLight)
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
        listOf(ElectricBlue, ElectricBlueLight)
    ),
    ambientGlow = CyanAccent
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
        listOf(ElectricBlue, CyanAccent)
    ),
    ambientGlow = CyanGlow
)

val LocalGlassColors = staticCompositionLocalOf { LightGlassColors }

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = DeepIndigo,
    secondary = Color(0xFF535F70),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E3F8),
    onSecondaryContainer = Color(0xFF101C2B),
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
    primaryContainer = Color(0xFF0040A8),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253141),
    secondaryContainer = Color(0xFF3B4858),
    onSecondaryContainer = Color(0xFFD7E3F8),
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
