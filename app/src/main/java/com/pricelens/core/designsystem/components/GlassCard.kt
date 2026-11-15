package com.pricelens.core.designsystem.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
 * Modern frosted glass card with responsive spring physics, dynamic elevation,
 * and specular perimeter bevel.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = PriceLensTheme.glassColors.surfaceGlass,
    borderColor: Color? = null,
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
        targetValue = if (isPressed && onClick != null) 0.972f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "glassCardSpring"
    )

    val currentElevation by animateDpAsState(
        targetValue = if (isPressed && onClick != null) 0.5.dp else elevation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "glassCardElevation"
    )

    val effectiveBorderStroke = when {
        borderBrush != null -> BorderStroke(borderWidth, borderBrush)
        borderColor != null -> BorderStroke(borderWidth, borderColor)
        else -> BorderStroke(borderWidth, PriceLensTheme.glassColors.specularBorderBrush)
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
            .shadow(
                elevation = currentElevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.08f),
                clip = false
            )
            .clip(shape)
            .then(clickableModifier),
        shape = shape,
        color = backgroundColor,
        border = effectiveBorderStroke
    ) {
        Box(content = content)
    }
}
