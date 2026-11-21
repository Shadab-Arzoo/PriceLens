package com.pricelens.presentation.deals

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pricelens.core.designsystem.AmberAlert
import com.pricelens.core.designsystem.CyanAccent
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.util.rememberInrCurrencyFormatter
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DealsRoute(
    onNavigateToComparison: (productId: String, productName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DealsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DealsScreen(
        uiState = uiState,
        onCategorySelected = viewModel::onCategorySelected,
        onStoreSelected = viewModel::onStoreSelected,
        onSortSelected = viewModel::onSortSelected,
        onBudgetSelected = viewModel::onBudgetSelected,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onMinDiscountChanged = viewModel::onMinDiscountChanged,
        onQuickAlert = viewModel::quickSetAlert,
        onDismissNotification = viewModel::clearNotification,
        onRefresh = viewModel::loadDeals,
        onNavigateToComparison = onNavigateToComparison,
        modifier = modifier
    )
}

@Composable
fun DealsScreen(
    uiState: DealsUiState,
    onCategorySelected: (DealCategory) -> Unit,
    onStoreSelected: (String) -> Unit,
    onSortSelected: (DealSortOrder) -> Unit,
    onBudgetSelected: (BudgetFilter) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onMinDiscountChanged: (Int) -> Unit,
    onQuickAlert: (DealItem, Double) -> Unit,
    onDismissNotification: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateToComparison: (productId: String, productName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = rememberInrCurrencyFormatter()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val onOpenStoreUrl: (String) -> Unit = remember(context) {
        { url ->
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(uiState.notificationMessage) {
        if (uiState.notificationMessage != null) {
            delay(3500)
            onDismissNotification()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AmberAlert)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Deals Radar",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textPrimary
                        )
                    }
                    Text(
                        text = "Live Price Cuts & Cross-Store Drops",
                        style = MaterialTheme.typography.labelSmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PriceLensTheme.glassColors.surfaceHighlight,
                    border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder),
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onRefresh()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh deals",
                            tint = PriceLensTheme.glassColors.ambientGlow,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "SYNC",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.ambientGlow
                        )
                    }
                }
            }

            // In-page search bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = {
                        Text(
                            text = "Filter active deals (e.g. iPhone, Sony, M3)...",
                            style = MaterialTheme.typography.bodySmall,
                            color = PriceLensTheme.glassColors.textTertiary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = PriceLensTheme.glassColors.ambientGlow,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = PriceLensTheme.glassColors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberAlert,
                        unfocusedBorderColor = PriceLensTheme.glassColors.specularBorder,
                        focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                        unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )
            }

            // Min Discount % Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val discountOptions = listOf(0 to "All Discounts", 15 to "15%+ Off 🔥", 20 to "20%+ Off ⚡", 25 to "25%+ Off 💥")
                discountOptions.forEach { (pct, label) ->
                    val isSelected = uiState.minDiscountPercent == pct
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) AmberAlert.copy(alpha = 0.2f) else PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, if (isSelected) AmberAlert else PriceLensTheme.glassColors.specularBorder),
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onMinDiscountChanged(pct)
                        }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AmberAlert else PriceLensTheme.glassColors.textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Category Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DealCategory.entries.forEach { category ->
                    val isSelected = uiState.selectedCategory == category
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) AmberAlert.copy(alpha = 0.2f) else PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) AmberAlert else PriceLensTheme.glassColors.specularBorder
                        ),
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCategorySelected(category)
                        }
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AmberAlert else PriceLensTheme.glassColors.textPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Budget Filters Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BudgetFilter.entries.forEach { budget ->
                    val isSelected = uiState.selectedBudget == budget
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.16f) else Color.Transparent,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBudgetSelected(budget)
                        }
                    ) {
                        Text(
                            text = budget.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Main Deals List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PriceLensTheme.glassColors.ambientGlow,
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Deal Spotlight Card
                    if (uiState.spotlightDeal != null && uiState.selectedCategory == DealCategory.ALL) {
                        item(key = "spotlight_deal_card") {
                            SpotlightDealCard(
                                deal = uiState.spotlightDeal,
                                currencyFormatter = currencyFormatter,
                                onCompare = { onNavigateToComparison(it.productId, it.title) }
                            )
                        }
                    }

                    // Section Title
                    item(key = "active_deals_section_title") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACTIVE DEALS (${uiState.deals.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.textSecondary
                            )

                            Text(
                                text = "Live Verified",
                                style = MaterialTheme.typography.labelSmall,
                                color = PriceLensTheme.glassColors.ambientGlow,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Deal Items
                    items(
                        items = uiState.deals,
                        key = { it.productId }
                    ) { deal ->
                        DealCard(
                            deal = deal,
                            currencyFormatter = currencyFormatter,
                            onCompare = { onNavigateToComparison(deal.productId, deal.title) },
                            onQuickAlert = {
                                val target = (deal.currentPrice * 0.95).toInt().toDouble()
                                onQuickAlert(deal, target)
                            },
                            onOpenStore = { onOpenStoreUrl(deal.storeUrl) }
                        )
                    }
                }
            }
        }

        // Floating Toast Notification
        AnimatedVisibility(
            visible = uiState.notificationMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 60.dp, start = 20.dp, end = 20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.95f),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = uiState.notificationMessage.orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismissNotification,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpotlightDealCard(
    deal: DealItem,
    currencyFormatter: NumberFormat,
    onCompare: (DealItem) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        borderColor = AmberAlert.copy(alpha = 0.5f)
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
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmberAlert.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, AmberAlert.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AmberAlert,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "DEAL OF THE DAY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberAlert
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "-${deal.discountPercent}% OFF",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = PriceLensTheme.glassColors.ambientGlow,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = deal.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PriceLensTheme.glassColors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Flash Countdown
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AmberAlert,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "FLASH SALE • ENDS IN 05h : 23m : 18s",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = AmberAlert
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Original: ${currencyFormatter.format(deal.originalPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        textDecoration = TextDecoration.LineThrough,
                        color = PriceLensTheme.glassColors.textTertiary
                    )
                    Text(
                        text = currencyFormatter.format(deal.currentPrice),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = PriceLensTheme.glassColors.ambientGlow
                    )
                }

                Text(
                    text = "Lowest on ${deal.lowestRetailer}",
                    style = MaterialTheme.typography.labelSmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AmberAlert.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, AmberAlert.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCompare(deal)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Compare Across ${deal.retailerCount} Stores & View Coupons",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberAlert
                    )
                }
            }
        }
    }
}

@Composable
private fun DealCard(
    deal: DealItem,
    currencyFormatter: NumberFormat,
    onCompare: () -> Unit,
    onQuickAlert: () -> Unit,
    onOpenStore: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        borderColor = PriceLensTheme.glassColors.specularBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Store + Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = PriceLensTheme.glassColors.ambientGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = deal.lowestRetailer,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.ambientGlow
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${deal.dealBadge} • -${deal.discountPercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.ambientGlow,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Product Title
            Text(
                text = deal.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PriceLensTheme.glassColors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Price & Savings Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = currencyFormatter.format(deal.currentPrice),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = PriceLensTheme.glassColors.ambientGlow
                        )
                        Text(
                            text = currencyFormatter.format(deal.originalPrice),
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough,
                            color = PriceLensTheme.glassColors.textTertiary
                        )
                    }
                    Text(
                        text = "Save ${currencyFormatter.format(deal.savingsAmount)} on this deal",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PriceLensTheme.glassColors.ambientGlow
                    )
                }

                Text(
                    text = "${deal.retailerCount} stores",
                    style = MaterialTheme.typography.labelSmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )
            }

            // Authentic Promo Highlight
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = AmberAlert,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = deal.highlightPromo,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = PriceLensTheme.glassColors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCompare()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Compare Stores",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.ambientGlow
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PriceLensTheme.glassColors.surfaceHighlight,
                    border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder),
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onQuickAlert()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = "Price Alert",
                            tint = PriceLensTheme.glassColors.ambientGlow,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Alert",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.ambientGlow
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PriceLensTheme.glassColors.surfaceHighlight,
                    border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder),
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenStore()
                    }
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open in browser",
                            tint = PriceLensTheme.glassColors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PriceLensTheme.glassColors.surfaceHighlight,
                    border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder),
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "🔥 PriceLens Deal: ${deal.title} is available at lowest price of ${currencyFormatter.format(deal.currentPrice)} (-${deal.discountPercent}% OFF) on ${deal.lowestRetailer}!\nLink: ${deal.storeUrl}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Deal"))
                        } catch (_: Exception) {}
                    }
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share deal",
                            tint = PriceLensTheme.glassColors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
