package com.pricelens.presentation.comparison

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.runtime.getValue
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
import com.pricelens.core.designsystem.MintSuccess
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.designsystem.components.LiquidGlassPrimaryButton
import com.pricelens.core.designsystem.components.LiquidGlassSecondaryButton
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = PriceLensTheme.glassColors.backgroundBrush)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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

        // 3. Track Outside Website & Price Drop Section
        item {
            TrackedAlertsSection(
                activeAlerts = activeAlerts,
                currencyFormatter = currencyFormatter,
                onOpenTrackDialog = { onOpenTrackDialog(null, details.lowestPriceOffer?.price, null) },
                onDeleteAlert = onDeleteAlert
            )
        }

        // 4. Observed Timestamp & Confidence
        item {
            ObservedTimestampCard(
                timestamp = details.lastRefreshedTimestamp,
                confidenceScore = details.confidenceScore
            )
        }

        // 5. Price History Card
        item {
            PriceHistoryCard(history = details.priceHistory)
        }

        // 6. Retailer Offers Section
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
                    }
                )
            }
        }

        // 7. Explicit Unavailable Retailers Section
        if (details.unavailableRetailers.isNotEmpty()) {
            item {
                Text(
                    text = "${stringResource(R.string.unavailable_retailers_title)} (${details.unavailableRetailers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PriceLensTheme.glassColors.textPrimary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            items(
                items = details.unavailableRetailers,
                key = { it.retailerId }
            ) { unavailable ->
                UnavailableRetailerCard(
                    unavailable = unavailable,
                    onTrackRetailer = {
                        onOpenTrackDialog(unavailable.retailerName, null, null)
                    }
                )
            }
        }

        // 8. Specifications Section
        if (details.specifications.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.specifications_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PriceLensTheme.glassColors.textPrimary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            item {
                SpecificationsCard(specifications = details.specifications)
            }
        }

        // 9. Similar & Cheaper Alternatives Section
        if (details.alternatives.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Similar & Cheaper Alternatives (${details.alternatives.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                    Text(
                        text = "Based on normalized catalog specifications and lower available prices",
                        style = MaterialTheme.typography.bodySmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                }
            }

            items(
                items = details.alternatives,
                key = { it.productId }
            ) { alternative ->
                AlternativeProductCard(
                    alternative = alternative,
                    currencyFormatter = currencyFormatter,
                    onSelectAlternative = onSelectAlternative
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

            Text(
                text = "Get notified instantly when this item drops below your target price on supported or custom outside websites.",
                style = MaterialTheme.typography.bodySmall,
                color = PriceLensTheme.glassColors.textSecondary
            )

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
private fun PriceHistoryCard(history: List<com.pricelens.domain.model.PriceHistoryPoint>) {
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
                Text(
                    text = "Price History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PriceLensTheme.glassColors.textPrimary
                )
                Text(
                    text = if (history.size >= 2) "${history.size} observations" else "History unavailable",
                    style = MaterialTheme.typography.labelSmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )
            }

            if (history.size < 2) {
                Text(
                    text = "History graphs and price drop trends are only displayed after at least two verified observations are recorded.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )
            } else {
                val prices = history.map { it.price }
                val low = prices.minOrNull() ?: 0.0
                val high = prices.maxOrNull() ?: 0.0
                val average = prices.average()
                val current = history.last().price

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

            // Title
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

            if (!details.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = details.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )
            }
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
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = MintSuccess.copy(alpha = 0.5f),
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
                    Text(
                        text = if (offer.price != null) currencyFormatter.format(offer.price) else "N/A",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
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
private fun UnavailableRetailerCard(
    unavailable: UnavailableRetailer,
    onTrackRetailer: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = AmberAlert.copy(alpha = 0.3f),
        backgroundColor = PriceLensTheme.glassColors.surfaceGlass.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = AmberAlert.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.RemoveShoppingCart,
                            contentDescription = null,
                            tint = AmberAlert,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = unavailable.retailerName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                    Text(
                        text = unavailable.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                }
            }

            OutlinedButton(
                onClick = onTrackRetailer,
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddAlert,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Track",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent
                )
            }
        }
    }
}

@Composable
private fun SpecificationsCard(
    specifications: Map<String, String>,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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

@Composable
private fun ComparisonLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = ElectricBlue,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = "Comparing retailer offers…",
                style = MaterialTheme.typography.bodyMedium,
                color = PriceLensTheme.glassColors.textSecondary
            )
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
