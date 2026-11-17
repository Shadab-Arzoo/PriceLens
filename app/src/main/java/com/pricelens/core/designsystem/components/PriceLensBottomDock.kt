package com.pricelens.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pricelens.core.designsystem.AmberAlert
import com.pricelens.core.designsystem.CyanAccent
import com.pricelens.core.designsystem.MintSuccess
import com.pricelens.core.designsystem.PriceLensTheme

enum class DockTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    EXPLORE("Explore", Icons.Filled.Home, Icons.Outlined.Home),
    SEARCH("Search", Icons.Filled.Search, Icons.Outlined.Search),
    DEALS("Deals", Icons.Filled.LocalOffer, Icons.Outlined.LocalOffer),
    WATCHLIST("Watchlist", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
    TOOLS("Tools", Icons.Filled.Tune, Icons.Outlined.Tune)
}

/**
 * Modern floating liquid-glass dock for primary navigation and rapid feature access.
 * Features smooth spring-driven sliding capsule, ambient under-glow, interactive micro-actions,
 * and tactile press physics.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PriceLensBottomDock(
    selectedTab: DockTab?,
    onTabSelected: (DockTab) -> Unit,
    modifier: Modifier = Modifier,
    onTabReselected: ((DockTab) -> Unit)? = null,
    onTabLongClick: ((DockTab) -> Unit)? = null,
    watchlistCount: Int = 0,
    hasActiveDeals: Boolean = true,
    hasLabsFeature: Boolean = true,
    isVisible: Boolean = true
) {
    val haptic = LocalHapticFeedback.current

    // Infinite breathing pulse for HOT deals badge
    val infiniteTransition = rememberInfiniteTransition(label = "dockDealsHotPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dockHotBadgePulseScale"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = PriceLensTheme.glassColors.surfaceGlass,
                border = BorderStroke(1.2.dp, PriceLensTheme.glassColors.specularBorder),
                shadowElevation = 18.dp,
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.28f),
                        spotColor = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.28f)
                    )
            ) {
                Column {
                    // Specular top rim light highlight
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        PriceLensTheme.glassColors.specularBorder.copy(alpha = 0.5f),
                                        Color.White.copy(alpha = if (PriceLensTheme.isDark) 0.30f else 0.55f),
                                        PriceLensTheme.glassColors.specularBorder.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        val totalWidth = maxWidth
                        val tabCount = DockTab.entries.size
                        val tabWidth = totalWidth / tabCount

                        // 1. Fluid Sliding Indicator Pill
                        val selectedIndex = selectedTab?.let { DockTab.entries.indexOf(it) } ?: -1
                        if (selectedIndex >= 0) {
                            val animatedIndex by animateFloatAsState(
                                targetValue = selectedIndex.toFloat(),
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "dockSlidingPill"
                            )

                            Box(
                                modifier = Modifier
                                    .offset(x = tabWidth * animatedIndex)
                                    .width(tabWidth)
                                    .height(58.dp)
                                    .padding(horizontal = 3.dp, vertical = 2.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.18f),
                                                PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.06f)
                                            )
                                        )
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            Brush.verticalGradient(
                                                listOf(
                                                    PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.45f),
                                                    PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f)
                                                )
                                            )
                                        ),
                                        RoundedCornerShape(22.dp)
                                    )
                            )
                        }

                        // 2. Interactive Tab Items Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DockTab.entries.forEach { tab ->
                                val isSelected = selectedTab == tab
                                val interactionSource = remember { MutableInteractionSource() }
                                val isPressed by interactionSource.collectIsPressedAsState()

                                // Tactile press & selection scale
                                val pressScale by animateFloatAsState(
                                    targetValue = if (isPressed) 0.90f else 1.0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "dockPressScale_${tab.name}"
                                )

                                val iconScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.16f else 1.0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "dockIconScale_${tab.name}"
                                )

                                val tabColor by animateColorAsState(
                                    targetValue = if (isSelected) {
                                        PriceLensTheme.glassColors.ambientGlow
                                    } else {
                                        PriceLensTheme.glassColors.textSecondary
                                    },
                                    label = "dockTabColor_${tab.name}"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .scale(pressScale)
                                        .clip(RoundedCornerShape(20.dp))
                                        .combinedClickable(
                                            interactionSource = interactionSource,
                                            indication = null,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                if (isSelected) {
                                                    onTabReselected?.invoke(tab)
                                                } else {
                                                    onTabSelected(tab)
                                                }
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onTabLongClick?.invoke(tab)
                                            }
                                        )
                                        .padding(vertical = 4.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(contentAlignment = Alignment.TopEnd) {
                                            Icon(
                                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.title,
                                                tint = tabColor,
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .scale(iconScale)
                                            )

                                            // Badges
                                            if (tab == DockTab.WATCHLIST && watchlistCount > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .offset(x = 8.dp, y = (-4).dp)
                                                        .clip(RoundedCornerShape(99.dp))
                                                        .background(MintSuccess)
                                                        .border(1.dp, Color.Black.copy(alpha = 0.25f), RoundedCornerShape(99.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    AnimatedContent(
                                                        targetState = watchlistCount,
                                                        transitionSpec = {
                                                            slideInVertically { height -> height } + fadeIn() togetherWith
                                                                slideOutVertically { height -> -height } + fadeOut()
                                                        },
                                                        label = "watchlistBadgeCount"
                                                    ) { count ->
                                                        Text(
                                                            text = if (count > 99) "99+" else count.toString(),
                                                            color = Color.Black,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            lineHeight = 10.sp
                                                        )
                                                    }
                                                }
                                            } else if (tab == DockTab.DEALS && hasActiveDeals) {
                                                Box(
                                                    modifier = Modifier
                                                        .offset(x = 10.dp, y = (-4).dp)
                                                        .scale(pulseScale)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(AmberAlert)
                                                        .padding(horizontal = 3.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "HOT",
                                                        color = Color.Black,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        lineHeight = 9.sp
                                                    )
                                                }
                                            } else if (tab == DockTab.TOOLS && hasLabsFeature) {
                                                Box(
                                                    modifier = Modifier
                                                        .offset(x = 5.dp, y = (-2).dp)
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(CyanAccent)
                                                        .border(1.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = tab.title,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = tabColor,
                                            maxLines = 1
                                        )

                                        // Active Tab Micro-Pill Glow Indicator
                                        AnimatedVisibility(
                                            visible = isSelected,
                                            enter = fadeIn() + scaleIn(),
                                            exit = fadeOut() + scaleOut()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 2.dp)
                                                    .size(width = 12.dp, height = 2.5.dp)
                                                    .clip(RoundedCornerShape(99.dp))
                                                    .background(PriceLensTheme.glassColors.ambientGlow)
                                            )
                                        }
                                        if (!isSelected) {
                                            Spacer(modifier = Modifier.height(4.5.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
