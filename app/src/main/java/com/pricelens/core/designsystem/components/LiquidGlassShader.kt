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
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CornerBasedShape
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
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
    uniform float fresnelIntensity;

    // 2D Signed Distance Function for a rounded rectangle centered at resolution / 2
    float sdRoundedBox(float2 p, float2 b, float r) {
        float2 q = abs(p) - b + r;
        return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
    }

    // Exact SDF outward surface normal matching Kyant0/AndroidLiquidGlass
    float2 gradSdRoundedBox(float2 p, float2 b, float r) {
        float2 q = abs(p) - b + r;
        if (q.x >= 0.0 || q.y >= 0.0) {
            return sign(p) * normalize(max(q, 0.0001));
        } else {
            float gradX = step(q.y, q.x);
            return sign(p) * float2(gradX, 1.0 - gradX);
        }
    }

    // Meniscus toric lens profile: smoothly curves from 0 in interior to 1 at rim
    float circleMap(float x) {
        float cx = clamp(x, 0.0, 1.0);
        return 1.0 - sqrt(max(0.0, 1.0 - cx * cx));
    }

    half4 main(float2 coord) {
        float2 halfRes = max(resolution * 0.5, float2(1.0));
        float2 p = coord - halfRes;
        float clampedRadius = min(cornerRadius, min(halfRes.x, halfRes.y));
        float dist = sdRoundedBox(p, halfRes, clampedRadius);

        // Outside the rounded rectangle bounds
        if (dist > 0.0) {
            return half4(0.0);
        }

        // Refraction zone: dist ranges from -refractionHeight to 0 at perimeter
        float rH = max(refractionHeight, 1.0);
        float edgeFactor = clamp(-dist / rH, 0.0, 1.0);
        float rim = 1.0 - edgeFactor; // 0 in center, 1 at edge

        // Smooth outward normal combining rounded-box gradient & subtle center depth
        float gradRadius = min(clampedRadius * 1.4, min(halfRes.x, halfRes.y));
        float2 radial = (length(p) > 0.001) ? normalize(p) : float2(0.0, 0.0);
        float2 norm = normalize(gradSdRoundedBox(p, halfRes, gradRadius) + radial * 0.18);

        // Meniscus / toric lens curve profile
        float curve = circleMap(rim);
        float2 refractedCoord = coord - norm * (curve * refractionAmount);

        // 7-band spectral Cauchy optical dispersion around curved edges
        float dispStrength = dispersion * curve * refractionAmount * 0.85;
        float2 dispVec = norm * dispStrength;

        half4 sampled;
        if (dispStrength > 0.05) {
            half4 col = half4(0.0);
            half4 red = content.eval(refractedCoord - dispVec);
            half4 orange = content.eval(refractedCoord - dispVec * 0.666);
            half4 yellow = content.eval(refractedCoord - dispVec * 0.333);
            half4 green = content.eval(refractedCoord);
            half4 cyan = content.eval(refractedCoord + dispVec * 0.333);
            half4 blue = content.eval(refractedCoord + dispVec * 0.666);
            half4 purple = content.eval(refractedCoord + dispVec);

            col.r = (red.r + orange.r + yellow.r) / 3.5 + purple.r / 7.0;
            col.g = orange.g / 7.0 + (yellow.g + green.g + cyan.g) / 3.5;
            col.b = (cyan.b + blue.b + purple.b) / 3.0;
            col.a = (red.a + orange.a + yellow.a + green.a + cyan.a + blue.a + purple.a) / 7.0;
            sampled = col;
        } else {
            sampled = content.eval(refractedCoord);
        }

        // Vibrancy saturation boost (Backdrop vibrancy primitive)
        half luma = dot(sampled.rgb, half3(0.2126, 0.7152, 0.0722));
        sampled.rgb = mix(half3(luma), sampled.rgb, half(1.35));

        // Specular ray lighting: default ambient light from upper-left
        float2 defaultLight = float2(-0.48, -0.877);

        // If touch is active, dynamically steer specular highlight towards finger touch point
        float2 touchDelta = touchPoint - coord;
        float2 touchDir = (length(touchDelta) > 0.001) ? normalize(touchDelta) : float2(0.0, 0.0);
        float2 effectiveLight = normalize(mix(defaultLight, touchDir, touchPressed * 0.75));

        float spec = pow(max(0.0, dot(norm, effectiveLight)), 3.2) * rim * specularAmount;

        // Schlick Fresnel grazing-angle rim reflection
        float fresnel = pow(rim, 2.4) * fresnelIntensity;

        // Resolution-scaled interactive touch contact gleam when pressed
        float minDim = max(min(resolution.x, resolution.y), 1.0);
        float normTouchDist = length(touchDelta) / (minDim * 0.42);
        float touchGlow = exp(-normTouchDist * normTouchDist * 2.5) * touchPressed * 0.28;

        // Blend sampled refracted content with translucent glass tint
        half4 tint = half4(tintColor);
        half4 result = mix(sampled, tint, tint.a);
        result.rgb += half3(spec + fresnel + touchGlow);

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
        blurRadiusPx: Float = 0f,
        fresnelIntensity: Float = 0.26f
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
        shader.setFloatUniform("fresnelIntensity", fresnelIntensity)

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
    interactiveTouch: Boolean = true,
    backdrop: Backdrop? = null
): Modifier = composed {
    val density = LocalDensity.current
    val effectiveTint = tintColor ?: PriceLensTheme.glassColors.surfaceGlass
    val effectiveBorderBrush = borderBrush ?: PriceLensTheme.glassColors.specularBorderBrush
    val isDark = PriceLensTheme.isDark
    val isCupertino = PriceLensTheme.isCupertino
    val glowColor = PriceLensTheme.glassColors.ambientGlow
    val effectIntensity = if (isCupertino) PriceLensTheme.iosGlassIntensity else 1.0f
    val resolvedBackdrop = backdrop ?: if (isCupertino) PriceLensTheme.canvasBackdrop else null

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

    // 1. Real optical backdrop sampling (blur + vibrancy + chromatic lens) behind foreground content
    if (resolvedBackdrop != null) {
        val supportsLens = shape is CornerBasedShape || shape is AbsoluteRoundedCornerShape
        modifier = modifier.drawBackdrop(
            backdrop = resolvedBackdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur((10f * effectIntensity).dp.toPx())
                if (supportsLens) {
                    lens(
                        refractionHeight = (12f * effectIntensity).dp.toPx(),
                        refractionAmount = (16f * effectIntensity).dp.toPx(),
                        depthEffect = true,
                        chromaticAberration = true
                    )
                }
            },
            highlight = {
                Highlight.Default.copy(
                    width = 0.85.dp,
                    alpha = (if (isDark) 0.58f else 0.82f) * (0.8f + 0.2f * animatedTouchPressed)
                )
            },
            innerShadow = {
                InnerShadow(
                    radius = (6f * effectIntensity).dp,
                    offset = DpOffset(0.dp, 2.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.50f),
                    alpha = 0.75f
                )
            },
            onDrawSurface = {
                drawRect(effectiveTint.copy(alpha = (effectiveTint.alpha * 0.72f).coerceIn(0.18f, 0.78f)))
            }
        )
    }

    // 2. Shape clip & optical border stroke
    modifier = modifier
        .clip(shape)
        .then(
            if (resolvedBackdrop == null) {
                Modifier.background(color = effectiveTint, shape = shape)
            } else {
                Modifier
            }
        )
        .border(
            width = borderWidth,
            brush = effectiveBorderBrush,
            shape = shape
        )

    // 3. Render AGSL shader onto an isolated background glass layer so child content stays razor-sharp
    val shaderGlassLayer = rememberGraphicsLayer()
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }
    val refractionHeightPx = with(density) { ((if (isCupertino) 24.dp else 20.dp) * effectIntensity).toPx() }
    val refractionAmountPx = with(density) { ((if (isCupertino) 12.dp else 10.dp) * effectIntensity).toPx() }
    val blurRadiusPx = if (resolvedBackdrop == null && isCupertino) with(density) { (8.dp * effectIntensity).toPx() } else 0f

    modifier = modifier.drawWithCache {
        val glintHeight = (if (isCupertino) 1.0.dp else 1.5.dp).toPx()
        val glintBrush = if (isCupertino) {
            Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    (if (isDark) Color.White.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.75f)),
                    Color.White.copy(alpha = if (isDark) 0.60f else 0.92f),
                    (if (isDark) Color.White.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.75f)),
                    Color.Transparent
                )
            )
        } else {
            Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    (if (isDark) Color.White.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.85f)),
                    (if (isDark) glowColor.copy(alpha = 0.40f) else Color.White),
                    (if (isDark) Color.White.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.85f)),
                    Color.Transparent
                )
            )
        }
        val volumeBrush = Brush.verticalGradient(
            0.0f to (if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.44f)),
            0.35f to (if (isDark) Color.White.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.10f)),
            0.85f to Color.Transparent,
            1.0f to (if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.16f))
        )

        onDrawWithContent {
            if (enableShader && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && size.width > 0f && size.height > 0f) {
                try {
                    val effect = LiquidGlassShaderHelper.createShaderEffect(
                        width = size.width,
                        height = size.height,
                        cornerRadiusPx = cornerRadiusPx,
                        refractionHeightPx = refractionHeightPx,
                        refractionAmountPx = refractionAmountPx,
                        dispersion = ((if (isCupertino) 0.30f else 0.38f) * effectIntensity).coerceIn(0f, 1f),
                        tintColor = effectiveTint.copy(alpha = if (resolvedBackdrop != null) 0.25f else effectiveTint.alpha),
                        specularAmount = if (isDark) (if (isCupertino) 0.60f else 0.70f) else 0.55f,
                        touchX = touchOffset.x,
                        touchY = touchOffset.y,
                        touchPressed = animatedTouchPressed,
                        blurRadiusPx = blurRadiusPx,
                        fresnelIntensity = (0.24f * effectIntensity).coerceIn(0.10f, 0.40f)
                    )
                    shaderGlassLayer.renderEffect = effect.asComposeRenderEffect()
                    shaderGlassLayer.record {
                        drawRect(brush = volumeBrush)
                    }
                    drawLayer(shaderGlassLayer)
                } catch (_: Throwable) {
                    drawRect(brush = volumeBrush)
                }
            } else {
                drawRect(brush = volumeBrush)
            }

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
                            Color.White.copy(alpha = 0.24f * animatedTouchPressed),
                            glowColor.copy(alpha = 0.14f * animatedTouchPressed),
                            Color.Transparent
                        ),
                        center = touchOffset,
                        radius = 84.dp.toPx()
                    ),
                    center = touchOffset,
                    radius = 84.dp.toPx()
                )
            }
        }
    }

    modifier
}
