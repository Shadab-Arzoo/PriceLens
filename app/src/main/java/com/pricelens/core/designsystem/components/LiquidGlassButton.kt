package com.pricelens.core.designsystem.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pricelens.core.designsystem.PriceLensTheme

/**
 * Modern Liquid Glass pill action button with:
 * - Optical meniscus lens gleam
 * - Specular top rim light highlight
 * - Chromatic perimeter dispersion border
 * - Colored ambient glow reflection
 * - Dynamic tactile spring compression physics
 */
@Composable
fun LiquidGlassPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    fillMaxWidth: Boolean = true,
    shape: Shape = RoundedCornerShape(9999.dp),
    height: Dp = 52.dp,
    gradientBrush: Brush = PriceLensTheme.glassColors.primaryGradientBrush
) {
    val isDark = PriceLensTheme.isDark
    val glowColor = PriceLensTheme.glassColors.ambientGlow

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !loading) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 500f
        ),
        label = "primaryButtonScale"
    )

    val currentElevation by animateDpAsState(
        targetValue = if (isPressed && enabled) 1.dp else if (enabled) 5.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 500f
        ),
        label = "primaryButtonElevation"
    )

    val glintAlpha by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 0.55f,
        animationSpec = spring(stiffness = 500f),
        label = "buttonGlintAlpha"
    )

    val ambientShadow = glowColor.copy(alpha = if (isDark) 0.25f else 0.12f)
    val spotShadow = Color.Black.copy(alpha = if (isDark) 0.35f else 0.15f)

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
                spotColor = spotShadow
            )
            .clip(shape)
            .clickable(
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                indication = ripple(color = PriceLensTheme.glassColors.actionLabel),
                onClick = onClick
            ),
        shape = shape,
        color = PriceLensTheme.glassColors.actionFill,
        border = BorderStroke(1.2.dp, PriceLensTheme.glassColors.specularBorderBrush)
    ) {
        Box(
            modifier = (if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.wrapContentWidth())
                .height(height)
                .background(
                    if (enabled) gradientBrush
                    else Brush.horizontalGradient(
                        listOf(Color.Gray.copy(alpha = 0.35f), Color.Gray.copy(alpha = 0.25f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Inner meniscus specular sheen
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.White.copy(alpha = if (isDark) 0.15f else 0.25f),
                            0.35f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = if (isDark) 0.12f else 0.05f)
                        )
                    )
            )

            // Top specular rim glint
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.4.dp)
                    .align(Alignment.TopCenter)
                    .graphicsLayer { alpha = glintAlpha }
                    .background(PriceLensTheme.glassColors.specularTopGlintBrush)
            )

            Crossfade(targetState = loading, label = "buttonLoadingTransition") { isLoading ->
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = PriceLensTheme.glassColors.actionLabel,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        modifier = Modifier.padding(horizontal = if (fillMaxWidth) 22.dp else 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = PriceLensTheme.glassColors.actionLabel,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PriceLensTheme.glassColors.actionLabel
                        )
                        if (trailingIcon != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = trailingIcon,
                                contentDescription = null,
                                tint = PriceLensTheme.glassColors.actionLabel,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Secondary Liquid Glass pill action button with frosted translucent fill,
 * chromatic specular outline, dynamic ambient glow, and tactile spring compression.
 */
@Composable
fun LiquidGlassSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = true,
    shape: Shape = RoundedCornerShape(9999.dp),
    height: Dp = 48.dp
) {
    val isDark = PriceLensTheme.isDark
    val glowColor = PriceLensTheme.glassColors.ambientGlow

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 500f
        ),
        label = "secondaryButtonScale"
    )

    val currentElevation by animateDpAsState(
        targetValue = if (isPressed && enabled) 0.5.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 500f
        ),
        label = "secondaryButtonElevation"
    )

    val glintAlpha by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.90f else 0.40f,
        animationSpec = spring(stiffness = 500f),
        label = "secondaryButtonGlintAlpha"
    )

    val ambientShadow = glowColor.copy(alpha = if (isDark) 0.15f else 0.06f)
    val spotShadow = Color.Black.copy(alpha = if (isDark) 0.22f else 0.08f)

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
                spotColor = spotShadow
            )
            .clip(shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(color = PriceLensTheme.glassColors.textPrimary),
                onClick = onClick
            ),
        shape = shape,
        color = PriceLensTheme.glassColors.surfaceGlass,
        border = BorderStroke(1.2.dp, PriceLensTheme.glassColors.specularBorderBrush)
    ) {
        Box(
            modifier = (if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.wrapContentWidth())
                .height(height),
            contentAlignment = Alignment.Center
        ) {
            // Subtle specular glint
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.2.dp)
                    .align(Alignment.TopCenter)
                    .graphicsLayer { alpha = glintAlpha }
                    .background(PriceLensTheme.glassColors.specularTopGlintBrush)
            )

            Row(
                modifier = Modifier.padding(horizontal = if (fillMaxWidth) 18.dp else 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PriceLensTheme.glassColors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = PriceLensTheme.glassColors.textPrimary
                )
            }
        }
    }
}
