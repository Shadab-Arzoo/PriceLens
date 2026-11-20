package com.pricelens.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pricelens.core.designsystem.PriceLensTheme

/**
 * Animated Shimmer brush that sweeps across placeholder elements to provide
 * silky, fluid skeleton loading states.
 */
@Composable
fun shimmerBrush(
    targetValue: Float = 1200f,
    durationMillis: Int = 2000
): Brush {
    val shimmerColors = listOf(
        PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.05f),
        PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.16f),
        PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.05f)
    )

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation, y = translateAnimation)
    )
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    height: Dp = 16.dp,
    width: Dp? = null,
    brush: Brush? = null
) {
    val effectiveBrush = brush ?: shimmerBrush()
    val boxModifier = if (width != null) {
        modifier.size(width = width, height = height)
    } else {
        modifier.fillMaxWidth().height(height)
    }

    Box(
        modifier = boxModifier
            .clip(shape)
            .background(effectiveBrush)
    )
}

/**
 * Dashboard Skeleton loader displayed smoothly during data fetching.
 * Uses a single unified shimmer transition across all skeleton nodes for optimal performance.
 */
@Composable
fun DashboardSkeleton(
    modifier: Modifier = Modifier
) {
    val sharedShimmer = shimmerBrush()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Action Hero Skeleton
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SkeletonBox(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        height = 42.dp,
                        width = 42.dp,
                        brush = sharedShimmer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        SkeletonBox(height = 16.dp, width = 160.dp, brush = sharedShimmer)
                        Spacer(modifier = Modifier.height(6.dp))
                        SkeletonBox(height = 12.dp, width = 220.dp, brush = sharedShimmer)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                SkeletonBox(height = 44.dp, shape = RoundedCornerShape(12.dp), brush = sharedShimmer)
            }
        }

        // Section Title Skeleton
        SkeletonBox(height = 18.dp, width = 140.dp, brush = sharedShimmer)

        // Cards Skeleton
        repeat(3) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        SkeletonBox(height = 16.dp, width = 180.dp, brush = sharedShimmer)
                        Spacer(modifier = Modifier.height(6.dp))
                        SkeletonBox(height = 12.dp, width = 120.dp, brush = sharedShimmer)
                    }
                    SkeletonBox(height = 20.dp, width = 70.dp, shape = RoundedCornerShape(6.dp), brush = sharedShimmer)
                }
            }
        }
    }
}
