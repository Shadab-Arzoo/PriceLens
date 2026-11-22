package com.pricelens.presentation.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.pricelens.R
import com.pricelens.core.designsystem.LocalThemeToggle
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.designsystem.components.LiquidGlassPrimaryButton
import com.pricelens.core.designsystem.components.ThemeToggleSwitch
import com.pricelens.core.util.rememberInrCurrencyFormatter
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 4 })

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = PriceLensTheme.glassColors.backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar with Brand, Dark/Light Switch & Skip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(PriceLensTheme.glassColors.ambientGlow, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PriceLens",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeToggleSwitch(
                        isDark = PriceLensTheme.isDark,
                        onToggle = LocalThemeToggle.current
                    )

                    TextButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onFinishOnboarding()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_skip),
                            style = MaterialTheme.typography.labelLarge,
                            color = PriceLensTheme.glassColors.textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Pager for the 4 Feature Slides
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> OnboardingSlide1()
                    1 -> OnboardingSlide2()
                    2 -> OnboardingSlide3()
                    3 -> OnboardingSlide4(
                        hasNotificationPermission = hasNotificationPermission,
                        onRequestNotifications = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                }
            }

            // Bottom Navigation Area (Interactive Dots + Continue/Get Started Button)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive Clickable Page Indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 28.dp else 8.dp,
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
                            label = "indicatorWidth"
                        )
                        val color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val isLastPage = pagerState.currentPage == 3
                LiquidGlassPrimaryButton(
                    text = if (isLastPage) stringResource(R.string.onboarding_get_started)
                    else stringResource(R.string.onboarding_continue),
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (isLastPage) {
                            onFinishOnboarding()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * Interactive 3D Hero Card with touch drag tilt physics
 */
@Composable
private fun InteractiveHeroCard(
    illustrationRes: Int,
    contentDescription: String,
    hintText: String = "Drag to tilt 3D lens",
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var isTouching by remember { mutableStateOf(false) }

    val animatedRotY by animateFloatAsState(
        targetValue = if (isTouching) (dragX / 12f).coerceIn(-15f, 15f) else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
        label = "heroRotY"
    )
    val animatedRotX by animateFloatAsState(
        targetValue = if (isTouching) (-dragY / 12f).coerceIn(-15f, 15f) else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
        label = "heroRotX"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isTouching) 1.03f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "heroScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "heroIdleFloat")
    val idleOffsetY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idleOffsetY"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .graphicsLayer {
                translationY = (if (isTouching) 0f else idleOffsetY) * density
                rotationX = animatedRotX
                rotationY = animatedRotY
                scaleX = animatedScale
                scaleY = animatedScale
                cameraDistance = 18f * density
            }
            .clip(RoundedCornerShape(22.dp))
            .background(PriceLensTheme.glassColors.surfaceGlass)
            .border(1.dp, PriceLensTheme.glassColors.specularBorder, RoundedCornerShape(22.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isTouching = true
                        dragX = offset.x - size.width / 2f
                        dragY = offset.y - size.height / 2f
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragX += dragAmount.x
                        dragY += dragAmount.y
                    },
                    onDragEnd = {
                        isTouching = false
                        dragX = 0f
                        dragY = 0f
                    },
                    onDragCancel = {
                        isTouching = false
                        dragX = 0f
                        dragY = 0f
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(illustrationRes),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentScale = ContentScale.Fit
        )

        // Subtle interactive hint badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isTouching) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.25f)
                    else PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.85f)
                )
                .border(
                    0.5.dp,
                    if (isTouching) PriceLensTheme.glassColors.ambientGlow
                    else PriceLensTheme.glassColors.specularBorder.copy(alpha = 0.5f),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isTouching) "✦ 3D Perspective Active" else "✦ $hintText",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = if (isTouching) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// File-level types for interactive onboarding demos
private data class MockDeal(
    val name: String,
    val dotColor: Color,
    val regularPrice: Int,
    val originalPrice: Int,
    val flashPrice: Int,
    val badge: String,
    val deliveryNote: String,
    val isLowest: Boolean = false
)

private enum class InspectorTarget {
    NONE, TITLE, SIZES, PRICE
}

/**
 * Slide 1: Welcome & Interactive Multi-Store Price Comparison Simulator
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnboardingSlide1() {
    val haptic = LocalHapticFeedback.current
    val currencyFormatter = rememberInrCurrencyFormatter()

    val stores = remember {
        listOf(
            MockDeal("Flipkart", Color(0xFF2874F0), 18499, 24900, 16499, "Lowest Price Guaranteed", "Free Delivery • In Stock", true),
            MockDeal("Amazon", Color(0xFFFF9900), 19990, 24900, 17990, "Prime 1-Day", "Amazon Fulfilled • In Stock", false),
            MockDeal("Croma", Color(0xFF00B0B9), 20490, 24900, 18490, "Store Pickup", "Ready in 3 hours at store", false),
            MockDeal("Reliance", Color(0xFFE42529), 19499, 24900, 17499, "Bank Discount", "Instant ₹1,500 off on Cards", false),
            MockDeal("Vijay Sales", Color(0xFFE31E24), 19890, 24900, 17890, "Exchange Bonus", "Extra ₹1,000 on exchange", false)
        )
    }

    var selectedStoreIndex by remember { mutableIntStateOf(0) }
    var isFlashSaleSimulated by remember { mutableStateOf(false) }

    val activeDeal = stores[selectedStoreIndex]
    val activePrice = if (isFlashSaleSimulated) activeDeal.flashPrice else activeDeal.regularPrice
    val savings = activeDeal.originalPrice - activePrice
    val savingsPercent = ((savings.toDouble() / activeDeal.originalPrice) * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Image Illustration Card with 3D Tilt
        InteractiveHeroCard(
            illustrationRes = R.drawable.illustration_intro_compare,
            contentDescription = "Price Comparison Illustration",
            hintText = "Drag to tilt 3D card"
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.intro_slide1_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PriceLensTheme.glassColors.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tap any store below or simulate a live price drop to see PriceLens compare prices in real time.",
                style = MaterialTheme.typography.bodyMedium,
                color = PriceLensTheme.glassColors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        // Interactive Store Chips Selector
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            stores.forEachIndexed { index, store ->
                val isSelected = selectedStoreIndex == index
                val chipGlow = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder
                val chipBg = if (isSelected) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f)
                else PriceLensTheme.glassColors.surfaceHighlight

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(chipBg)
                        .border(if (isSelected) 1.5.dp else 1.dp, chipGlow, RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedStoreIndex = index
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(store.dotColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = store.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                    if (store.isLowest && !isFlashSaleSimulated) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "★",
                            fontSize = 11.sp,
                            color = Color(0xFF34C759)
                        )
                    }
                }
            }
        }

        // Interactive Deal Card Preview
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(activeDeal.dotColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activeDeal.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textPrimary
                        )
                    }

                    // Lowest Price or Deal Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isFlashSaleSimulated) Color(0xFFFF9500).copy(alpha = 0.18f)
                                else if (activeDeal.isLowest) Color(0xFF34C759).copy(alpha = 0.18f)
                                else PriceLensTheme.glassColors.surfaceHighlight
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isFlashSaleSimulated) "⚡ Live Flash Sale"
                            else if (activeDeal.isLowest) "🔥 Lowest Price"
                            else activeDeal.badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isFlashSaleSimulated) Color(0xFFFF9500)
                            else if (activeDeal.isLowest) Color(0xFF34C759)
                            else PriceLensTheme.glassColors.textSecondary
                        )
                    }
                }

                Text(
                    text = "Apple AirPods Pro (2nd Gen) with MagSafe",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PriceLensTheme.glassColors.textPrimary
                )

                // Price display with animated transition
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimatedContent(
                        targetState = activePrice,
                        label = "priceAnimation"
                    ) { price ->
                        Text(
                            text = currencyFormatter.format(price),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isFlashSaleSimulated) Color(0xFF34C759) else PriceLensTheme.glassColors.textPrimary
                        )
                    }

                    Text(
                        text = currencyFormatter.format(activeDeal.originalPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PriceLensTheme.glassColors.textSecondary,
                        textDecoration = TextDecoration.LineThrough
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF34C759).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Save ₹$savings ($savingsPercent% OFF)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34C759)
                        )
                    }
                }

                Text(
                    text = activeDeal.deliveryNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )

                // Interactive Action Button inside the card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isFlashSaleSimulated) PriceLensTheme.glassColors.surfaceHighlight
                            else PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f)
                        )
                        .border(
                            1.dp,
                            if (isFlashSaleSimulated) PriceLensTheme.glassColors.specularBorder
                            else PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.6f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isFlashSaleSimulated = !isFlashSaleSimulated
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isFlashSaleSimulated) Icons.Default.Refresh else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isFlashSaleSimulated) PriceLensTheme.glassColors.textPrimary else PriceLensTheme.glassColors.ambientGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFlashSaleSimulated) "↺ Reset Simulation" else "⚡ Simulate Live Price Drop (-₹2,000)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isFlashSaleSimulated) PriceLensTheme.glassColors.textPrimary else PriceLensTheme.glassColors.ambientGlow
                    )
                }
            }
        }
    }
}

/**
 * Slide 2: Universal Link Tracking & Interactive Tap Inspector Simulator
 */
@Composable
private fun OnboardingSlide2() {
    val haptic = LocalHapticFeedback.current

    var selectedTarget by remember { mutableStateOf(InspectorTarget.NONE) }
    var selectedSize by remember { mutableStateOf("US 9") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Image Illustration Card with 3D Tilt
        InteractiveHeroCard(
            illustrationRes = R.drawable.illustration_intro_universal,
            contentDescription = "Universal Price Tracking Illustration",
            hintText = "Drag to tilt 3D card"
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.intro_slide2_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PriceLensTheme.glassColors.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Try the Tap Inspector below: tap directly on the price tag in this mock web store to lock and track it!",
                style = MaterialTheme.typography.bodyMedium,
                color = PriceLensTheme.glassColors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        // Mock Web Store Viewport Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mock Browser URL Address Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PriceLensTheme.glassColors.surfaceHighlight)
                        .border(1.dp, PriceLensTheme.glassColors.specularBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = PriceLensTheme.glassColors.ambientGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "store.nike.com/in/air-max-pulse",
                        style = MaterialTheme.typography.bodySmall,
                        color = PriceLensTheme.glassColors.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = PriceLensTheme.glassColors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Interactive Web Store Elements
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Product Title (Tappable Element)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedTarget == InspectorTarget.TITLE) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .border(
                                1.dp,
                                if (selectedTarget == InspectorTarget.TITLE) PriceLensTheme.glassColors.ambientGlow
                                else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedTarget = InspectorTarget.TITLE
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Column {
                            Text(
                                text = "Nike Air Max Pulse - Black / Volt",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.textPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB800),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "4.8 (1,420 verified buyer ratings)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PriceLensTheme.glassColors.textSecondary
                                )
                            }
                        }
                    }

                    // Size selector row (Tappable Element)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedTarget == InspectorTarget.SIZES) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .border(
                                1.dp,
                                if (selectedTarget == InspectorTarget.SIZES) PriceLensTheme.glassColors.ambientGlow
                                else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("US 8", "US 9", "US 10", "US 11").forEach { size ->
                            val isChosen = selectedSize == size
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isChosen) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.2f)
                                        else PriceLensTheme.glassColors.surfaceHighlight
                                    )
                                    .border(
                                        1.dp,
                                        if (isChosen) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedSize = size
                                        selectedTarget = InspectorTarget.SIZES
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = size,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isChosen) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textSecondary
                                )
                            }
                        }
                    }

                    // Tappable Price Tag Target (The Star of the Simulator!)
                    val isPriceLocked = selectedTarget == InspectorTarget.PRICE
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isPriceLocked) Color(0xFF34C759).copy(alpha = 0.15f)
                                else PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.10f)
                            )
                            .border(
                                if (isPriceLocked) 2.dp else 1.5.dp,
                                if (isPriceLocked) Color(0xFF34C759) else PriceLensTheme.glassColors.ambientGlow,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedTarget = InspectorTarget.PRICE
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "₹9,995",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isPriceLocked) Color(0xFF34C759) else PriceLensTheme.glassColors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "₹13,995",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PriceLensTheme.glassColors.textSecondary,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isPriceLocked) Color(0xFF34C759) else PriceLensTheme.glassColors.ambientGlow
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isPriceLocked) "✓ TARGET LOCKED" else "👉 TAP TO TRACK",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPriceLocked) Color.White else (if (PriceLensTheme.isDark) Color.Black else Color.White)
                                )
                            }
                        }
                    }
                }

                // Interactive Feedback Banner
                AnimatedVisibility(
                    visible = selectedTarget != InspectorTarget.NONE,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    when (selectedTarget) {
                        InspectorTarget.PRICE -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF34C759).copy(alpha = 0.12f))
                                    .border(1.dp, Color(0xFF34C759).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF34C759),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Price Target Successfully Linked!",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34C759)
                                    )
                                    Text(
                                        text = "Selector: .product-price-current • Checks prices every 4 hours automatically.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = PriceLensTheme.glassColors.textSecondary
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedTarget = InspectorTarget.NONE
                                    }
                                ) {
                                    Text(
                                        text = "Reset",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PriceLensTheme.glassColors.ambientGlow
                                    )
                                }
                            }
                        }
                        InspectorTarget.TITLE, InspectorTarget.SIZES -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFF9500).copy(alpha = 0.12f))
                                    .border(1.dp, Color(0xFFFF9500).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9500),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "That's product metadata! Tap the highlighted Price Tag (₹9,995) to track prices.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                            }
                        }
                        InspectorTarget.NONE -> Unit
                    }
                }
            }
        }
    }
}

/**
 * Slide 3: Price Drop Alerts & Interactive Target Slider Sandbox
 */
@Composable
private fun OnboardingSlide3() {
    val haptic = LocalHapticFeedback.current
    val currencyFormatter = rememberInrCurrencyFormatter()

    val basePrice = 26990f
    var targetPrice by remember { mutableFloatStateOf(22990f) }
    var showMockNotification by remember { mutableStateOf(false) }

    val savings = (basePrice - targetPrice).toInt()
    val dropPercent = ((savings / basePrice) * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Image Illustration Card with 3D Tilt
        InteractiveHeroCard(
            illustrationRes = R.drawable.illustration_intro_alerts,
            contentDescription = "Price Drop Alerts Illustration",
            hintText = "Drag to tilt 3D card"
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.intro_slide3_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PriceLensTheme.glassColors.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Drag the interactive slider to set your target price, then tap 'Simulate Alert' to test background notifications.",
                style = MaterialTheme.typography.bodyMedium,
                color = PriceLensTheme.glassColors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        // Mock Notification Pop-Down Banner
        AnimatedVisibility(
            visible = showMockNotification,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it }
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showMockNotification = false
                    },
                shape = RoundedCornerShape(16.dp),
                borderColor = Color(0xFF34C759)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34C759).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFF34C759),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "PriceLens • Just now",
                                style = MaterialTheme.typography.labelSmall,
                                color = PriceLensTheme.glassColors.textSecondary
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = PriceLensTheme.glassColors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "🔔 Target Reached: Sony WH-1000XM5",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textPrimary
                        )
                        Text(
                            text = "Price dropped to ₹${targetPrice.toInt() - 500}! That is ₹500 below your target. Tap to view deal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PriceLensTheme.glassColors.textSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Interactive Target Slider Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Sony WH-1000XM5 ANC",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textPrimary
                        )
                        Text(
                            text = "Current Retail Price: ${currencyFormatter.format(basePrice.toInt())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PriceLensTheme.glassColors.textSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "-$dropPercent%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PriceLensTheme.glassColors.ambientGlow
                        )
                    }
                }

                // Target Price Headline
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Your Target Price Alert",
                            style = MaterialTheme.typography.labelSmall,
                            color = PriceLensTheme.glassColors.textSecondary
                        )
                        Text(
                            text = currencyFormatter.format(targetPrice.toInt()),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.ambientGlow
                        )
                    }

                    Text(
                        text = "Save ₹$savings",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF34C759)
                    )
                }

                // Interactive Slider
                Slider(
                    value = targetPrice,
                    onValueChange = { newValue ->
                        targetPrice = newValue
                    },
                    onValueChangeFinished = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    valueRange = 18000f..26990f,
                    steps = 17,
                    colors = SliderDefaults.colors(
                        thumbColor = PriceLensTheme.glassColors.ambientGlow,
                        activeTrackColor = PriceLensTheme.glassColors.ambientGlow,
                        inactiveTrackColor = PriceLensTheme.glassColors.surfaceHighlight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Simulate Notification Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (showMockNotification) PriceLensTheme.glassColors.surfaceHighlight
                            else PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f)
                        )
                        .border(
                            1.dp,
                            if (showMockNotification) PriceLensTheme.glassColors.specularBorder
                            else PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showMockNotification = true
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = PriceLensTheme.glassColors.ambientGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔔 Simulate Price Drop Notification",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.ambientGlow
                    )
                }
            }
        }
    }
}

/**
 * Slide 4: 100% Private, On-Device & Interactive Setup
 */
@Composable
private fun OnboardingSlide4(
    hasNotificationPermission: Boolean,
    onRequestNotifications: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Currency selector state
    var selectedCurrency by remember { mutableStateOf("₹ INR") }
    val currencies = listOf("₹ INR", "$ USD", "€ EUR", "£ GBP")

    // Expandable audit cards state
    var expandedAuditIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Image Illustration Card with 3D Tilt
        InteractiveHeroCard(
            illustrationRes = R.drawable.illustration_intro_privacy,
            contentDescription = "Privacy & Security Illustration",
            hintText = "Drag to tilt 3D card"
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.intro_slide4_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PriceLensTheme.glassColors.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose your currency, inspect our on-device privacy guarantee, and enable price drop notifications.",
                style = MaterialTheme.typography.bodyMedium,
                color = PriceLensTheme.glassColors.textSecondary,
                textAlign = TextAlign.Center
            )
        }

        // Interactive Currency Selector Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Preferred Store Currency",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PriceLensTheme.glassColors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currencies.forEach { currency ->
                        val isSelected = selectedCurrency == currency
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.2f)
                                    else PriceLensTheme.glassColors.surfaceHighlight
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedCurrency = currency
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currency,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // Notifications Permission Action Card
        PermissionCardItem(
            icon = Icons.Default.Notifications,
            title = "Price Drop Alerts",
            description = "Receive instant push alerts the minute your tracked items drop below your target.",
            isGranted = hasNotificationPermission,
            actionLabel = "Enable Alerts",
            onAction = onRequestNotifications
        )

        // Interactive Expandable Privacy Audit Accordion
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val audits = listOf(
                Triple(
                    "🛡️ 100% Local SQLite Database",
                    "Stored entirely on your device",
                    "All bookmarks, product URLs, custom targets, and price history snapshots are saved exclusively in your local encrypted Room SQLite database."
                ),
                Triple(
                    "🔒 Zero Cloud Telemetry & No Accounts",
                    "No sign-up or tracking SDKs",
                    "PriceLens has no sign-up forms, requires no email address, collects zero user identifiers, and embeds zero advertising or analytics SDKs."
                ),
                Triple(
                    "⚡ Direct Store Connections",
                    "Direct phone-to-retailer communication",
                    "When price checks run, your device connects directly to Amazon, Flipkart, or your chosen store without routing through third-party servers."
                )
            )

            audits.forEachIndexed { index, (title, subtitle, description) ->
                val isExpanded = expandedAuditIndex == index
                val rotationAngle by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    label = "arrowRotation"
                )

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        expandedAuditIndex = if (isExpanded) -1 else index
                    }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = PriceLensTheme.glassColors.textSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = PriceLensTheme.glassColors.textSecondary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(rotationAngle)
                            )
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PriceLensTheme.glassColors.textSecondary,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionCardItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    val isDark = PriceLensTheme.isDark
    val glow = PriceLensTheme.glassColors.ambientGlow
    val iconBg = if (isGranted) Color(0xFF34C759).copy(alpha = 0.15f)
    else PriceLensTheme.glassColors.surfaceHighlight
    val iconTint = if (isGranted) Color(0xFF34C759) else PriceLensTheme.glassColors.textPrimary

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        borderColor = if (isGranted) Color(0xFF34C759).copy(alpha = 0.4f)
        else PriceLensTheme.glassColors.specularBorder,
        onClick = if (!isGranted) onAction else null
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PriceLensTheme.glassColors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = PriceLensTheme.glassColors.textSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isGranted) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF34C759).copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34C759)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(glow)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF040404) else Color.White
                    )
                }
            }
        }
    }
}
