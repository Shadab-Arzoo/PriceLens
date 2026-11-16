package com.pricelens.core.designsystem.components

import androidx.compose.animation.core.Spring
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
 * Clean modern surface card with 16dp rounded corners, crisp hairline perimeter,
 * subtle elevation, and responsive spring physics.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = PriceLensTheme.glassColors.surfaceGlass,
    borderColor: Color? = null,
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 1.dp,
    showSpecularSheen: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 450f
        ),
        label = "surfaceCardSpring"
    )

    val currentElevation by animateDpAsState(
        targetValue = if (isPressed && onClick != null) 0.dp else elevation,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 450f
        ),
        label = "surfaceCardElevation"
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
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.12f),
                clip = false
            )
            .clip(shape)
            .then(clickableModifier),
        shape = shape,
        color = backgroundColor,
        border = effectiveBorderStroke
    ) {
        Box(contentAlignment = Alignment.TopStart, content = content)
    }
}
