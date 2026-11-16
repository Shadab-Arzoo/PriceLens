package com.pricelens.core.designsystem.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pricelens.core.designsystem.BrandMint
import com.pricelens.core.designsystem.LocalThemeToggle
import com.pricelens.core.designsystem.MintSuccess
import com.pricelens.core.designsystem.PriceLensTheme

@Composable
fun PriceLensTopBar(
    title: String,
    subtitle: String,
    onWatchlistClick: () -> Unit,
    modifier: Modifier = Modifier,
    watchlistCount: Int = 0,
    onToggleTheme: (() -> Unit)? = null,
    onLabsClick: (() -> Unit)? = null,
    isLabsActive: Boolean = false
) {
    val isDark = PriceLensTheme.isDark
    val defaultToggle = LocalThemeToggle.current
    val toggleAction = onToggleTheme ?: defaultToggle

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MintSuccess)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MintSuccess.copy(alpha = 0.12f))
                            .border(1.dp, MintSuccess.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MintSuccess
                        )
                    }
                }
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // PriceLens Labs (Experimental Features) Button
                if (onLabsClick != null) {
                    val labsInteractionSource = remember { MutableInteractionSource() }
                    val isLabsPressed by labsInteractionSource.collectIsPressedAsState()
                    val labsScale by animateFloatAsState(
                        targetValue = if (isLabsPressed) 0.90f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "labsScale"
                    )

                    val infiniteTransition = rememberInfiniteTransition(label = "labsPulse")
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1100, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dotAlpha"
                    )
                    val dotScale by infiniteTransition.animateFloat(
                        initialValue = 0.85f,
                        targetValue = 1.25f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1100, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dotScale"
                    )

                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(
                            onClick = onLabsClick,
                            interactionSource = labsInteractionSource,
                            modifier = Modifier
                                .size(42.dp)
                                .graphicsLayer(scaleX = labsScale, scaleY = labsScale)
                                .clip(CircleShape)
                                .background(PriceLensTheme.glassColors.surfaceGlass)
                                .border(
                                    1.dp,
                                    if (isLabsActive) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.6f)
                                    else PriceLensTheme.glassColors.specularBorder,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "PriceLens Labs",
                                tint = if (isLabsActive) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (isLabsActive) {
                            Box(
                                modifier = Modifier
                                    .offset(x = 2.dp, y = (-2).dp)
                                    .size(8.dp)
                                    .graphicsLayer(scaleX = dotScale, scaleY = dotScale, alpha = dotAlpha)
                                    .clip(CircleShape)
                                    .background(PriceLensTheme.glassColors.ambientGlow)
                            )
                        }
                    }
                }

                // Light Mode / Dark Mode Switch
                ThemeToggleSwitch(
                    isDark = isDark,
                    onToggle = toggleAction
                )

                // Bookmarks Button
                val bookmarkInteractionSource = remember { MutableInteractionSource() }
                val isBookmarkPressed by bookmarkInteractionSource.collectIsPressedAsState()
                val bookmarkScale by animateFloatAsState(
                    targetValue = if (isBookmarkPressed) 0.88f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "bookmarkButtonScale"
                )

                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(
                        onClick = onWatchlistClick,
                        interactionSource = bookmarkInteractionSource,
                        modifier = Modifier
                            .size(42.dp)
                            .graphicsLayer {
                                scaleX = bookmarkScale
                                scaleY = bookmarkScale
                            }
                            .clip(CircleShape)
                            .background(PriceLensTheme.glassColors.surfaceGlass)
                            .border(1.dp, PriceLensTheme.glassColors.specularBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = "Watchlist",
                            tint = PriceLensTheme.glassColors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (watchlistCount > 0) {
                        Box(
                            modifier = Modifier
                                .offset(x = 3.dp, y = (-2).dp)
                                .clip(CircleShape)
                                .background(MintSuccess)
                                .padding(horizontal = if (watchlistCount > 9) 4.dp else 0.dp)
                                .size(if (watchlistCount > 9) 18.dp else 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (watchlistCount > 99) "99+" else watchlistCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF040404),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeToggleSwitch(
    isDark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "themeToggleScale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (isDark) 0f else 180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "themeToggleRotation"
    )

    IconButton(
        onClick = onToggle,
        interactionSource = interactionSource,
        modifier = modifier
            .size(42.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
            .clip(CircleShape)
            .background(PriceLensTheme.glassColors.surfaceGlass)
            .border(1.dp, PriceLensTheme.glassColors.specularBorder, CircleShape)
    ) {
        Crossfade(
            targetState = isDark,
            animationSpec = tween(durationMillis = 240),
            label = "themeIconCrossfade"
        ) { dark ->
            Icon(
                imageVector = if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = if (dark) "Switch to Light Mode" else "Switch to Dark Mode",
                tint = if (dark) BrandMint else Color(0xFFF59E0B),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
