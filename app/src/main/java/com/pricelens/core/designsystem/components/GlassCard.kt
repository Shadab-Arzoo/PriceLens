package com.pricelens.core.designsystem.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.pricelens.core.designsystem.PriceLensTheme

/**
 * Optical Liquid Glass Card featuring:
 * - Kyant0/AndroidLiquidGlass backdrop vibrancy, Gaussian blur, lens refraction & dispersion in iOS mode
 * - Translucent tinted glass body with refractive depth
 * - Chromatic dispersion specular border (prism separation at edges)
 * - Directional upper specular reflection glint & inner meniscus light gradient
 * - Ambient accent glow shadow & tactile spring compression on press
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    backgroundColor: Color = PriceLensTheme.glassColors.surfaceGlass,
    borderColor: Color? = null,
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.2.dp,
    elevation: Dp = 3.dp,
    showSpecularSheen: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isCupertino = PriceLensTheme.isCupertino
    val isDark = PriceLensTheme.isDark
    val glowColor = PriceLensTheme.glassColors.ambientGlow
    val canvasBackdrop = PriceLensTheme.canvasBackdrop
    val effectIntensity = if (isCupertino) PriceLensTheme.iosGlassIntensity else 1.0f

    val effectiveBorderStroke = when {
        borderBrush != null -> BorderStroke(borderWidth, borderBrush)
        borderColor != null -> BorderStroke(borderWidth, borderColor)
        isCupertino -> BorderStroke(0.75.dp, PriceLensTheme.glassColors.specularBorderBrush)
        else -> BorderStroke(borderWidth, PriceLensTheme.glassColors.specularBorderBrush)
    }

    val ambientShadow = if (isCupertino) {
        Color.Black.copy(alpha = if (isDark) 0.24f else 0.05f)
    } else {
        glowColor.copy(alpha = if (isDark) 0.16f else 0.08f)
    }
    val spotShadow = if (isCupertino) {
        Color.Black.copy(alpha = if (isDark) 0.36f else 0.09f)
    } else {
        Color.Black.copy(alpha = if (isDark) 0.28f else 0.10f)
    }

    val interactionSource = remember(onClick != null) { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 450f
        ),
        label = "glassCardSpring"
    )

    val currentElevation by animateDpAsState(
        targetValue = if (onClick != null && isPressed) 0.5.dp else elevation,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 450f
        ),
        label = "glassCardElevation"
    )

    val glintAlpha by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.90f else if (isCupertino) 0.65f else 0.50f,
        animationSpec = spring(stiffness = 500f),
        label = "glassCardGlintAlpha"
    )

    val activeBackdrop = if (isCupertino) canvasBackdrop else null
    val useBackdrop = activeBackdrop != null
    val backdropModifier = if (activeBackdrop != null) {
        val supportsLens = shape is CornerBasedShape || shape is AbsoluteRoundedCornerShape
        Modifier.drawBackdrop(
            backdrop = activeBackdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur((10f * effectIntensity).dp.toPx())
                if (supportsLens) {
                    lens(
                        refractionHeight = (9f * effectIntensity).dp.toPx(),
                        refractionAmount = (14f * effectIntensity).dp.toPx(),
                        depthEffect = true,
                        chromaticAberration = true
                    )
                }
            },
            highlight = {
                Highlight.Default.copy(
                    width = 0.75.dp,
                    alpha = if (isDark) 0.52f else 0.76f
                )
            },
            shadow = {
                Shadow(
                    radius = (currentElevation.value * 2.5f).dp,
                    color = Color.Black.copy(alpha = if (isDark) 0.26f else 0.07f)
                )
            },
            innerShadow = {
                InnerShadow(
                    radius = (5f * effectIntensity).dp,
                    offset = DpOffset(0.dp, 1.5.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.09f) else Color.White.copy(alpha = 0.45f),
                    alpha = 0.75f
                )
            },
            onDrawSurface = {
                drawRect(backgroundColor)
            }
        )
    } else {
        Modifier.shadow(
            elevation = currentElevation,
            shape = shape,
            ambientColor = ambientShadow,
            spotColor = spotShadow,
            clip = false
        )
    }

    val interactiveModifier = if (onClick != null) {
        Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(backdropModifier)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = PriceLensTheme.glassColors.textPrimary),
                onClick = onClick
            )
    } else {
        Modifier
            .then(backdropModifier)
            .clip(shape)
    }

    Surface(
        modifier = modifier.then(interactiveModifier),
        shape = shape,
        color = if (useBackdrop) Color.Transparent else backgroundColor,
        border = effectiveBorderStroke
    ) {
        Box {
            if (showSpecularSheen) {
                if (!isCupertino) {
                    // Inner meniscus glass volume gradient (non-Apple themes)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    0.0f to Color.White.copy(alpha = if (isDark) 0.05f else 0.12f),
                                    0.28f to Color.Transparent,
                                    1.0f to Color.Black.copy(alpha = if (isDark) 0.06f else 0.02f)
                                )
                            )
                    )
                } else {
                    // iOS Liquid Glass: Optical frosted volume gleam & bottom bounce light
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    0.0f to (if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.42f)),
                                    0.26f to (if (isDark) Color.White.copy(alpha = 0.02f) else Color.White.copy(alpha = 0.10f)),
                                    0.85f to Color.Transparent,
                                    1.0f to (if (isDark) Color.White.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.14f))
                                )
                            )
                    )
                }
                // Directional top rim specular reflection glint
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isCupertino) 0.9.dp else 1.4.dp)
                        .align(Alignment.TopCenter)
                        .graphicsLayer { alpha = glintAlpha }
                        .background(PriceLensTheme.glassColors.specularTopGlintBrush)
                )
            }
            content()
        }
    }
}
