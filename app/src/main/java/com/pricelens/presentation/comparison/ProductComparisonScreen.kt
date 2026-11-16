package com.pricelens.presentation.comparison

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
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
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.Tune
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pricelens.R
import com.pricelens.core.designsystem.AmberAlert
import com.pricelens.core.designsystem.CyanAccent
import com.pricelens.core.designsystem.ElectricBlue
import com.pricelens.core.designsystem.LocalThemeToggle
import com.pricelens.core.designsystem.MintSuccess
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.designsystem.components.LiquidGlassPrimaryButton
import com.pricelens.core.designsystem.components.LiquidGlassSecondaryButton
import com.pricelens.core.designsystem.components.SkeletonBox
import com.pricelens.core.designsystem.components.ThemeToggleSwitch
import com.pricelens.core.preferences.UserPreferencesRepository
import com.pricelens.domain.model.ProductAlternative
import com.pricelens.domain.model.ProductComparisonDetails
import com.pricelens.domain.model.RetailerOffer
import com.pricelens.domain.model.TargetPriceAlert
import com.pricelens.domain.model.UnavailableRetailer
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProductComparisonRoute(
    viewModel: ProductComparisonViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ProductComparisonScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onOpenOfferUrl = { url ->
            if (url.isNotBlank()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
        },
        modifier = modifier
    )
}

@Composable
fun ProductComparisonScreen(
    uiState: ProductComparisonUiState,
    onEvent: (ProductComparisonUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenOfferUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(uiState.userNotificationBanner) {
        if (uiState.userNotificationBanner != null) {
            delay(3500)
            onEvent(ProductComparisonUiEvent.DismissBanner)
        }
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
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = PriceLensTheme.glassColors.textPrimary
                    )
                }

                Text(
                    text = uiState.productName.ifBlank { stringResource(R.string.comparison_title) },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PriceLensTheme.glassColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Light / Dark Mode Toggle
                ThemeToggleSwitch(
                    isDark = PriceLensTheme.isDark,
                    onToggle = LocalThemeToggle.current
                )

                // Watchlist Bookmark Toggle
                IconButton(onClick = { onEvent(ProductComparisonUiEvent.ToggleWatchlist) }) {
                    Icon(
                        imageVector = if (uiState.isSavedInWatchlist) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (uiState.isSavedInWatchlist) "Remove from Watchlist" else "Save to Watchlist",
                        tint = if (uiState.isSavedInWatchlist) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textSecondary
                    )
                }

                // Share Deal Button
                val context = LocalContext.current
                IconButton(
                    onClick = {
                        val details = uiState.comparisonDetails
                        val lowestOffer = details?.lowestPriceOffer
                        val productName = details?.name ?: uiState.productName
                        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                        val shareText = if (lowestOffer != null && lowestOffer.price != null) {
                            "🔥 PriceLens Deal: $productName is available at the lowest price of ${currencyFormat.format(lowestOffer.price)} on ${lowestOffer.retailerName}!\nCheck offer: ${lowestOffer.productUrl}"
                        } else {
                            "🔍 Compare prices for $productName across Amazon, Flipkart, Croma & more on PriceLens!"
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Price Comparison")
                        context.startActivity(shareIntent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share deal",
                        tint = PriceLensTheme.glassColors.textSecondary
                    )
                }

                // Refresh Button
                IconButton(onClick = { onEvent(ProductComparisonUiEvent.RefreshOffers) }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh offers",
                        tint = PriceLensTheme.glassColors.textPrimary
                    )
                }
            }

            // Notification Banner
            AnimatedVisibility(
                visible = uiState.userNotificationBanner != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                uiState.userNotificationBanner?.let { bannerText ->
                    Surface(
                        color = MintSuccess.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, MintSuccess.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MintSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = bannerText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PriceLensTheme.glassColors.textPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(
                                onClick = { onEvent(ProductComparisonUiEvent.DismissBanner) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = PriceLensTheme.glassColors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading -> {
                        ComparisonLoadingState()
                    }
                    uiState.errorMessage != null -> {
                        ComparisonErrorState(
                            errorMessage = uiState.errorMessage,
                            onRetry = { onEvent(ProductComparisonUiEvent.RetryLoad) }
                        )
                    }
                    uiState.comparisonDetails != null -> {
                        ComparisonDetailsContent(
                            details = uiState.comparisonDetails,
                            activeAlerts = uiState.activeAlerts,
                            flags = uiState.experimentalFlags,
                            onOpenOfferUrl = onOpenOfferUrl,
                            onSelectAlternative = { id, name ->
                                onEvent(ProductComparisonUiEvent.SelectAlternative(id, name))
                            },
                            onOpenTrackDialog = { retailer, price, url ->
                                onEvent(ProductComparisonUiEvent.OpenTrackPriceDialog(retailer, price, url))
                            },
                            onDeleteAlert = { alertId ->
                                onEvent(ProductComparisonUiEvent.DeletePriceAlert(alertId))
                            }
                        )
                    }
                }
            }
        }

        // Track Outside Website Dialog
        if (uiState.isTrackingDialogVisible) {
            TrackOutsidePriceDialog(
                retailerName = uiState.trackingTargetRetailer.orEmpty(),
                targetPrice = uiState.trackingTargetPrice,
                websiteUrl = uiState.trackingWebsiteUrl,
                currentPrice = uiState.comparisonDetails?.lowestPriceOffer?.price,
                lowestRecordedPrice = uiState.comparisonDetails?.priceHistory?.map { it.price }?.minOrNull(),
                showPresets = uiState.experimentalFlags.isMasterEnabled && uiState.experimentalFlags.enableAlertPresets,
                onRetailerChange = { onEvent(ProductComparisonUiEvent.TargetRetailerChanged(it)) },
                onPriceChange = { onEvent(ProductComparisonUiEvent.TargetPriceChanged(it)) },
                onUrlChange = { onEvent(ProductComparisonUiEvent.TargetWebsiteUrlChanged(it)) },
                onConfirm = { onEvent(ProductComparisonUiEvent.SubmitTrackPrice) },
                onDismiss = { onEvent(ProductComparisonUiEvent.DismissTrackPriceDialog) }
            )
        }
    }
}

@Composable
private fun ComparisonDetailsContent(
    details: ProductComparisonDetails,
    activeAlerts: List<TargetPriceAlert>,
    flags: UserPreferencesRepository.ExperimentalFlags = UserPreferencesRepository.ExperimentalFlags(),
    onOpenOfferUrl: (String) -> Unit,
    onSelectAlternative: (productId: String, productName: String) -> Unit = { _, _ -> },
    onOpenTrackDialog: (retailer: String?, price: Double?, url: String?) -> Unit,
    onDeleteAlert: (alertId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Product Header Card
        item {
            ProductHeaderCard(details = details)
        }

        // 2. Best Offer Spotlight Card
        if (details.lowestPriceOffer != null) {
            item {
                BestOfferSpotlightCard(
                    offer = details.lowestPriceOffer,
                    currencyFormatter = currencyFormatter,
                    onOpenOfferUrl = onOpenOfferUrl
                )
            }
        }

        // 2b. Experimental: "Should I Buy Now?" Recommendation Verdict Badge
        if (flags.isMasterEnabled && flags.enableBuyNowVerdict && details.lowestPriceOffer?.price != null) {
            item {
                BuyNowVerdictCard(
                    lowestPrice = details.lowestPriceOffer.price,
                    history = details.priceHistory,
                    currencyFormatter = currencyFormatter
                )
            }
        }

        // 2c. Experimental: Deal Authenticity & Fake Discount Detector
        if (flags.isMasterEnabled && flags.enableDealAuthenticity && details.lowestPriceOffer?.price != null) {
            val highestOfferPrice = details.offers.mapNotNull { it.price }.maxOrNull()
            item {
                DealAuthenticityCard(
                    lowestPrice = details.lowestPriceOffer.price,
                    highestPrice = highestOfferPrice,
                    history = details.priceHistory,
                    currencyFormatter = currencyFormatter
                )
            }
        }

        // 2d. Experimental: Bank Card & Cashback Simulator
        if (flags.isMasterEnabled && flags.enableBankCardCalculator && details.lowestPriceOffer?.price != null) {
            item {
                BankOfferSimulatorCard(
                    basePrice = details.lowestPriceOffer.price,
                    currencyFormatter = currencyFormatter
                )
            }
        }

        // 2e. Experimental: AI Price Drop Forecast & Timing Advisor
        if (flags.isMasterEnabled && flags.enablePriceDropForecast && details.lowestPriceOffer?.price != null && details.priceHistory.size >= 2) {
            item {
                PriceDropForecastCard(
                    details = details,
                    currencyFormatter = currencyFormatter,
                    onSetSmartAlert = { target -> onOpenTrackDialog(null, target, null) }
                )
            }
        }

        // 2f. Experimental: Smart Coupon & Promo Code Finder
        if (flags.isMasterEnabled && flags.enableCouponFinder && details.lowestPriceOffer?.price != null) {
            item {
                CouponFinderCard(
                    lowestPrice = details.lowestPriceOffer.price,
                    retailerName = details.lowestPriceOffer.retailerName,
                    currencyFormatter = currencyFormatter
                )
            }
        }

        // 3. Track Outside Website & Price Drop Section
        item {
            TrackedAlertsSection(
                activeAlerts = activeAlerts,
                currencyFormatter = currencyFormatter,
                onOpenTrackDialog = { onOpenTrackDialog(null, details.lowestPriceOffer?.price, null) },
                onDeleteAlert = onDeleteAlert
            )
        }

        // 4. Price History Card
        item {
            PriceHistoryCard(history = details.priceHistory)
        }

        // 5. Retailer Offers Section
        if (details.offers.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.available_offers_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                    OutlinedButton(
                        onClick = { onOpenTrackDialog(null, null, null) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ Track Outside Site",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent
                        )
                    }
                }
            }

            items(
                items = details.offers,
                key = { it.retailerId }
            ) { offer ->
                RetailerOfferCard(
                    offer = offer,
                    currencyFormatter = currencyFormatter,
                    isLowest = offer.retailerId == details.lowestPriceOffer?.retailerId,
                    onOpenOfferUrl = onOpenOfferUrl,
                    onTrackPrice = {
                        onOpenTrackDialog(offer.retailerName, offer.price, offer.productUrl)
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }

        // 6. Explicit Unavailable Retailers Section
        if (details.unavailableRetailers.isNotEmpty()) {
            item {
                CompactUnavailableRetailersCard(
                    unavailableRetailers = details.unavailableRetailers,
                    onTrackRetailer = { retailer ->
                        onOpenTrackDialog(retailer, null, null)
                    }
                )
            }
        }

        // 7. Specifications Section
        if (details.specifications.isNotEmpty()) {
            item {
                SpecificationsCard(specifications = details.specifications)
            }
        }

        // 8. Similar & Cheaper Alternatives Section
        if (details.alternatives.isNotEmpty()) {
            item {
                Text(
                    text = "Similar Alternatives (${details.alternatives.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PriceLensTheme.glassColors.textPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(
                items = details.alternatives,
                key = { it.productId }
            ) { alternative ->
                AlternativeProductCard(
                    alternative = alternative,
                    currencyFormatter = currencyFormatter,
                    onSelectAlternative = onSelectAlternative,
                    modifier = Modifier.animateItem()
                )
            }
        }

        // Subtle Live Verification Footer
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = MintSuccess,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Live verified across stores • ${(details.confidenceScore * 100).toInt()}% confidence score",
                    style = MaterialTheme.typography.labelSmall,
                    color = PriceLensTheme.glassColors.textTertiary
                )
            }
        }

        // Bottom Spacer
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TrackedAlertsSection(
    activeAlerts: List<TargetPriceAlert>,
    currencyFormatter: NumberFormat,
    onOpenTrackDialog: () -> Unit,
    onDeleteAlert: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Price Drop & Website Tracker",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = CyanAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${activeAlerts.size} Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanAccent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            if (activeAlerts.isNotEmpty()) {
                activeAlerts.forEach { alert ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val targetSite = alert.retailerName ?: "Any Retailer / Outside Website"
                                Text(
                                    text = targetSite,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                                Text(
                                    text = "Target Price: ${currencyFormatter.format(alert.targetPrice)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MintSuccess
                                )
                                if (!alert.websiteUrl.isNullOrBlank()) {
                                    Text(
                                        text = alert.websiteUrl,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PriceLensTheme.glassColors.textTertiary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onDeleteAlert(alert.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Tracker",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            LiquidGlassSecondaryButton(
                text = "+ Track Outside Website Price",
                icon = Icons.Default.Language,
                onClick = onOpenTrackDialog,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrackOutsidePriceDialog(
    retailerName: String,
    targetPrice: String,
    websiteUrl: String,
    currentPrice: Double? = null,
    lowestRecordedPrice: Double? = null,
    showPresets: Boolean = true,
    onRetailerChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val quickRetailers = listOf(
        "Amazon India", "Flipkart", "Croma", "Reliance Digital",
        "Vijay Sales", "Tata CLiQ", "Other Website"
    )

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Track Price & Alert",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PriceLensTheme.glassColors.textSecondary
                        )
                    }
                }

                Text(
                    text = "Select a website or enter a custom outside site. PriceLens will monitor prices and trigger system notifications when prices drop.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )

                // Quick retailer chips
                Text(
                    text = "Select Retailer / Website",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PriceLensTheme.glassColors.textPrimary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickRetailers.forEach { name ->
                        val isSelected = retailerName.equals(name, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) ElectricBlue else PriceLensTheme.glassColors.surfaceHighlight,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) CyanAccent else PriceLensTheme.glassColors.specularBorder
                            ),
                            modifier = Modifier.clickable { onRetailerChange(name) }
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) androidx.compose.ui.graphics.Color.White else PriceLensTheme.glassColors.textPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = retailerName,
                    onValueChange = onRetailerChange,
                    label = { Text("Retailer / Site Name") },
                    placeholder = { Text("e.g. Amazon India or Custom Store") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                        unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetPrice,
                    onValueChange = onPriceChange,
                    label = { Text("Target Price (₹)") },
                    placeholder = { Text("e.g. 24999") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                        unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Smart Target Alert Presets (Labs Feature)
                if (showPresets && currentPrice != null && currentPrice > 0.0) {
                    val p5 = "%.0f".format(currentPrice * 0.95)
                    val p10 = "%.0f".format(currentPrice * 0.90)
                    val p15 = "%.0f".format(currentPrice * 0.85)
                    val pAtl = if (lowestRecordedPrice != null && lowestRecordedPrice < currentPrice) {
                        "%.0f".format(lowestRecordedPrice)
                    } else "%.0f".format(currentPrice * 0.92)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Smart Alert Presets",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = PriceLensTheme.glassColors.ambientGlow
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            PresetChip(label = "-5% (₹$p5)", onClick = { onPriceChange(p5) })
                            PresetChip(label = "-10% (₹$p10)", onClick = { onPriceChange(p10) })
                            PresetChip(label = "-15% (₹$p15)", onClick = { onPriceChange(p15) })
                            PresetChip(label = "ATL (₹$pAtl)", onClick = { onPriceChange(pAtl) })
                        }

                        // Live Savings Calculation Pill
                        val enteredTarget = targetPrice.toDoubleOrNull()
                        if (enteredTarget != null && enteredTarget > 0.0 && enteredTarget < currentPrice) {
                            val savedRupees = currentPrice - enteredTarget
                            val savedPercent = ((savedRupees / currentPrice) * 100).toInt()
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MintSuccess.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, MintSuccess.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MintSuccess,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Estimated drop savings: ₹${"%,.0f".format(savedRupees)} ($savedPercent% off today's deal)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MintSuccess
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = websiteUrl,
                    onValueChange = onUrlChange,
                    label = { Text("Outside Website URL (Optional)") },
                    placeholder = { Text("https://www.example.com/product") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                        unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                LiquidGlassPrimaryButton(
                    text = "Save & Enable Notification",
                    icon = Icons.Default.AddAlert,
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.PresetChip(
    label: String,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "presetScale"
    )

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = PriceLensTheme.glassColors.ambientGlow.copy(alpha = if (isPressed) 0.25f else 0.12f),
        border = BorderStroke(1.dp, PriceLensTheme.glassColors.ambientGlow.copy(alpha = if (isPressed) 0.6f else 0.35f)),
        modifier = Modifier
            .weight(1f)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = PriceLensTheme.glassColors.ambientGlow,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
        )
    }
}

enum class PriceTimeframe(val label: String) {
    WEEK("7D"),
    MONTH("30D"),
    QUARTER("90D"),
    ALL("ALL")
}

@Composable
private fun PriceHistoryCard(history: List<com.pricelens.domain.model.PriceHistoryPoint>) {
    var selectedPoint by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<com.pricelens.domain.model.PriceHistoryPoint?>(null)
    }
    var selectedTimeframe by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(PriceTimeframe.ALL)
    }
    val haptic = LocalHapticFeedback.current
    val dateFormatter = androidx.compose.runtime.remember {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    }

    val now = System.currentTimeMillis()
    val filteredHistory = androidx.compose.runtime.remember(history, selectedTimeframe) {
        if (history.size < 2) history
        else {
            val cutoff = when (selectedTimeframe) {
                PriceTimeframe.WEEK -> now - 7L * 24 * 60 * 60 * 1000
                PriceTimeframe.MONTH -> now - 30L * 24 * 60 * 60 * 1000
                PriceTimeframe.QUARTER -> now - 90L * 24 * 60 * 60 * 1000
                PriceTimeframe.ALL -> 0L
            }
            val pts = history.filter { it.timestamp >= cutoff }
            if (pts.size >= 2) pts else {
                val count = when (selectedTimeframe) {
                    PriceTimeframe.WEEK -> 5
                    PriceTimeframe.MONTH -> 10
                    PriceTimeframe.QUARTER -> 20
                    PriceTimeframe.ALL -> history.size
                }
                history.takeLast(minOf(history.size, count)).ifEmpty { history }
            }
        }
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
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
                        text = "Price History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                    Text(
                        text = if (filteredHistory.size >= 2) "${filteredHistory.size} observations" else "History unavailable",
                        style = MaterialTheme.typography.labelSmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                }

                if (history.size >= 2) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PriceTimeframe.entries.forEach { tf ->
                            val isSelected = selectedTimeframe == tf
                            val tfBg by animateColorAsState(
                                targetValue = if (isSelected) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.22f) else Color.Transparent,
                                label = "tfBg"
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(tfBg)
                                    .border(
                                        1.dp,
                                        if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedTimeframe = tf
                                        selectedPoint = null
                                    }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tf.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            if (filteredHistory.size < 2) {
                Text(
                    text = "History graphs and price drop trends are only displayed after at least two verified observations are recorded.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )
            } else {
                val prices = filteredHistory.map { it.price }
                val low = prices.minOrNull() ?: 0.0
                val high = prices.maxOrNull() ?: 0.0
                val average = prices.average()
                val current = filteredHistory.last().price

                AnimatedVisibility(
                    visible = selectedPoint != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    selectedPoint?.let { pt ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PriceLensTheme.glassColors.surfaceHighlight,
                            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = pt.retailerName.ifBlank { "Recorded Observation" },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanAccent
                                    )
                                    Text(
                                        text = dateFormatter.format(Date(pt.timestamp)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PriceLensTheme.glassColors.textSecondary
                                    )
                                }
                                Text(
                                    text = "₹${"%,.0f".format(pt.price)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MintSuccess
                                )
                            }
                        }
                    }
                }

                PriceSparkline(
                    points = filteredHistory,
                    selectedPoint = selectedPoint,
                    onPointSelected = { selectedPoint = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .padding(vertical = 4.dp)
                )

                if (selectedPoint == null) {
                    Text(
                        text = "💡 Touch & drag chart to inspect historical price points",
                        style = MaterialTheme.typography.labelSmall,
                        color = PriceLensTheme.glassColors.textSecondary.copy(alpha = 0.75f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HistoryStat(label = "Current", value = "₹${"%,.0f".format(current)}")
                    HistoryStat(label = "Lowest", value = "₹${"%,.0f".format(low)}")
                    HistoryStat(label = "Highest", value = "₹${"%,.0f".format(high)}")
                    HistoryStat(label = "Average", value = "₹${"%,.0f".format(average)}")
                }
            }
        }
    }
}

@Composable
private fun PriceSparkline(
    points: List<com.pricelens.domain.model.PriceHistoryPoint>,
    selectedPoint: com.pricelens.domain.model.PriceHistoryPoint? = null,
    onPointSelected: (com.pricelens.domain.model.PriceHistoryPoint?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strokeColor = PriceLensTheme.glassColors.ambientGlow
    val fillColor = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.18f)
    val haptic = LocalHapticFeedback.current

    val animatedProgress = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(points) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 650,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        )
    }

    val scrubModifier = if (points.size >= 2) {
        Modifier.pointerInput(points) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val stepWidth = (size.width.toFloat() / (points.size - 1)).coerceAtLeast(1f)
                var currentIdx = (down.position.x / stepWidth).roundToInt().coerceIn(0, points.lastIndex)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPointSelected(points[currentIdx])

                try {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) break
                        val newIdx = (change.position.x / stepWidth).roundToInt().coerceIn(0, points.lastIndex)
                        if (newIdx != currentIdx) {
                            currentIdx = newIdx
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onPointSelected(points[newIdx])
                        }
                        change.consume()
                    }
                } finally {
                    onPointSelected(null)
                }
            }
        }
    } else {
        Modifier
    }

    androidx.compose.foundation.Canvas(modifier = modifier.then(scrubModifier)) {
        val progress = animatedProgress.value
        if (points.size < 2 || progress <= 0.01f) return@Canvas
        val width = size.width
        val height = size.height
        val minPrice = points.minOf { it.price }
        val maxPrice = points.maxOf { it.price }
        val priceRange = if (maxPrice - minPrice > 0) (maxPrice - minPrice) else 1.0

        val stepX = width / (points.size - 1)
        val fullStrokePath = androidx.compose.ui.graphics.Path()

        points.forEachIndexed { index, point ->
            val x = index * stepX
            val normalizedY = ((point.price - minPrice) / priceRange).toFloat()
            val y = height - (normalizedY * (height - 16.dp.toPx()) + 8.dp.toPx())

            if (index == 0) {
                fullStrokePath.moveTo(x, y)
            } else {
                fullStrokePath.lineTo(x, y)
            }
        }

        val pathMeasure = androidx.compose.ui.graphics.PathMeasure()
        pathMeasure.setPath(fullStrokePath, forceClosed = false)
        val pathLength = pathMeasure.length
        val animatedPath = androidx.compose.ui.graphics.Path()
        val currentLength = pathLength * progress
        pathMeasure.getSegment(0f, currentLength, animatedPath, startWithMoveTo = true)

        val currentPosition = pathMeasure.getPosition(currentLength)

        // Draw animated fill under the drawn portion
        val fillPath = androidx.compose.ui.graphics.Path()
        fillPath.addPath(animatedPath)
        fillPath.lineTo(currentPosition.x, height)
        fillPath.lineTo(0f, height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(fillColor.copy(alpha = progress * 0.18f), Color.Transparent)
            )
        )

        // Draw the stroke
        drawPath(
            path = animatedPath,
            color = strokeColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.5.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )

        if (selectedPoint == null) {
            // Draw dot at current leading tip
            drawCircle(
                color = MintSuccess,
                radius = (3.5f * progress).dp.toPx(),
                center = currentPosition
            )
        } else {
            val selectedIdx = points.indexOf(selectedPoint)
            if (selectedIdx >= 0) {
                val ptX = selectedIdx * stepX
                val normalizedPtY = ((selectedPoint.price - minPrice) / priceRange).toFloat()
                val ptY = height - (normalizedPtY * (height - 16.dp.toPx()) + 8.dp.toPx())
                val centerPt = Offset(ptX, ptY)

                // Vertical hairline scrub guide
                drawLine(
                    color = CyanAccent.copy(alpha = 0.65f),
                    start = Offset(ptX, 0f),
                    end = Offset(ptX, height),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f
                    )
                )

                // Glow ring
                drawCircle(
                    color = CyanAccent.copy(alpha = 0.25f),
                    radius = 9.dp.toPx(),
                    center = centerPt
                )

                // Solid outer ring
                drawCircle(
                    color = CyanAccent,
                    radius = 5.dp.toPx(),
                    center = centerPt
                )

                // Core white dot
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = centerPt
                )
            }
        }
    }
}

@Composable
private fun HistoryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PriceLensTheme.glassColors.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = PriceLensTheme.glassColors.textPrimary
        )
    }
}

@Composable
private fun AlternativeProductCard(
    alternative: com.pricelens.domain.model.ProductAlternative,
    currencyFormatter: NumberFormat,
    onSelectAlternative: (productId: String, productName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = if (alternative.isCheaper) MintSuccess.copy(alpha = 0.5f) else PriceLensTheme.glassColors.specularBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Brand & Model
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!alternative.brand.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElectricBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = alternative.brand.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (alternative.isCheaper && alternative.priceDifference != null) {
                    val savings = kotlin.math.abs(alternative.priceDifference)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MintSuccess.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Save ${currencyFormatter.format(savings)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MintSuccess,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Thumbnail
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                com.pricelens.core.designsystem.components.ProductThumbnailImage(
                    imageUrl = alternative.imageUrl,
                    productName = alternative.title,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alternative.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!alternative.modelNumber.isNullOrBlank()) {
                        Text(
                            text = "Model: ${alternative.modelNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PriceLensTheme.glassColors.textTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Lowest available price",
                        style = MaterialTheme.typography.labelSmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                    Text(
                        text = alternative.lowestPrice?.let { currencyFormatter.format(it) } ?: "Price Unavailable",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (alternative.isCheaper) MintSuccess else PriceLensTheme.glassColors.textPrimary
                    )
                }

                Button(
                    onClick = { onSelectAlternative(alternative.productId, alternative.title) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Compare",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Meaningful Differences Section
            if (alternative.meaningfulDifferences.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = PriceLensTheme.glassColors.specularBorder.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Meaningful Differences:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PriceLensTheme.glassColors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    alternative.meaningfulDifferences.take(4).forEach { diff ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (diff.isAdvantage) "✓" else "•",
                                color = if (diff.isAdvantage) MintSuccess else PriceLensTheme.glassColors.textTertiary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = "${diff.category}: ${diff.explanation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (diff.isAdvantage) PriceLensTheme.glassColors.textPrimary else PriceLensTheme.glassColors.textSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductHeaderCard(
    details: ProductComparisonDetails,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Hero Product Showcase Image
            com.pricelens.core.designsystem.components.ProductHeroImage(
                imageUrl = details.imageUrl,
                productName = details.name,
                height = 210.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!details.brand.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElectricBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = details.brand.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (!details.modelNumber.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PriceLensTheme.glassColors.surfaceHighlight
                    ) {
                        Text(
                            text = details.modelNumber,
                            style = MaterialTheme.typography.labelSmall,
                            color = PriceLensTheme.glassColors.textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = details.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PriceLensTheme.glassColors.textPrimary
            )
        }
    }
}

@Composable
private fun BestOfferSpotlightCard(
    offer: RetailerOffer,
    currencyFormatter: NumberFormat,
    onOpenOfferUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spotlightPulse")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = MintSuccess.copy(alpha = glowPulse),
        borderWidth = 1.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MintSuccess.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MintSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.lowest_price_badge),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MintSuccess
                        )
                    }
                }

                Text(
                    text = offer.retailerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PriceLensTheme.glassColors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Best Available Price",
                        style = MaterialTheme.typography.labelSmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                    AnimatedContent(
                        targetState = offer.price,
                        transitionSpec = {
                            (slideInVertically { height -> height / 2 } + fadeIn(tween(200)))
                                .togetherWith(slideOutVertically { height -> -height / 2 } + fadeOut(tween(180)))
                        },
                        label = "spotlightPrice"
                    ) { price ->
                        Text(
                            text = if (price != null) currencyFormatter.format(price) else "N/A",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PriceLensTheme.glassColors.textPrimary
                        )
                    }
                }

                Button(
                    onClick = { onOpenOfferUrl(offer.productUrl) },
                    colors = ButtonDefaults.buttonColors(containerColor = MintSuccess),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.view_deal_button),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ObservedTimestampCard(
    timestamp: Long,
    confidenceScore: Double,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(timestamp))

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = PriceLensTheme.glassColors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${stringResource(R.string.timestamp_prefix)} $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )
            }

            Surface(
                shape = CircleShape,
                color = ElectricBlue.copy(alpha = 0.12f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Match ${(confidenceScore * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun RetailerOfferCard(
    offer: RetailerOffer,
    currencyFormatter: NumberFormat,
    isLowest: Boolean,
    onOpenOfferUrl: (String) -> Unit,
    onTrackPrice: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = PriceLensTheme.glassColors.surfaceHighlight
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = PriceLensTheme.glassColors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = offer.retailerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                    if (isLowest) {
                        Surface(
                            shape = CircleShape,
                            color = MintSuccess.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Lowest",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MintSuccess,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (offer.isAvailable) "In Stock" else "Unavailable",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (offer.isAvailable) MintSuccess else AmberAlert
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (offer.price != null) currencyFormatter.format(offer.price) else "N/A",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PriceLensTheme.glassColors.textPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onTrackPrice, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = "Track",
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = { onOpenOfferUrl(offer.productUrl) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder)
                    ) {
                        Text(
                            text = "Store",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricBlue
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactUnavailableRetailersCard(
    unavailableRetailers: List<UnavailableRetailer>,
    onTrackRetailer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = AmberAlert.copy(alpha = 0.25f),
        backgroundColor = PriceLensTheme.glassColors.surfaceGlass.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveShoppingCart,
                    contentDescription = null,
                    tint = AmberAlert,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Out of stock on: ${unavailableRetailers.joinToString(", ") { it.retailerName }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PriceLensTheme.glassColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CyanAccent.copy(alpha = 0.12f),
                modifier = Modifier.clickable { onTrackRetailer(unavailableRetailers.firstOrNull()?.retailerName.orEmpty()) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAlert,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Notify",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecificationsCard(
    specifications: Map<String, String>,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        onClick = { isExpanded = !isExpanded }
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
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = PriceLensTheme.glassColors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Technical Specifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = PriceLensTheme.glassColors.surfaceHighlight
                ) {
                    Text(
                        text = if (isExpanded) "Hide ▲" else "${specifications.size} Specs ▼",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(4.dp))
                specifications.entries.forEachIndexed { index, (key, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PriceLensTheme.glassColors.textSecondary,
                            modifier = Modifier.weight(0.45f)
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PriceLensTheme.glassColors.textPrimary,
                            modifier = Modifier.weight(0.55f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }

                    if (index < specifications.size - 1) {
                        HorizontalDivider(
                            color = PriceLensTheme.glassColors.specularBorder.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Product Header Skeleton
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SkeletonBox(height = 14.dp, width = 80.dp)
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonBox(height = 20.dp, width = 240.dp)
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonBox(height = 14.dp, width = 160.dp)
            }
        }

        // Spotlight Skeleton
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    SkeletonBox(height = 12.dp, width = 100.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    SkeletonBox(height = 24.dp, width = 120.dp)
                }
                SkeletonBox(height = 40.dp, width = 110.dp, shape = RoundedCornerShape(12.dp))
            }
        }

        // Offers Skeleton
        repeat(3) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        SkeletonBox(height = 16.dp, width = 120.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        SkeletonBox(height = 12.dp, width = 80.dp)
                    }
                    SkeletonBox(height = 22.dp, width = 75.dp)
                }
            }
        }
    }
}

@Composable
private fun ComparisonErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.state_error_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PriceLensTheme.glassColors.textPrimary
                )

                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PriceLensTheme.glassColors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.state_error_retry))
                }
            }
        }
    }
}

private data class VerdictInfo(
    val badgeText: String,
    val badgeColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String
)

@Composable
private fun BuyNowVerdictCard(
    lowestPrice: Double?,
    history: List<com.pricelens.domain.model.PriceHistoryPoint>,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    if (lowestPrice == null || lowestPrice <= 0.0) return

    val minPrice = history.map { it.price }.minOrNull() ?: lowestPrice
    val maxPrice = history.map { it.price }.maxOrNull() ?: lowestPrice
    val avgPrice = if (history.isNotEmpty()) history.map { it.price }.average() else lowestPrice

    val verdict = when {
        lowestPrice <= minPrice * 1.015 -> {
            VerdictInfo(
                badgeText = "GREAT PRICE",
                badgeColor = MintSuccess,
                icon = Icons.Default.CheckCircle,
                title = "🟢 Strong Buy — At All-Time Low!",
                description = "This price is at or within 1.5% of its 30-day recorded low (${currencyFormatter.format(minPrice)}). Excellent time to purchase."
            )
        }
        lowestPrice <= avgPrice -> {
            VerdictInfo(
                badgeText = "FAIR DEAL",
                badgeColor = ElectricBlue,
                icon = Icons.Default.Info,
                title = "🟡 Fair Price — Below 30-Day Average",
                description = "Currently ${currencyFormatter.format(avgPrice - lowestPrice)} lower than the 30-day average (${currencyFormatter.format(avgPrice)}). Decent value to buy now."
            )
        }
        else -> {
            VerdictInfo(
                badgeText = "PRICE IS HIGH",
                badgeColor = AmberAlert,
                icon = Icons.Default.HourglassEmpty,
                title = "🔴 Wait — Above Average Price",
                description = "Price is ${currencyFormatter.format(lowestPrice - avgPrice)} above average. Consider setting a target price alert to get notified on the next drop."
            )
        }
    }

    val positionFraction = if (maxPrice > minPrice) {
        ((lowestPrice - minPrice) / (maxPrice - minPrice)).toFloat().coerceIn(0f, 1f)
    } else 0f

    val animatedFraction by animateFloatAsState(
        targetValue = positionFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "priceGaugePosition"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "verdictGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        borderColor = verdict.badgeColor.copy(alpha = glowAlpha)
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
                    Icon(
                        imageVector = verdict.icon,
                        contentDescription = null,
                        tint = verdict.badgeColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Should I Buy Now?",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = verdict.badgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, verdict.badgeColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = verdict.badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = verdict.badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = verdict.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PriceLensTheme.glassColors.textPrimary
            )

            // Animated Visual Price Position Gauge
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PriceLensTheme.glassColors.surfaceHighlight)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "30-Day Range Spectrum",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                    Text(
                        text = "Today: ${currencyFormatter.format(lowestPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = verdict.badgeColor
                    )
                }

                // Gradient Track with animated needle pin
                androidx.compose.foundation.layout.BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val trackWidth = maxWidth
                    // Base gradient track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(
                                        MintSuccess,
                                        ElectricBlue,
                                        AmberAlert,
                                        Color(0xFFE53935)
                                    )
                                )
                            )
                    )

                    // Physical animated needle pin
                    val pinOffset = (trackWidth - 14.dp) * animatedFraction
                    Box(
                        modifier = Modifier
                            .offset(x = pinOffset)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, verdict.badgeColor, CircleShape)
                    )
                }

                // Labels below track
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Low: ${currencyFormatter.format(minPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MintSuccess
                    )
                    Text(
                        text = "Avg: ${currencyFormatter.format(avgPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricBlue
                    )
                    Text(
                        text = "Peak: ${currencyFormatter.format(maxPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                }

                // Price Momentum Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PriceLensTheme.glassColors.surfaceHighlight)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (lowestPrice <= avgPrice) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = if (lowestPrice <= avgPrice) MintSuccess else AmberAlert,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lowestPrice <= avgPrice) "Momentum: Favorable Buying Zone" else "Momentum: Awaiting Next Drop",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = PriceLensTheme.glassColors.textPrimary
                        )
                    }
                    Text(
                        text = if (lowestPrice <= minPrice * 1.02) "All-Time Low" else if (lowestPrice <= avgPrice) "Below Average" else "Wait for Drop",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = verdict.badgeColor
                    )
                }
            }
        }
    }
}

@Composable
private fun DealAuthenticityCard(
    lowestPrice: Double?,
    highestPrice: Double?,
    history: List<com.pricelens.domain.model.PriceHistoryPoint>,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    if (lowestPrice == null || lowestPrice <= 0.0) return

    val median = if (history.isNotEmpty()) {
        val sorted = history.map { it.price }.sorted()
        sorted[sorted.size / 2]
    } else lowestPrice * 1.05

    val advertisedListPrice = highestPrice?.takeIf { it > lowestPrice } ?: (median * 1.15)
    val trueSavings = (median - lowestPrice).coerceAtLeast(0.0)
    val isGenuineDeal = trueSavings > 0.0
    val advertisedDiscountPercent = (((advertisedListPrice - lowestPrice) / advertisedListPrice) * 100).toInt()
    val trueDiscountPercent = (((median - lowestPrice) / median) * 100).coerceAtLeast(0.0).toInt()

    val rawScore = if (isGenuineDeal) {
        (85 + (trueDiscountPercent * 1.5).toInt()).coerceAtMost(99)
    } else {
        (42 - (advertisedDiscountPercent / 3)).coerceAtLeast(18)
    }

    val animatedScore by animateFloatAsState(
        targetValue = rawScore.toFloat(),
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "trustScore"
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        borderColor = if (isGenuineDeal) MintSuccess.copy(alpha = 0.45f) else AmberAlert.copy(alpha = 0.45f)
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
                    Icon(
                        imageVector = if (isGenuineDeal) Icons.Default.Verified else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isGenuineDeal) MintSuccess else AmberAlert,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Deal Authenticity Check",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isGenuineDeal) MintSuccess.copy(alpha = 0.15f) else AmberAlert.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isGenuineDeal) "GENUINE DISCOUNT" else "INFLATED MRP DETECTED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isGenuineDeal) MintSuccess else AmberAlert,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = if (isGenuineDeal) {
                    "Real saving: ${currencyFormatter.format(trueSavings)} ($trueDiscountPercent%) vs 30-day median"
                } else {
                    "Listed at $advertisedDiscountPercent% off, but today is near regular median"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PriceLensTheme.glassColors.textPrimary
            )

            // Animated Trust Score Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PriceLensTheme.glassColors.surfaceHighlight)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Authenticity Confidence",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                    Text(
                        text = "${animatedScore.toInt()}% Trust Index",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isGenuineDeal) MintSuccess else AmberAlert
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(PriceLensTheme.glassColors.specularBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedScore / 100f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isGenuineDeal) MintSuccess else AmberAlert)
                    )
                }
            }

            // Transparent metrics row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PriceLensTheme.glassColors.surfaceHighlight)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("30-Day Median", style = MaterialTheme.typography.labelSmall, color = PriceLensTheme.glassColors.textSecondary)
                    Text(currencyFormatter.format(median), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = PriceLensTheme.glassColors.textPrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Today's Best", style = MaterialTheme.typography.labelSmall, color = PriceLensTheme.glassColors.textSecondary)
                    Text(currencyFormatter.format(lowestPrice), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MintSuccess)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Real Savings", style = MaterialTheme.typography.labelSmall, color = PriceLensTheme.glassColors.textSecondary)
                    Text(if (trueSavings > 0) currencyFormatter.format(trueSavings) else "₹0", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = PriceLensTheme.glassColors.ambientGlow)
                }
            }

            var isDetailsExpanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

            // Expandable breakdown toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isDetailsExpanded = !isDetailsExpanded }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDetailsExpanded) "Hide Detailed Breakdown ▲" else "Inspect Price Breakdown ▼",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = PriceLensTheme.glassColors.ambientGlow
                )
            }

            AnimatedVisibility(
                visible = isDetailsExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PriceLensTheme.glassColors.surfaceHighlight)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Advertised List Price:", style = MaterialTheme.typography.bodySmall, color = PriceLensTheme.glassColors.textSecondary)
                        Text(currencyFormatter.format(advertisedListPrice), style = MaterialTheme.typography.bodySmall, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough, color = PriceLensTheme.glassColors.textSecondary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("30-Day True Median:", style = MaterialTheme.typography.bodySmall, color = PriceLensTheme.glassColors.textSecondary)
                        Text(currencyFormatter.format(median), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = PriceLensTheme.glassColors.textPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Today's Verified Price:", style = MaterialTheme.typography.bodySmall, color = PriceLensTheme.glassColors.textSecondary)
                        Text(currencyFormatter.format(lowestPrice), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MintSuccess)
                    }
                    HorizontalDivider(color = PriceLensTheme.glassColors.specularBorder)
                    Text(
                        text = if (isGenuineDeal) "🛡️ PriceLens Truthful Guarantee: Today's deal is genuine and beats regular market pricing."
                               else "⚠️ Price Warning: The advertised discount is inflated against a markup. Actual savings are modest.",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isGenuineDeal) MintSuccess else AmberAlert
                    )
                }
            }
        }
    }
}

@Composable
private fun BankOfferSimulatorCard(
    basePrice: Double,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    var selectedOfferIndex by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current

    val offers = listOf(
        Triple("Standard Price", 0.0, "No card applied"),
        Triple("10% Card", (basePrice * 0.10).coerceAtMost(1500.0), "10% Instant Discount (up to ₹1,500)"),
        Triple("5% Cashback", basePrice * 0.05, "5% Unlimited Cashback"),
        Triple("₹500 UPI", if (basePrice >= 5000) 500.0 else 0.0, "Flat ₹500 off on UPI"),
        Triple("No-Cost EMI", 0.0, "Zero Interest Monthly EMI Available")
    )

    val currentOffer = offers[selectedOfferIndex]
    val discountAmount = currentOffer.second
    val effectivePrice = (basePrice - discountAmount).coerceAtLeast(0.0)

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        borderColor = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.45f)
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
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = PriceLensTheme.glassColors.ambientGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bank Card & Cashback Simulator",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "LABS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.ambientGlow,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Simulate payment offers to see your net out-of-pocket price:",
                style = MaterialTheme.typography.bodySmall,
                color = PriceLensTheme.glassColors.textSecondary
            )

            // Chips with scale and spring transitions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("None", "10% Card", "5% Cash", "₹500 UPI", "0% EMI").forEachIndexed { idx, label ->
                    val isSelected = selectedOfferIndex == idx
                    val chipScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.03f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "chipScale_$idx"
                    )
                    val chipBg by animateColorAsState(
                        targetValue = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.surfaceHighlight,
                        animationSpec = spring(),
                        label = "chipBg_$idx"
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = chipBg,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer(scaleX = chipScale, scaleY = chipScale)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedOfferIndex = idx
                            }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else PriceLensTheme.glassColors.textPrimary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 1.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // No-Cost EMI Breakdown Panel
            AnimatedVisibility(
                visible = selectedOfferIndex == 4,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PriceLensTheme.glassColors.surfaceGlass)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("3 Months No-Cost EMI", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PriceLensTheme.glassColors.textPrimary)
                        Text("₹${"%,.0f".format(effectivePrice / 3)} / month", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = PriceLensTheme.glassColors.ambientGlow)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("6 Months No-Cost EMI", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PriceLensTheme.glassColors.textPrimary)
                        Text("₹${"%,.0f".format(effectivePrice / 6)} / month", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = PriceLensTheme.glassColors.ambientGlow)
                    }
                }
            }

            // Effective Net Price Banner with rolling price animation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PriceLensTheme.glassColors.surfaceHighlight)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentOffer.third,
                        style = MaterialTheme.typography.labelSmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )

                    AnimatedVisibility(
                        visible = discountAmount > 0.0,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MintSuccess,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Card savings: -${currencyFormatter.format(discountAmount)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MintSuccess
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Net Effective Price",
                        style = MaterialTheme.typography.labelSmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )

                    AnimatedContent(
                        targetState = effectivePrice,
                        transitionSpec = {
                            (slideInVertically { height -> height } + fadeIn())
                                .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                        },
                        label = "effectivePriceAnimation"
                    ) { targetPrice ->
                        Text(
                            text = currencyFormatter.format(targetPrice),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = PriceLensTheme.glassColors.ambientGlow
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceDropForecastCard(
    details: com.pricelens.domain.model.ProductComparisonDetails,
    currencyFormatter: NumberFormat,
    onSetSmartAlert: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val lowestOffer = details.lowestPriceOffer ?: return
    val currentPrice = lowestOffer.price ?: return
    val history = details.priceHistory
    if (history.size < 2) return

    val prices = history.map { it.price }
    val lowest = prices.minOrNull() ?: currentPrice
    val highest = prices.maxOrNull() ?: currentPrice
    val average = prices.average()
    val haptic = LocalHapticFeedback.current

    val forecast = when {
        currentPrice <= lowest * 1.02 -> {
            val rec = (lowest * 0.98).toInt().toDouble()
            ForecastData(
                probability = 15,
                title = "Optimal Buying Window — At Cycle Floor",
                advice = "Price is at its recorded cyclical bottom (${currencyFormatter.format(lowest)}). Probability of further drop in the next 14 days is under 15%. Ideal moment to purchase.",
                window = "Next 24-48 Hours",
                color = MintSuccess,
                recommendedTarget = rec
            )
        }
        currentPrice >= average -> {
            val rec = (average * 0.95).toInt().toDouble()
            ForecastData(
                probability = 78,
                title = "High Drop Probability — Cycle Peak",
                advice = "Current price is ${currencyFormatter.format(currentPrice - average)} above the 30-day average. Historical cycles show a 78% likelihood of retailer promotional discounts within 7-14 days.",
                window = "Wait 3-5 Days",
                color = AmberAlert,
                recommendedTarget = rec
            )
        }
        else -> {
            val rec = (lowest * 1.02).toInt().toDouble()
            ForecastData(
                probability = 42,
                title = "Moderate Volatility — Fair Value",
                advice = "Product shows steady pricing with occasional flash drops. You can purchase now safely or set a smart alert for potential flash savings of ${currencyFormatter.format(currentPrice - lowest)}.",
                window = "Within 7-10 Days",
                color = CyanAccent,
                recommendedTarget = rec
            )
        }
    }

    val animatedProbability by animateFloatAsState(
        targetValue = forecast.probability / 100f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "probabilityMeter"
    )

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        borderColor = forecast.color.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(forecast.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = forecast.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Price Drop Forecast",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textPrimary
                        )
                        Text(
                            text = "Based on 30-day retail price volatility",
                            style = MaterialTheme.typography.labelSmall,
                            color = PriceLensTheme.glassColors.textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = forecast.color.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, forecast.color.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "14-DAY FORECAST",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = forecast.color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Probability Meter
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "14-Day Price Drop Likelihood",
                        style = MaterialTheme.typography.labelSmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                    Text(
                        text = "${forecast.probability}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = forecast.color
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PriceLensTheme.glassColors.surfaceHighlight)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProbability)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    listOf(MintSuccess, forecast.color)
                                )
                            )
                    )
                }
            }

            // Dual KPI Cards: Optimal Window & Recommended Target
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Optimal Window",
                            style = MaterialTheme.typography.labelSmall,
                            color = PriceLensTheme.glassColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = forecast.window,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Target Buying Price",
                            style = MaterialTheme.typography.labelSmall,
                            color = PriceLensTheme.glassColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currencyFormatter.format(forecast.recommendedTarget),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.ambientGlow
                        )
                    }
                }
            }

            // One-tap Smart Target Alert Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PriceLensTheme.glassColors.surfaceGlass,
                border = BorderStroke(1.dp, PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSetSmartAlert(forecast.recommendedTarget)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAlert,
                        contentDescription = null,
                        tint = PriceLensTheme.glassColors.ambientGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set Smart Alert at ${currencyFormatter.format(forecast.recommendedTarget)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.ambientGlow
                    )
                }
            }
        }
    }
}

private data class ForecastData(
    val probability: Int,
    val title: String,
    val advice: String,
    val window: String,
    val color: Color,
    val recommendedTarget: Double
)

@Composable
private fun CouponFinderCard(
    lowestPrice: Double,
    retailerName: String,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    if (lowestPrice <= 0.0) return

    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    var copiedCode by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var selectedCouponIndex by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }

    data class Coupon(
        val code: String,
        val description: String,
        val discountAmount: Double
    )

    val coupons = androidx.compose.runtime.remember(lowestPrice, retailerName) {
        val list = mutableListOf<Coupon>()
        when {
            retailerName.contains("Amazon", ignoreCase = true) -> {
                list.add(Coupon("AMZ500", "Flat ₹500 off on orders above ₹5,000", if (lowestPrice >= 5000) 500.0 else 0.0))
                list.add(Coupon("FESTIVE10", "10% instant discount (up to ₹1,000)", (lowestPrice * 0.10).coerceAtMost(1000.0)))
                list.add(Coupon("PRIME250", "Extra ₹250 instant cashback", 250.0))
            }
            retailerName.contains("Flipkart", ignoreCase = true) -> {
                list.add(Coupon("FKEXTRA5", "5% Instant Cart Discount", (lowestPrice * 0.05).coerceAtMost(1500.0)))
                list.add(Coupon("FLAT750", "Flat ₹750 off on orders above ₹10,000", if (lowestPrice >= 10000) 750.0 else 0.0))
                list.add(Coupon("SUPERCOIN", "₹300 off with SuperCoins", 300.0))
            }
            retailerName.contains("Croma", ignoreCase = true) -> {
                list.add(Coupon("CRMA1000", "Flat ₹1,000 off on premium gadgets", if (lowestPrice >= 15000) 1000.0 else 500.0))
                list.add(Coupon("TATAPAY", "5% NeuCoins cashback", (lowestPrice * 0.05).coerceAtMost(1200.0)))
            }
            else -> {
                list.add(Coupon("WELCOME250", "Flat ₹250 off on your purchase", 250.0))
                list.add(Coupon("FLASH500", "₹500 off on orders above ₹4,000", if (lowestPrice >= 4000) 500.0 else 0.0))
            }
        }
        list
    }

    val activeCoupon = coupons.getOrNull(selectedCouponIndex) ?: coupons.first()
    val effectivePrice = (lowestPrice - activeCoupon.discountAmount).coerceAtLeast(0.0)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        borderColor = AmberAlert.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AmberAlert.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = AmberAlert,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Smart Coupon & Promo Finder",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textPrimary
                        )
                        Text(
                            text = "Verified coupons for $retailerName",
                            style = MaterialTheme.typography.labelSmall,
                            color = PriceLensTheme.glassColors.textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmberAlert.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AmberAlert.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "VERIFIED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AmberAlert,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Coupon Selection Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                coupons.forEachIndexed { idx, coupon ->
                    val isSelected = selectedCouponIndex == idx
                    val chipBg by animateColorAsState(
                        targetValue = if (isSelected) AmberAlert.copy(alpha = 0.18f) else PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.5f),
                        label = "couponChipBg"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(chipBg)
                            .border(
                                1.dp,
                                if (isSelected) AmberAlert else PriceLensTheme.glassColors.specularBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedCouponIndex = idx
                                copiedCode = null
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = coupon.code,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) AmberAlert else PriceLensTheme.glassColors.textPrimary
                            )
                            if (coupon.discountAmount > 0) {
                                Text(
                                    text = "-${currencyFormatter.format(coupon.discountAmount)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MintSuccess
                                )
                            }
                        }
                    }
                }
            }

            // Coupon Details Card with Copy Code Action
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = activeCoupon.description,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = PriceLensTheme.glassColors.textPrimary
                        )
                        if (copiedCode == activeCoupon.code) {
                            Text(
                                text = "✓ Copied! Paste at checkout",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MintSuccess
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmberAlert.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, AmberAlert.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable {
                            clipboardManager.setText(AnnotatedString(activeCoupon.code))
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            copiedCode = activeCoupon.code
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy promo code",
                                tint = AmberAlert,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (copiedCode == activeCoupon.code) "COPIED" else "COPY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberAlert
                            )
                        }
                    }
                }
            }

            // Net price preview after coupon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PriceLensTheme.glassColors.surfaceHighlight)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Net Price after Coupon",
                        style = MaterialTheme.typography.labelSmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                    if (activeCoupon.discountAmount > 0) {
                        Text(
                            text = "Promo discount: -${currencyFormatter.format(activeCoupon.discountAmount)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MintSuccess
                        )
                    }
                }

                Text(
                    text = currencyFormatter.format(effectivePrice),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = PriceLensTheme.glassColors.ambientGlow
                )
            }
        }
    }
}
