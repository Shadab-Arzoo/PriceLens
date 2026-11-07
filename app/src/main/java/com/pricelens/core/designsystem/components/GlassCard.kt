package com.pricelens.core.designsystem.components

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
 * A layered, translucent card inspired by Apple Liquid Glass.
 *
 * Features specular refraction highlights along curved borders, subtle internal sheen,
 * and tactile spring physics on touch/click.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = PriceLensTheme.glassColors.surfaceGlass,
    borderColor: Color = PriceLensTheme.glassColors.specularBorder,
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 2.dp,
    showSpecularSheen: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 380f
        ),
        label = "glassCardScaleBounce"
    )

    val effectiveBorderStroke = if (borderBrush != null) {
        BorderStroke(borderWidth, borderBrush)
    } else {
        BorderStroke(borderWidth, PriceLensTheme.glassColors.specularBorderBrush)
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(color = PriceLensTheme.glassColors.textPrimary),
            onClick = onClick
        )
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = elevation, shape = shape, clip = false)
            .clip(shape)
            .then(clickableModifier),
        shape = shape,
        color = backgroundColor,
        border = effectiveBorderStroke
    ) {
        Box {
            // Subtle specular glass sheen along top edge
            if (showSpecularSheen) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    PriceLensTheme.glassColors.surfaceHighlight,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
            Box(content = content)
        }
    }
}
