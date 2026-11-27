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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.pricelens.core.designsystem.LocalThemeToggle
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
    val isCupertino = PriceLensTheme.isCupertino
    val canvasBackdrop = PriceLensTheme.canvasBackdrop
    val effectIntensity = if (isCupertino) PriceLensTheme.iosGlassIntensity else 1.0f
    val surfaceGlassColor = PriceLensTheme.glassColors.surfaceGlass
    val defaultToggle = LocalThemeToggle.current
    val toggleAction = onToggleTheme ?: defaultToggle

    val glassCircleModifier: Modifier = rememberCupertinoCircleGlassModifier(
        isCupertino = isCupertino,
        isDark = isDark,
        canvasBackdrop = canvasBackdrop,
        effectIntensity = effectIntensity,
        surfaceColor = surfaceGlassColor
    )

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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PriceLensTheme.glassColors.ambientGlow)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.12f))
                            .border(1.dp, PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.ambientGlow,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = PriceLensTheme.glassColors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                .graphicsLayer {
                                    scaleX = labsScale
                                    scaleY = labsScale
                                }
                                .clip(CircleShape)
                                .then(glassCircleModifier)
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
                                    .offset(x = (-1).dp, y = 1.dp)
                                    .size(8.dp)
                                    .graphicsLayer {
                                        scaleX = dotScale
                                        scaleY = dotScale
                                        alpha = dotAlpha
                                    }
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
                            .then(glassCircleModifier)
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
                                .defaultMinSize(minWidth = 17.dp, minHeight = 17.dp)
                                .clip(CircleShape)
                                .background(PriceLensTheme.glassColors.ambientGlow)
                                .padding(horizontal = if (watchlistCount > 9) 4.dp else 0.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (watchlistCount > 99) "99+" else watchlistCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF040404),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun rememberCupertinoCircleGlassModifier(
    isCupertino: Boolean = PriceLensTheme.isCupertino,
    isDark: Boolean = PriceLensTheme.isDark,
    canvasBackdrop: com.kyant.backdrop.Backdrop? = PriceLensTheme.canvasBackdrop,
    effectIntensity: Float = if (isCupertino) PriceLensTheme.iosGlassIntensity else 1.0f,
    surfaceColor: Color = PriceLensTheme.glassColors.surfaceGlass
): Modifier {
    return if (isCupertino && canvasBackdrop != null) {
        Modifier.drawBackdrop(
            backdrop = canvasBackdrop,
            shape = { CircleShape },
            effects = {
                vibrancy()
                blur((6f * effectIntensity).dp.toPx())
                lens(
                    refractionHeight = (8f * effectIntensity).dp.toPx(),
                    refractionAmount = (12f * effectIntensity).dp.toPx(),
                    depthEffect = true,
                    chromaticAberration = true
                )
            },
            highlight = {
                Highlight.Default.copy(
                    width = 0.75.dp,
                    alpha = if (isDark) 0.60f else 0.82f
                )
            },
            onDrawSurface = {
                drawRect(surfaceColor)
            }
        )
    } else {
        Modifier.background(surfaceColor)
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

    val circleGlassModifier = rememberCupertinoCircleGlassModifier(
        isCupertino = PriceLensTheme.isCupertino,
        isDark = isDark,
        canvasBackdrop = PriceLensTheme.canvasBackdrop,
        effectIntensity = if (PriceLensTheme.isCupertino) PriceLensTheme.iosGlassIntensity else 1.0f,
        surfaceColor = PriceLensTheme.glassColors.surfaceGlass
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
            .then(circleGlassModifier)
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
                tint = if (dark) PriceLensTheme.glassColors.ambientGlow else Color(0xFFF59E0B),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Shared pinned top-bar container with Liquid Glass backdrop in iOS mode
 * and a clean subtle translucent scrim + hairline divider in normal mode.
 */
@Composable
fun PinnedHeaderSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isCupertino = PriceLensTheme.isCupertino
    val headerBackdrop = if (isCupertino) PriceLensTheme.canvasBackdrop else null
    val effectIntensity = if (isCupertino) PriceLensTheme.iosGlassIntensity else 1.0f
    val scrimColor = PriceLensTheme.glassColors.headerScrim

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (headerBackdrop != null) {
                    Modifier.drawBackdrop(
                        backdrop = headerBackdrop,
                        shape = { RectangleShape },
                        effects = {
                            vibrancy()
                            blur((12f * effectIntensity).dp.toPx())
                        },
                        highlight = null,
                        shadow = null,
                        onDrawSurface = { drawRect(scrimColor) }
                    )
                } else {
                    Modifier
                }
            ),
        color = if (headerBackdrop != null) Color.Transparent else scrimColor
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
            HorizontalDivider(
                thickness = if (isCupertino) 0.75.dp else 0.5.dp,
                color = PriceLensTheme.glassColors.headerDivider
            )
        }
    }
}
