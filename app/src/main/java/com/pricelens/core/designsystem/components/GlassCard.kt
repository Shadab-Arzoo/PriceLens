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
import androidx.compose.ui.unit.dp
import com.pricelens.core.designsystem.PriceLensTheme

/**
 * Optical Liquid Glass Card featuring:
 * - Translucent tinted glass body with refractive depth
 * - Chromatic dispersion specular border (prism separation at edges)
 * - Directional upper specular reflection glint
 * - Inner meniscus light gradient
 * - Ambient accent glow shadow
 * - Tactile spring compression on press
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
    val isDark = PriceLensTheme.isDark
    val glowColor = PriceLensTheme.glassColors.ambientGlow

    val effectiveBorderStroke = when {
        borderBrush != null -> BorderStroke(borderWidth, borderBrush)
        borderColor != null -> BorderStroke(borderWidth, borderColor)
        else -> BorderStroke(borderWidth, PriceLensTheme.glassColors.specularBorderBrush)
    }

    val ambientShadow = glowColor.copy(alpha = if (isDark) 0.16f else 0.08f)
    val spotShadow = Color.Black.copy(alpha = if (isDark) 0.28f else 0.10f)

    if (onClick != null) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.965f else 1f,
            animationSpec = spring(
                dampingRatio = 0.72f,
                stiffness = 450f
            ),
            label = "glassCardSpring"
        )

        val currentElevation by animateDpAsState(
            targetValue = if (isPressed) 0.5.dp else elevation,
            animationSpec = spring(
                dampingRatio = 0.72f,
                stiffness = 450f
            ),
            label = "glassCardElevation"
        )

        val glintAlpha by animateFloatAsState(
            targetValue = if (isPressed) 0.90f else 0.50f,
            animationSpec = spring(stiffness = 500f),
            label = "glassCardGlintAlpha"
        )

        Surface(
            modifier = modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(
                    elevation = currentElevation,
                    shape = shape,
                    ambientColor = ambientShadow,
                    spotColor = spotShadow,
                    clip = false
                )
                .clip(shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(color = PriceLensTheme.glassColors.textPrimary),
                    onClick = onClick
                ),
            shape = shape,
            color = backgroundColor,
            border = effectiveBorderStroke
        ) {
            Box {
                if (showSpecularSheen) {
                    // Inner meniscus glass volume gradient
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
                    // Directional top rim specular reflection glint
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.4.dp)
                            .align(Alignment.TopCenter)
                            .graphicsLayer { alpha = glintAlpha }
                            .background(PriceLensTheme.glassColors.specularTopGlintBrush)
                    )
                }
                content()
            }
        }
    } else {
        Surface(
            modifier = modifier
                .shadow(
                    elevation = elevation,
                    shape = shape,
                    ambientColor = ambientShadow,
                    spotColor = spotShadow,
                    clip = false
                )
                .clip(shape),
            shape = shape,
            color = backgroundColor,
            border = effectiveBorderStroke
        ) {
            Box {
                if (showSpecularSheen) {
                    // Inner meniscus glass volume gradient
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
                    // Directional top rim specular reflection glint
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.4.dp)
                            .align(Alignment.TopCenter)
                            .background(PriceLensTheme.glassColors.specularTopGlintBrush)
                    )
                }
                content()
            }
        }
    }
}
