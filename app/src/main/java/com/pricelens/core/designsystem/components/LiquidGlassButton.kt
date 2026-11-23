package com.pricelens.core.designsystem.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import com.pricelens.core.designsystem.PriceLensTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

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
    val isCupertino = PriceLensTheme.isCupertino
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
        targetValue = if (isPressed && enabled) 1.dp else if (enabled) (if (isCupertino) 2.dp else 5.dp) else 0.dp,
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

    val ambientShadow = if (isCupertino) {
        Color.Black.copy(alpha = if (isDark) 0.20f else 0.05f)
    } else {
        glowColor.copy(alpha = if (isDark) 0.25f else 0.12f)
    }
    val spotShadow = if (isCupertino) {
        Color.Black.copy(alpha = if (isDark) 0.35f else 0.10f)
    } else {
        Color.Black.copy(alpha = if (isDark) 0.35f else 0.15f)
    }

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
        border = if (isCupertino) {
            BorderStroke(0.5.dp, if (isDark) Color.White.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.40f))
        } else {
            BorderStroke(1.2.dp, PriceLensTheme.glassColors.specularBorderBrush)
        }
    ) {
        Box(
            modifier = (if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.wrapContentWidth())
                .height(height)
                .background(
                    if (enabled) {
                        if (isCupertino && gradientBrush == PriceLensTheme.glassColors.primaryGradientBrush) {
                            Brush.verticalGradient(
                                listOf(
                                    if (isDark) Color(0xFF0091FF) else Color(0xFF0088FF),
                                    if (isDark) Color(0xFF0077E6) else Color(0xFF006ED6)
                                )
                            )
                        } else {
                            gradientBrush
                        }
                    } else {
                        Brush.horizontalGradient(
                            listOf(Color.Gray.copy(alpha = 0.35f), Color.Gray.copy(alpha = 0.25f))
                        )
                    }
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
    val isCupertino = PriceLensTheme.isCupertino
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
        targetValue = if (isPressed && enabled) 0.5.dp else (if (isCupertino) 1.dp else 2.dp),
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

    val ambientShadow = if (isCupertino) {
        Color.Black.copy(alpha = if (isDark) 0.15f else 0.03f)
    } else {
        glowColor.copy(alpha = if (isDark) 0.15f else 0.06f)
    }
    val spotShadow = if (isCupertino) {
        Color.Black.copy(alpha = if (isDark) 0.25f else 0.06f)
    } else {
        Color.Black.copy(alpha = if (isDark) 0.22f else 0.08f)
    }

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
        color = if (isCupertino) {
            if (!isDark) Color(0xFFFAFAFA).copy(alpha = 0.65f) else Color(0xFF2C2C2E).copy(alpha = 0.60f)
        } else {
            PriceLensTheme.glassColors.surfaceGlass
        },
        border = if (isCupertino) {
            BorderStroke(0.75.dp, if (isDark) Color.White.copy(alpha = 0.20f) else Color.Black.copy(alpha = 0.08f))
        } else {
            BorderStroke(1.2.dp, PriceLensTheme.glassColors.specularBorderBrush)
        }
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

            val secondaryTextColor = if (isCupertino) {
                if (isDark) Color(0xFF0091FF) else Color(0xFF0088FF)
            } else {
                PriceLensTheme.glassColors.textPrimary
            }

            Row(
                modifier = Modifier.padding(horizontal = if (fillMaxWidth) 18.dp else 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = secondaryTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = secondaryTextColor
                )
            }
        }
    }
}

/**
 * iOS Liquid Glass Button inspired by Kyant0/AndroidLiquidGlass.
 *
 * Implements fluid touch offset translation (via hyperbolic tangent damping)
 * and interactive directional squash-and-stretch scaling (via trigonometric angle projection).
 */
@Composable
fun LiquidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    content: @Composable RowScope.() -> Unit
) {
    val isCupertino = PriceLensTheme.isCupertino
    val isDark = PriceLensTheme.isDark
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val pressAnimatable = remember { Animatable(0f) }
    val animatedOffsetX = remember { Animatable(0f) }
    val animatedOffsetY = remember { Animatable(0f) }

    val defaultSurfaceColor = if (isCupertino) {
        if (!isDark) Color(0xFFFAFAFA).copy(alpha = 0.60f) else Color(0xFF1E1E1E).copy(alpha = 0.60f)
    } else {
        PriceLensTheme.glassColors.surfaceGlass
    }
    val resolvedSurfaceColor = if (surfaceColor.isSpecified) surfaceColor else defaultSurfaceColor

    val borderStroke = if (isCupertino) {
        BorderStroke(0.75.dp, if (isDark) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.08f))
    } else {
        BorderStroke(1.2.dp, PriceLensTheme.glassColors.specularBorderBrush)
    }

    Surface(
        modifier = modifier
            .graphicsLayer {
                if (isInteractive) {
                    val width = size.width
                    val height = size.height

                    val progress = pressAnimatable.value
                    val scale = lerp(1f, 1f + with(density) { 4.dp.toPx() } / (if (size.height > 0) size.height else 1f), progress)

                    val maxOffset = size.minDimension.coerceAtLeast(1f)
                    val initialDerivative = 0.05f
                    val offX = animatedOffsetX.value
                    val offY = animatedOffsetY.value
                    translationX = maxOffset * tanh(initialDerivative * offX / maxOffset)
                    translationY = maxOffset * tanh(initialDerivative * offY / maxOffset)

                    val maxDragScale = with(density) { 4.dp.toPx() } / (if (size.height > 0) size.height else 1f)
                    val offsetAngle = atan2(offY, offX)
                    val maxDim = size.maxDimension.coerceAtLeast(1f)
                    scaleX = scale + maxDragScale * abs(cos(offsetAngle) * offX / maxDim) *
                            (width / (if (height > 0) height else 1f)).fastCoerceAtMost(1f)
                    scaleY = scale + maxDragScale * abs(sin(offsetAngle) * offY / maxDim) *
                            (height / (if (width > 0) width else 1f)).fastCoerceAtMost(1f)
                }
            }
            .clip(RoundedCornerShape(9999.dp))
            .pointerInput(isInteractive) {
                if (!isInteractive) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val pointerId = down.id
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val initialOffset = down.position - center
                    coroutineScope.launch {
                        pressAnimatable.animateTo(
                            1f,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                        )
                    }
                    coroutineScope.launch {
                        animatedOffsetX.animateTo(
                            initialOffset.x,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                        )
                    }
                    coroutineScope.launch {
                        animatedOffsetY.animateTo(
                            initialOffset.y,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                        )
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId }
                        if (change == null || !change.pressed) {
                            break
                        }
                        val currentOffset = change.position - center
                        coroutineScope.launch {
                            animatedOffsetX.snapTo(currentOffset.x)
                            animatedOffsetY.snapTo(currentOffset.y)
                        }
                        change.consume()
                    }

                    coroutineScope.launch {
                        pressAnimatable.animateTo(
                            0f,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                        )
                    }
                    coroutineScope.launch {
                        animatedOffsetX.animateTo(
                            0f,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                        )
                        animatedOffsetY.animateTo(
                            0f,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                        )
                    }
                }
            }
            .clickable(
                role = Role.Button,
                onClick = onClick
            ),
        shape = RoundedCornerShape(9999.dp),
        color = resolvedSurfaceColor,
        border = borderStroke
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Specular top rim light highlight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        if (isCupertino) {
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    (if (isDark) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.65f)),
                                    Color.Transparent
                                )
                            )
                        } else {
                            PriceLensTheme.glassColors.specularTopGlintBrush
                        }
                    )
            )

            if (tint.isSpecified) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(tint.copy(alpha = 0.75f))
                )
            }

            Row(
                modifier = Modifier
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}
