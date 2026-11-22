package com.pricelens.core.designsystem.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pricelens.core.designsystem.PriceLensTheme
import org.intellij.lang.annotations.Language

/**
 * Advanced AGSL Runtime Shader for physically-accurate Liquid Glass rendering
 * based on Kyant0/AndroidLiquidGlass and QmDeve/AndroidLiquidGlassView.
 *
 * Core Optical Physics:
 * 1. 2D Signed Distance Field (SDF) of rounded boxes / capsules
 * 2. Snell's Law toric & meniscus lens refraction bending pixels along outward normals
 * 3. Cauchy chromatic dispersion: physical wavelength separation of R, G, B channels
 * 4. Vibrancy saturation matrix: boosts background contrast & saturation before tinting
 * 5. Dynamic touch tracking: specular highlights dynamically orient towards finger touches
 * 6. Contact specular gleam and chromatic perimeter rim light
 */
@Language("AGSL")
const val LIQUID_GLASS_AGSL = """
    uniform shader content;
    uniform float2 resolution;
    uniform float cornerRadius;
    uniform float refractionHeight;
    uniform float refractionAmount;
    uniform float dispersion;
    uniform float4 tintColor;
    uniform float specularAmount;
    uniform float2 touchPoint;
    uniform float touchPressed;

    // 2D Signed Distance Function for a rounded rectangle centered at resolution / 2
    float sdRoundedBox(float2 p, float2 b, float r) {
        float2 q = abs(p) - b + r;
        return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
    }

    half4 main(float2 coord) {
        float2 halfRes = resolution * 0.5;
        float2 p = coord - halfRes;
        float dist = sdRoundedBox(p, halfRes, cornerRadius);

        // Outside the rounded rectangle bounds
        if (dist > 0.0) {
            return half4(0.0);
        }

        // Refraction zone: dist ranges from -refractionHeight to 0 at perimeter
        float rH = max(refractionHeight, 1.0);
        float edgeFactor = clamp(-dist / rH, 0.0, 1.0);
        float rim = 1.0 - edgeFactor; // 0 in center, 1 at edge

        // Surface normal pointing outward
        float2 norm = (length(p) > 0.001) ? normalize(p) : float2(0.0, 0.0);

        // Meniscus / toric lens curve profile
        float curve = sin(rim * 1.5707963);

        // Cauchy optical dispersion: Red, Green, Blue channel displacement along normal
        float disp = dispersion * refractionAmount * curve;
        float2 offsetR = norm * (curve * refractionAmount * (1.0 + disp * 1.25));
        float2 offsetG = norm * (curve * refractionAmount);
        float2 offsetB = norm * (curve * refractionAmount * (1.0 - disp * 1.25));

        half4 colR = content.eval(coord - offsetR);
        half4 colG = content.eval(coord - offsetG);
        half4 colB = content.eval(coord - offsetB);

        half4 sampled = half4(colR.r, colG.g, colB.b, colG.a);

        // Vibrancy saturation matrix boost (Backdrop vibrancy primitive)
        half luma = dot(sampled.rgb, half3(0.2126, 0.7152, 0.0722));
        sampled.rgb = mix(half3(luma), sampled.rgb, half(1.30));

        // Specular ray lighting
        // Default ambient light from upper-left
        float2 defaultLight = float2(-0.5, -0.866);

        // If touch is active, dynamically steer specular highlight towards finger touch point
        float2 touchDir = (length(touchPoint - coord) > 0.001) ? normalize(touchPoint - coord) : float2(0.0, 0.0);
        float2 effectiveLight = mix(defaultLight, touchDir, touchPressed * 0.70);

        float spec = pow(max(0.0, dot(norm, effectiveLight)), 3.5) * rim * specularAmount;

        // Interactive touch contact gleam when pressed
        float touchDist = length(coord - touchPoint);
        float touchGlow = exp(-touchDist * 0.030) * touchPressed * 0.25;

        // Subtle chromatic perimeter rim glow
        float rimGlow = pow(rim, 2.2) * 0.20;

        // Blend sampled refracted content with translucent glass tint
        half4 tint = half4(tintColor);
        half4 result = mix(sampled, tint, tint.a);
        result.rgb += half3(spec + rimGlow + touchGlow);

        return result;
    }
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
object LiquidGlassShaderHelper {
    fun createShaderEffect(
        width: Float,
        height: Float,
        cornerRadiusPx: Float,
        refractionHeightPx: Float = 24f,
        refractionAmountPx: Float = 14f,
        dispersion: Float = 0.35f,
        tintColor: Color,
        specularAmount: Float = 0.65f,
        touchX: Float = 0f,
        touchY: Float = 0f,
        touchPressed: Float = 0f,
        blurRadiusPx: Float = 0f
    ): RenderEffect {
        val shader = RuntimeShader(LIQUID_GLASS_AGSL)
        shader.setFloatUniform("resolution", width, height)
        shader.setFloatUniform("cornerRadius", cornerRadiusPx)
        shader.setFloatUniform("refractionHeight", refractionHeightPx)
        shader.setFloatUniform("refractionAmount", refractionAmountPx)
        shader.setFloatUniform("dispersion", dispersion)
        shader.setFloatUniform(
            "tintColor",
            tintColor.red,
            tintColor.green,
            tintColor.blue,
            tintColor.alpha
        )
        shader.setFloatUniform("specularAmount", specularAmount)
        shader.setFloatUniform("touchPoint", touchX, touchY)
        shader.setFloatUniform("touchPressed", touchPressed)

        val shaderEffect = RenderEffect.createRuntimeShaderEffect(shader, "content")

        return if (blurRadiusPx > 0f) {
            try {
                val blurEffect = RenderEffect.createBlurEffect(blurRadiusPx, blurRadiusPx, Shader.TileMode.CLAMP)
                RenderEffect.createChainEffect(shaderEffect, blurEffect)
            } catch (_: Throwable) {
                shaderEffect
            }
        } else {
            shaderEffect
        }
    }
}

/**
 * Modifier that applies the physically-accurate Liquid Glass visual effect:
 * - On Android 13+ (API 33+), GPU-accelerated AGSL runtime shader for refraction, dispersion, vibrancy & touch glint.
 * - On all Android versions, renders layered optical glass:
 *   - Translucent glass body fill
 *   - Curvature-based specular rim light highlight
 *   - Multi-stop chromatic dispersion border stroke
 *   - Upper specular glint line
 *   - Interactive touch-following gleam
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(18.dp),
    cornerRadius: Dp = 18.dp,
    tintColor: Color? = null,
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.2.dp,
    showTopGlint: Boolean = true,
    enableShader: Boolean = true,
    interactiveTouch: Boolean = true
): Modifier = composed {
    val density = LocalDensity.current
    val effectiveTint = tintColor ?: PriceLensTheme.glassColors.surfaceGlass
    val effectiveBorderBrush = borderBrush ?: PriceLensTheme.glassColors.specularBorderBrush
    val isDark = PriceLensTheme.isDark
    val glowColor = PriceLensTheme.glassColors.ambientGlow

    var touchOffset by remember { mutableStateOf(Offset.Zero) }
    var isTouchActive by remember { mutableStateOf(false) }

    val animatedTouchPressed by animateFloatAsState(
        targetValue = if (isTouchActive) 1f else 0f,
        animationSpec = spring(stiffness = 600f),
        label = "liquidGlassTouchPressed"
    )

    var modifier = this

    // Interactive pointer input tracking for touch-reactive refraction
    if (interactiveTouch) {
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                touchOffset = down.position
                isTouchActive = true

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id }
                    if (change == null || !change.pressed) {
                        break
                    }
                    touchOffset = change.position
                }
                isTouchActive = false
            }
        }
    }

    // 1. On Android 13+, attach the AGSL runtime shader
    if (enableShader && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }
        val refractionHeightPx = with(density) { 22.dp.toPx() }
        val refractionAmountPx = with(density) { 10.dp.toPx() }

        modifier = modifier.graphicsLayer {
            if (size.width > 0 && size.height > 0) {
                try {
                    val effect = LiquidGlassShaderHelper.createShaderEffect(
                        width = size.width,
                        height = size.height,
                        cornerRadiusPx = cornerRadiusPx,
                        refractionHeightPx = refractionHeightPx,
                        refractionAmountPx = refractionAmountPx,
                        dispersion = 0.35f,
                        tintColor = effectiveTint,
                        specularAmount = if (isDark) 0.65f else 0.50f,
                        touchX = touchOffset.x,
                        touchY = touchOffset.y,
                        touchPressed = animatedTouchPressed,
                        blurRadiusPx = 0f
                    )
                    renderEffect = effect.asComposeRenderEffect()
                } catch (_: Throwable) {
                    // Graceful fallback
                }
            }
        }
    }

    // 2. Base translucent glass body fill and shape clip
    modifier = modifier
        .clip(shape)
        .background(color = effectiveTint, shape = shape)
        .border(
            width = borderWidth,
            brush = effectiveBorderBrush,
            shape = shape
        )

    // 3. Draw specular glint reflection on top edge & touch contact gleam
    modifier = modifier.drawWithCache {
        val glintHeight = 1.5.dp.toPx()
        val glintBrush = Brush.horizontalGradient(
            listOf(
                Color.Transparent,
                (if (isDark) Color.White.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.85f)),
                (if (isDark) glowColor.copy(alpha = 0.40f) else Color.White),
                (if (isDark) Color.White.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.85f)),
                Color.Transparent
            )
        )
        onDrawWithContent {
            drawContent()
            if (showTopGlint) {
                drawRect(
                    brush = glintBrush,
                    topLeft = Offset.Zero,
                    size = Size(size.width, glintHeight)
                )
            }
            if (animatedTouchPressed > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f * animatedTouchPressed),
                            glowColor.copy(alpha = 0.14f * animatedTouchPressed),
                            Color.Transparent
                        ),
                        center = touchOffset,
                        radius = 80.dp.toPx()
                    ),
                    center = touchOffset,
                    radius = 80.dp.toPx()
                )
            }
        }
    }

    modifier
}
