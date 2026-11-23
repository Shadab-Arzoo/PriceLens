package com.pricelens.core.designsystem.components

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pricelens.core.designsystem.PriceLensTheme
import kotlinx.coroutines.launch

/**
 * Jetpack Compose implementation of QmDeve/AndroidLiquidGlassView.
 * (https://github.com/QmDeve/AndroidLiquidGlassView)
 *
 * Simulates a realistic physically-based Liquid Glass surface with:
 * 1. Physical Snell's Law refraction: Bends pixels along boundary normals.
 * 2. Cauchy chromatic dispersion: Wavelength separation of R, G, B channels.
 * 3. Configurable parameters:
 *    - `cornerRadius` (from setCornerRadius)
 *    - `refractionHeight` (from setRefractionHeight)
 *    - `refractionOffset` / `refractionAmount` (from setRefractionOffset)
 *    - `dispersion` (from setDispersion)
 *    - `blurRadius` (from setBlurRadius)
 * 4. iOS-style interactive touch effects:
 *    - 3D perspective tilt (`rotationX`, `rotationY`) tracking touch coordinates
 *    - Tactile spring compression on press
 *    - Touch-following specular contact gleam
 *    - Smooth spring return on touch release
 * 5. Safe Fallback Behavior:
 *    - On Android versions below 13 (API < 33), gracefully renders clean translucent glass without crashing.
 * 6. Theme Isolation:
 *    - When iOS Switch is OFF (`!PriceLensTheme.isCupertino`), maintains the default theme appearance with zero regressions.
 */
@Composable
fun LiquidGlassView(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    refractionHeight: Dp = 26.dp,
    refractionOffset: Dp = 12.dp,
    dispersion: Float = 0.25f,
    blurRadius: Dp = 16.dp,
    tintColor: Color = Color.Unspecified,
    specularAmount: Float = 0.60f,
    borderWidth: Dp = 0.75.dp,
    borderColor: Color? = null,
    isInteractive: Boolean = true,
    shape: Shape = RoundedCornerShape(cornerRadius),
    content: @Composable BoxScope.() -> Unit
) {
    val isCupertino = PriceLensTheme.isCupertino
    val isDark = PriceLensTheme.isDark
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // 1. Resolve theme colors
    val defaultTint = if (isCupertino) {
        if (!isDark) Color(0xFFFAFAFA).copy(alpha = 0.65f) else Color(0xFF1C1C1E).copy(alpha = 0.60f)
    } else {
        PriceLensTheme.glassColors.surfaceGlass
    }
    val effectiveTint = if (tintColor.isSpecified) tintColor else defaultTint

    val defaultBorderColor = if (isCupertino) {
        if (isDark) Color(0x38FFFFFF) else Color(0x20000000)
    } else {
        PriceLensTheme.glassColors.specularBorder
    }
    val effectiveBorderColor = borderColor ?: defaultBorderColor

    val defaultGlintBrush = PriceLensTheme.glassColors.specularTopGlintBrush
    val effectIntensity = if (isCupertino) PriceLensTheme.iosGlassIntensity else 1.0f

    // 2. QmDeve iOS-style touch physics: 3D tilt, spring scaling, and touch tracking
    val tiltRotationX = remember { Animatable(0f) }
    val tiltRotationY = remember { Animatable(0f) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }
    var isTouchActive by remember { mutableStateOf(false) }

    val pressScale by animateFloatAsState(
        targetValue = if (isTouchActive && isInteractive && isCupertino) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 500f
        ),
        label = "liquidGlassViewPressScale"
    )

    val animatedTouchPressed by animateFloatAsState(
        targetValue = if (isTouchActive && isCupertino) 1f else 0f,
        animationSpec = spring(stiffness = 600f),
        label = "liquidGlassViewTouchPressed"
    )

    // Interactive pointer input tracking for tilt and specular highlight
    val touchModifier = if (isInteractive && isCupertino) {
        Modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                touchOffset = down.position
                isTouchActive = true

                val width = size.width.toFloat()
                val height = size.height.toFloat()
                if (width > 0 && height > 0) {
                    val normX = ((down.position.x - width / 2f) / (width / 2f)).coerceIn(-1f, 1f)
                    val normY = ((down.position.y - height / 2f) / (height / 2f)).coerceIn(-1f, 1f)
                    val maxTilt = 4.5f // subtle iOS tilt
                    coroutineScope.launch {
                        tiltRotationX.animateTo(
                            -normY * maxTilt,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                        )
                    }
                    coroutineScope.launch {
                        tiltRotationY.animateTo(
                            normX * maxTilt,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                        )
                    }
                }

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id }
                    if (change == null || !change.pressed) {
                        break
                    }
                    touchOffset = change.position
                    val curWidth = size.width.toFloat()
                    val curHeight = size.height.toFloat()
                    if (curWidth > 0 && curHeight > 0) {
                        val normX = ((change.position.x - curWidth / 2f) / (curWidth / 2f)).coerceIn(-1f, 1f)
                        val normY = ((change.position.y - curHeight / 2f) / (curHeight / 2f)).coerceIn(-1f, 1f)
                        val maxTilt = 4.5f
                        coroutineScope.launch {
                            tiltRotationX.snapTo(-normY * maxTilt)
                            tiltRotationY.snapTo(normX * maxTilt)
                        }
                    }
                }

                isTouchActive = false
                coroutineScope.launch {
                    tiltRotationX.animateTo(
                        0f,
                        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                    )
                    tiltRotationY.animateTo(
                        0f,
                        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                    )
                }
            }
        }
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .then(touchModifier)
            .graphicsLayer {
                if (isCupertino) {
                    scaleX = pressScale
                    scaleY = pressScale
                    if (isInteractive) {
                        rotationX = tiltRotationX.value
                        rotationY = tiltRotationY.value
                        cameraDistance = 16f * density.density
                    }
                }

                // 3. Android 13+ GPU AGSL Refraction & Dispersion Shader
                if (isCupertino && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && size.width > 0 && size.height > 0) {
                    try {
                        val effect = LiquidGlassShaderHelper.createShaderEffect(
                            width = size.width,
                            height = size.height,
                            cornerRadiusPx = with(density) { cornerRadius.toPx() },
                            refractionHeightPx = with(density) { (refractionHeight * effectIntensity).toPx() },
                            refractionAmountPx = with(density) { (refractionOffset * effectIntensity).toPx() },
                            dispersion = (dispersion * effectIntensity).coerceIn(0f, 1f),
                            tintColor = effectiveTint,
                            specularAmount = specularAmount,
                            touchX = touchOffset.x,
                            touchY = touchOffset.y,
                            touchPressed = animatedTouchPressed,
                            blurRadiusPx = with(density) { (blurRadius * effectIntensity).toPx() }
                        )
                        renderEffect = effect.asComposeRenderEffect()
                    } catch (_: Throwable) {
                        // Graceful fallback for non-shader devices
                    }
                }
            }
            .clip(shape)
            .drawWithCache {
                val glintHeight = (if (isCupertino) 1.0.dp else 1.4.dp).toPx()
                val glintBrush = if (isCupertino) {
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            if (isDark) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.65f),
                            Color.White.copy(alpha = if (isDark) 0.50f else 0.85f),
                            if (isDark) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.65f),
                            Color.Transparent
                        )
                    )
                } else {
                    defaultGlintBrush
                }

                onDrawWithContent {
                    drawContent()

                    // Top specular glint line
                    drawRect(
                        brush = glintBrush,
                        topLeft = Offset.Zero,
                        size = Size(size.width, glintHeight)
                    )

                    // Touch contact gleam on active touch
                    if (animatedTouchPressed > 0f && isCupertino) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.22f * animatedTouchPressed),
                                    Color.White.copy(alpha = 0.08f * animatedTouchPressed),
                                    Color.Transparent
                                ),
                                center = touchOffset,
                                radius = 70.dp.toPx()
                            ),
                            center = touchOffset,
                            radius = 70.dp.toPx()
                        )
                    }
                }
            },
        shape = shape,
        color = effectiveTint,
        border = BorderStroke(borderWidth, effectiveBorderColor)
    ) {
        Box {
            if (isCupertino) {
                // Frosted optical volume sheen
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to (if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.40f)),
                                0.30f to (if (isDark) Color.White.copy(alpha = 0.02f) else Color.White.copy(alpha = 0.08f)),
                                1.0f to Color.Transparent
                            )
                        )
                )
            } else {
                // Meniscus light gradient (for default non-Cupertino theme)
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
            }

            content()
        }
    }
}
