package com.pricelens.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.catalog.components.LiquidBottomTab
import com.kyant.backdrop.catalog.components.LiquidBottomTabs
import com.pricelens.core.designsystem.AmberAlert
import com.pricelens.core.designsystem.CyanAccent
import com.pricelens.core.designsystem.MintSuccess
import com.pricelens.core.designsystem.PriceLensTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

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
 * Helper object providing pure slide math and index calculation for the dock scrubber gesture.
 */
object DockGestureHelper {
    /**
     * Calculates the rubber-banded display index and clamped target index based on horizontal drag offset.
     */
    fun computeSlideIndices(
        baseIndex: Int,
        dragDeltaX: Float,
        tabWidthPx: Float,
        tabCount: Int
    ): SlideResult {
        if (tabWidthPx <= 0f || tabCount <= 0) {
            val clampedBase = baseIndex.coerceIn(0, (tabCount - 1).coerceAtLeast(0))
            return SlideResult(
                displayIndex = clampedBase.toFloat(),
                hoveredIndex = clampedBase,
                rawIndex = clampedBase.toFloat()
            )
        }

        val rawIndex = baseIndex.toFloat() + (dragDeltaX / tabWidthPx)
        val maxIndex = (tabCount - 1).toFloat()
        val displayIndex = if (rawIndex < 0f) {
            rawIndex * 0.25f
        } else if (rawIndex > maxIndex) {
            maxIndex + (rawIndex - maxIndex) * 0.25f
        } else {
            rawIndex
        }

        val hoveredIndex = rawIndex.roundToInt().coerceIn(0, tabCount - 1)
        return SlideResult(
            displayIndex = displayIndex,
            hoveredIndex = hoveredIndex,
            rawIndex = rawIndex
        )
    }

    data class SlideResult(
        val displayIndex: Float,
        val hoveredIndex: Int,
        val rawIndex: Float
    )
}

/**
 * Modern floating liquid-glass dock for primary navigation and rapid feature access.
 * Features hold-and-slide tab scrubbing with live haptics, spring-driven sliding capsule,
 * ambient under-glow, interactive micro-actions, and tactile press physics.
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
    isVisible: Boolean = true,
    backdrop: Backdrop? = null
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Sliding & scrubber state
    val selectedIndex = selectedTab?.let { DockTab.entries.indexOf(it) } ?: -1
    var isSliding by remember { mutableStateOf(false) }
    var isHoldingActiveTab by remember { mutableStateOf(false) }
    var dragDisplayIndex by remember { mutableFloatStateOf(0f) }
    var currentHoveredIndex by remember { mutableIntStateOf(if (selectedIndex >= 0) selectedIndex else 0) }

    val pillAnimatable = remember {
        Animatable(if (selectedIndex >= 0) selectedIndex.toFloat() else 0f)
    }

    // Keep pill synchronized with external route/tab updates when not actively dragging
    LaunchedEffect(selectedIndex) {
        if (!isSliding && selectedIndex >= 0) {
            currentHoveredIndex = selectedIndex
            pillAnimatable.animateTo(
                targetValue = selectedIndex.toFloat(),
                animationSpec = spring(
                    dampingRatio = 0.85f,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    val isCupertino = PriceLensTheme.isCupertino
    val isDark = PriceLensTheme.isDark

    if (isCupertino) {
        val effectiveBackdrop = backdrop ?: rememberLayerBackdrop()
        val currentTabIndex = if (selectedIndex >= 0) selectedIndex else 0
        val tabsCount = DockTab.entries.size

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            LiquidBottomTabs(
                selectedTabIndex = { currentTabIndex },
                onTabSelected = { index ->
                    val tab = DockTab.entries.getOrNull(index)
                    if (tab != null) {
                        if (tab == selectedTab) {
                            onTabReselected?.invoke(tab)
                        } else {
                            onTabSelected(tab)
                        }
                    }
                },
                backdrop = effectiveBackdrop,
                tabsCount = tabsCount,
                modifier = Modifier.fillMaxWidth()
            ) {
                DockTab.entries.forEachIndexed { index, tab ->
                    val isSelected = selectedIndex == index
                    LiquidBottomTab(
                        onClick = {
                            if (isSelected) {
                                onTabReselected?.invoke(tab)
                            } else {
                                onTabSelected(tab)
                            }
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            if (tab == DockTab.WATCHLIST && watchlistCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-4).dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(PriceLensTheme.glassColors.ambientGlow)
                                )
                            }
                        }
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textTertiary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    } else {
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
            contentAlignment = if (isCupertino) Alignment.BottomCenter else Alignment.Center
        ) {
            Surface(
                shape = if (isCupertino) {
                    RoundedCornerShape(0.dp) // Locked flush to screen edges like actual iOS
                } else {
                    RoundedCornerShape(30.dp)
                },
                color = if (isCupertino) {
                    if (!isDark) Color(0xFFF9F9F9).copy(alpha = 0.88f) else Color(0xFF161618).copy(alpha = 0.88f)
                } else {
                    PriceLensTheme.glassColors.surfaceGlass
                },
                border = if (isCupertino) {
                    BorderStroke(0.5.dp, if (isDark) Color(0x38FFFFFF) else Color(0x29000000))
                } else {
                    BorderStroke(1.2.dp, PriceLensTheme.glassColors.specularBorderBrush)
                },
                shadowElevation = if (isCupertino) 0.dp else 20.dp,
                modifier = if (isCupertino) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier
                        .widthIn(max = 440.dp)
                        .shadow(
                            elevation = 28.dp,
                            shape = RoundedCornerShape(30.dp),
                            ambientColor = PriceLensTheme.glassColors.ambientGlow.copy(alpha = if (isDark) 0.35f else 0.18f),
                            spotColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.12f)
                        )
                }
            ) {
                Box {
                    if (!isCupertino) {
                        // Inner meniscus glass volume gradient (for non-Apple themes)
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        0.0f to Color.White.copy(alpha = if (isDark) 0.09f else 0.20f),
                                        0.35f to Color.Transparent,
                                        1.0f to Color.Black.copy(alpha = if (isDark) 0.08f else 0.03f)
                                    )
                                )
                        )
                    }

                    Column(
                        modifier = if (isCupertino) {
                            Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding() // Translucent glass locks to bottom bezel while tabs sit above home indicator
                        } else {
                            Modifier
                        }
                    ) {
                        // Specular top rim light highlight / iOS hairline separator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isCupertino) 0.5.dp else 1.5.dp)
                                .background(
                                    if (isCupertino) {
                                        SolidColor(if (isDark) Color(0x38FFFFFF) else Color(0x29000000))
                                    } else {
                                        PriceLensTheme.glassColors.specularTopGlintBrush
                                    }
                                )
                        )

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = if (isCupertino) 0.dp else 6.dp,
                                vertical = if (isCupertino) 2.dp else 6.dp
                            )
                    ) {
                        val totalWidth = maxWidth
                        val tabCount = DockTab.entries.size
                        val tabWidth = totalWidth / tabCount
                        val tabWidthPx = with(density) { tabWidth.toPx() }

                        // 1. Fluid Sliding Indicator Pill with Hold-and-Slide support (iOS Liquid Glass Lens Capsule when in iOS mode)
                        if (selectedIndex >= 0 && !isCupertino) {
                            val pillScale by animateFloatAsState(
                                targetValue = when {
                                    isSliding -> 1.05f
                                    isHoldingActiveTab -> 0.94f
                                    else -> 1.0f
                                },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "dockPillScale"
                            )

                            val pillGlowAlpha by animateFloatAsState(
                                targetValue = if (isSliding) 0.30f else 0.18f,
                                label = "dockPillGlowAlpha"
                            )

                            val pillBorderAlpha by animateFloatAsState(
                                targetValue = if (isSliding) 0.65f else 0.45f,
                                label = "dockPillBorderAlpha"
                            )

                            Box(
                                modifier = Modifier
                                    .offset {
                                        val currentPillIndex = if (isSliding) dragDisplayIndex else pillAnimatable.value
                                        IntOffset((tabWidthPx * currentPillIndex).roundToInt(), 0)
                                    }
                                    .width(tabWidth)
                                    .height(58.dp)
                                    .graphicsLayer {
                                        scaleX = pillScale
                                        scaleY = pillScale
                                    }
                                    .padding(horizontal = 3.dp, vertical = 2.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                PriceLensTheme.glassColors.ambientGlow.copy(alpha = pillGlowAlpha),
                                                PriceLensTheme.glassColors.ambientGlow.copy(alpha = pillGlowAlpha * 0.35f)
                                            )
                                        )
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            Brush.verticalGradient(
                                                listOf(
                                                    PriceLensTheme.glassColors.ambientGlow.copy(alpha = pillBorderAlpha),
                                                    PriceLensTheme.glassColors.ambientGlow.copy(alpha = pillBorderAlpha * 0.33f)
                                                )
                                            )
                                        ),
                                        RoundedCornerShape(22.dp)
                                    )
                            )
                        } else if (selectedIndex >= 0 && isCupertino) {
                            // iOS Liquid Glass Sliding Capsule Lens (inspired by Kyant0 LiquidBottomTabs)
                            val iosPillScaleX by animateFloatAsState(
                                targetValue = when {
                                    isSliding -> 1.06f
                                    isHoldingActiveTab -> 0.95f
                                    else -> 1.0f
                                },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "dockIosPillScaleX"
                            )

                            val iosPillScaleY by animateFloatAsState(
                                targetValue = when {
                                    isSliding -> 0.96f
                                    isHoldingActiveTab -> 0.95f
                                    else -> 1.0f
                                },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "dockIosPillScaleY"
                            )

                            Box(
                                modifier = Modifier
                                    .offset {
                                        val currentPillIndex = if (isSliding) dragDisplayIndex else pillAnimatable.value
                                        IntOffset((tabWidthPx * currentPillIndex).roundToInt(), 0)
                                    }
                                    .width(tabWidth)
                                    .height(if (isCupertino) 48.dp else 58.dp)
                                    .graphicsLayer {
                                        scaleX = iosPillScaleX
                                        scaleY = iosPillScaleY
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                if (!isDark) Color.Black.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.16f),
                                                if (!isDark) Color.Black.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.09f)
                                            )
                                        )
                                    )
                                    .border(
                                        BorderStroke(
                                            0.75.dp,
                                            Brush.verticalGradient(
                                                listOf(
                                                    if (!isDark) Color.White.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.28f),
                                                    if (!isDark) Color.Black.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.08f)
                                                )
                                            )
                                        ),
                                        RoundedCornerShape(9999.dp)
                                    )
                            )
                        }

                        // 2. Interactive Tab Items Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isCupertino) 50.dp else 58.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DockTab.entries.forEachIndexed { index, tab ->
                                val isSelected = selectedTab == tab
                                val isHighlighted = if (isSliding) (index == currentHoveredIndex) else isSelected
                                val interactionSource = remember { MutableInteractionSource() }
                                val isPressed by interactionSource.collectIsPressedAsState()

                                // Tactile press & selection scale
                                val pressScale by animateFloatAsState(
                                    targetValue = if (isPressed || (isSelected && isHoldingActiveTab && !isSliding)) {
                                        if (isCupertino) 0.94f else 0.90f
                                    } else 1.0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "dockPressScale_${tab.name}"
                                )

                                val iconScale by animateFloatAsState(
                                    targetValue = if (isHighlighted) {
                                        if (isCupertino) 1.12f else 1.16f
                                    } else 1.0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "dockIconScale_${tab.name}"
                                )

                                val tabColor by animateColorAsState(
                                    targetValue = if (isHighlighted) {
                                        if (isCupertino) {
                                            if (isDark) Color(0xFF0091FF) else Color(0xFF0088FF)
                                        } else {
                                            PriceLensTheme.glassColors.ambientGlow
                                        }
                                    } else {
                                        if (isCupertino) {
                                            if (isDark) Color(0xFF98989D) else Color(0xFF8E8E93)
                                        } else {
                                            PriceLensTheme.glassColors.textSecondary
                                        }
                                    },
                                    label = "dockTabColor_${tab.name}"
                                )

                                val tabModifier = Modifier
                                    .weight(1f)
                                    .graphicsLayer {
                                        scaleX = pressScale
                                        scaleY = pressScale
                                    }
                                    .clip(RoundedCornerShape(20.dp))
                                    .pointerInput(tab, tabWidth) {
                                        val tabWidthPx = if (size.width > 0) size.width.toFloat() else with(density) { tabWidth.toPx() }
                                        val touchSlop = viewConfiguration.touchSlop
                                        val longPressTimeout = viewConfiguration.longPressTimeoutMillis

                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false)
                                            val pointerId = down.id
                                            val downTime = System.currentTimeMillis()
                                            var totalDragX = 0f
                                            var isDraggingLocal = false

                                            if (tab == selectedTab) {
                                                isHoldingActiveTab = true
                                            }

                                            while (true) {
                                                val event = awaitPointerEvent()
                                                val change = event.changes.firstOrNull { it.id == pointerId }
                                                if (change == null || !change.pressed) {
                                                    break
                                                }

                                                val deltaX = change.position.x - down.position.x
                                                val deltaY = change.position.y - down.position.y

                                                if (!isDraggingLocal) {
                                                    if (abs(deltaX) > touchSlop && abs(deltaX) >= abs(deltaY)) {
                                                        isDraggingLocal = true
                                                        isSliding = true
                                                        currentHoveredIndex = index
                                                        totalDragX = deltaX

                                                        val result = DockGestureHelper.computeSlideIndices(
                                                            baseIndex = index,
                                                            dragDeltaX = totalDragX,
                                                            tabWidthPx = tabWidthPx,
                                                            tabCount = tabCount
                                                        )
                                                        dragDisplayIndex = result.displayIndex
                                                        if (result.hoveredIndex != currentHoveredIndex) {
                                                            currentHoveredIndex = result.hoveredIndex
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        }
                                                        change.consume()
                                                    } else if (abs(deltaY) > touchSlop && abs(deltaY) > abs(deltaX)) {
                                                        // Vertical gesture disambiguation - abort gesture tracking cleanly
                                                        break
                                                    }
                                                } else {
                                                    totalDragX = deltaX
                                                    val result = DockGestureHelper.computeSlideIndices(
                                                        baseIndex = index,
                                                        dragDeltaX = totalDragX,
                                                        tabWidthPx = tabWidthPx,
                                                        tabCount = tabCount
                                                    )
                                                    dragDisplayIndex = result.displayIndex
                                                    if (result.hoveredIndex != currentHoveredIndex) {
                                                        currentHoveredIndex = result.hoveredIndex
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    }
                                                    change.consume()
                                                }
                                            }

                                            isHoldingActiveTab = false

                                            if (isDraggingLocal) {
                                                isSliding = false
                                                val targetTab = DockTab.entries[currentHoveredIndex]
                                                if (targetTab != selectedTab) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    onTabSelected(targetTab)
                                                } else {
                                                    onTabReselected?.invoke(targetTab)
                                                }
                                                val finalIndex = currentHoveredIndex.toFloat()
                                                coroutineScope.launch {
                                                    pillAnimatable.snapTo(dragDisplayIndex)
                                                    pillAnimatable.animateTo(
                                                        targetValue = finalIndex,
                                                        animationSpec = spring(
                                                            dampingRatio = 0.85f,
                                                            stiffness = Spring.StiffnessMedium
                                                        )
                                                    )
                                                }
                                            } else {
                                                val elapsed = System.currentTimeMillis() - downTime
                                                if (tab == selectedTab) {
                                                    if (elapsed >= longPressTimeout && onTabLongClick != null) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        onTabLongClick.invoke(tab)
                                                    } else {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        onTabReselected?.invoke(tab)
                                                    }
                                                } else {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    onTabSelected(tab)
                                                }
                                            }
                                        }
                                    }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)

                                Box(
                                    modifier = tabModifier,
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(contentAlignment = Alignment.TopEnd) {
                                            Icon(
                                                imageVector = if (isHighlighted) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.title,
                                                tint = tabColor,
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .graphicsLayer {
                                                        scaleX = iconScale
                                                        scaleY = iconScale
                                                    }
                                            )

                                            // Badges
                                            if (tab == DockTab.WATCHLIST && watchlistCount > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .offset(x = 8.dp, y = (-4).dp)
                                                        .clip(RoundedCornerShape(99.dp))
                                                        .background(if (isCupertino) Color(0xFFFF3B30) else PriceLensTheme.glassColors.ambientGlow)
                                                        .border(
                                                            1.dp,
                                                            if (isCupertino) (if (isDark) Color(0xFF1C1C1E) else Color.White) else Color.Black.copy(alpha = 0.25f),
                                                            RoundedCornerShape(99.dp)
                                                        )
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
                                                            color = if (isCupertino) Color.White else Color.Black,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            lineHeight = 10.sp
                                                        )
                                                    }
                                                }
                                            } else if (tab == DockTab.DEALS && hasActiveDeals) {
                                                HotDealsBadge(modifier = Modifier.offset(x = 10.dp, y = (-4).dp), isCupertino = isCupertino)
                                            } else if (tab == DockTab.TOOLS && hasLabsFeature) {
                                                Box(
                                                    modifier = Modifier
                                                        .offset(x = 5.dp, y = (-2).dp)
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isCupertino) (if (isDark) Color(0xFF0091FF) else Color(0xFF0088FF)) else PriceLensTheme.glassColors.ambientGlow)
                                                        .border(1.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = tab.title,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                                            color = tabColor,
                                            maxLines = 1
                                        )

                                        // Active Tab Micro-Pill Indicator (omitted in Apple mode for native iOS Tab Bar HIG fidelity)
                                        if (!isCupertino) {
                                            AnimatedVisibility(
                                                visible = isHighlighted,
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
                                        }
                                        if (!isHighlighted || isCupertino) {
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
}
}

@Composable
private fun HotDealsBadge(modifier: Modifier = Modifier, isCupertino: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "dockDealsHotPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (isCupertino) 1f else 0.94f,
        targetValue = if (isCupertino) 1f else 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dockHotBadgePulseScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
            .clip(RoundedCornerShape(6.dp))
            .background(if (isCupertino) Color(0xFFFF3B30) else AmberAlert)
            .padding(horizontal = 3.dp, vertical = 1.dp)
    ) {
        Text(
            text = "HOT",
            color = if (isCupertino) Color.White else Color.Black,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 9.sp
        )
    }
}
