package com.pricelens.presentation.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pricelens.core.designsystem.AmberAlert
import com.pricelens.core.designsystem.ElectricBlue
import com.pricelens.core.designsystem.MintSuccess
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.designsystem.components.LiquidGlassPrimaryButton
import com.pricelens.core.designsystem.components.SkeletonBox
import com.pricelens.domain.model.WatchlistItem
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WatchlistRoute(
    viewModel: WatchlistViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToComparison: (productId: String, productName: String) -> Unit,
    onNavigateToBrowse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.notificationMessage) {
        uiState.notificationMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onEvent(WatchlistUiEvent.ClearNotificationMessage)
        }
    }

    WatchlistScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToComparison = onNavigateToComparison,
        onNavigateToBrowse = onNavigateToBrowse,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    uiState: WatchlistUiState,
    onEvent: (WatchlistUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToComparison: (productId: String, productName: String) -> Unit,
    onNavigateToBrowse: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(PriceLensTheme.glassColors.backgroundBrush),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            WatchlistTopBar(
                itemCount = uiState.totalTrackedCount,
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SkeletonBox(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        height = 80.dp
                    )
                    SkeletonBox(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        height = 110.dp
                    )
                    SkeletonBox(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        height = 110.dp
                    )
                }
            } else if (uiState.items.isEmpty()) {
                EmptyWatchlistState(
                    onBrowseClick = onNavigateToBrowse,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Summary Banner Card
                    item(key = "stats_card") {
                        WatchlistStatsCard(
                            totalCount = uiState.totalTrackedCount,
                            priceDropCount = uiState.priceDropCount,
                            activeFilter = uiState.activeFilter,
                            onAllClick = { onEvent(WatchlistUiEvent.FilterChanged(WatchlistFilter.ALL)) },
                            onPriceDropsClick = { onEvent(WatchlistUiEvent.FilterChanged(WatchlistFilter.PRICE_DROPS)) }
                        )
                    }

                    // Filter Chips Row
                    item(key = "filter_chips") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isAllSelected = uiState.activeFilter == WatchlistFilter.ALL
                            val isDropSelected = uiState.activeFilter == WatchlistFilter.PRICE_DROPS

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isAllSelected) ElectricBlue.copy(alpha = 0.15f) else PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.6f))
                                    .border(1.dp, if (isAllSelected) ElectricBlue.copy(alpha = 0.6f) else PriceLensTheme.glassColors.specularBorder, RoundedCornerShape(12.dp))
                                    .clickable { onEvent(WatchlistUiEvent.FilterChanged(WatchlistFilter.ALL)) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "All (${uiState.totalTrackedCount})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAllSelected) ElectricBlue else PriceLensTheme.glassColors.textSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDropSelected) MintSuccess.copy(alpha = 0.15f) else PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.6f))
                                    .border(1.dp, if (isDropSelected) MintSuccess.copy(alpha = 0.6f) else PriceLensTheme.glassColors.specularBorder, RoundedCornerShape(12.dp))
                                    .clickable { onEvent(WatchlistUiEvent.FilterChanged(WatchlistFilter.PRICE_DROPS)) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = if (isDropSelected) MintSuccess else PriceLensTheme.glassColors.textSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Price Drops (${uiState.priceDropCount})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isDropSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isDropSelected) MintSuccess else PriceLensTheme.glassColors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Search Filter
                    if (uiState.totalTrackedCount > 2) {
                        item(key = "search_filter") {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { onEvent(WatchlistUiEvent.SearchQueryChanged(it)) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        text = "Filter saved items...",
                                        color = PriceLensTheme.glassColors.textSecondary
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = PriceLensTheme.glassColors.textSecondary
                                    )
                                },
                                trailingIcon = {
                                    if (uiState.searchQuery.isNotBlank()) {
                                        IconButton(onClick = { onEvent(WatchlistUiEvent.SearchQueryChanged("")) }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = PriceLensTheme.glassColors.textSecondary
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = PriceLensTheme.glassColors.surfaceGlass.copy(alpha = 0.5f),
                                    unfocusedContainerColor = PriceLensTheme.glassColors.surfaceGlass.copy(alpha = 0.3f),
                                    focusedBorderColor = ElectricBlue.copy(alpha = 0.6f),
                                    unfocusedBorderColor = PriceLensTheme.glassColors.specularBorder.copy(alpha = 0.5f),
                                    focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                                    unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                                )
                            )
                        }
                    }

                    // Empty filter state
                    if (uiState.filteredItems.isEmpty()) {
                        item(key = "empty_filter_state") {
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (uiState.activeFilter == WatchlistFilter.PRICE_DROPS) Icons.AutoMirrored.Filled.TrendingDown else Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        tint = PriceLensTheme.glassColors.textTertiary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = if (uiState.activeFilter == WatchlistFilter.PRICE_DROPS) "No Price Drops Active Yet" else "No Matching Products",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PriceLensTheme.glassColors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (uiState.activeFilter == WatchlistFilter.PRICE_DROPS) "PriceLens background worker is actively monitoring your saved products for target price drops." else "Try searching with a different keyword or clear the search filter.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PriceLensTheme.glassColors.textSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Watchlist items
                    items(
                        items = uiState.filteredItems,
                        key = { it.productId }
                    ) { item ->
                        WatchlistItemCard(
                            item = item,
                            currencyFormatter = currencyFormatter,
                            onClick = { onNavigateToComparison(item.productId, item.productName) },
                            onDelete = { onEvent(WatchlistUiEvent.RemoveItem(item.productId)) }
                        )
                    }

                    item(key = "footer_spacer") {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchlistTopBar(
    itemCount: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PriceLensTheme.glassColors.surfaceGlass)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back",
                    tint = PriceLensTheme.glassColors.textPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tracked Watchlist",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PriceLensTheme.glassColors.textPrimary
                )
                Text(
                    text = if (itemCount == 1) "1 active price tracker" else "$itemCount active price trackers",
                    style = MaterialTheme.typography.bodySmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElectricBlue.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(MintSuccess)
                    )
                    Text(
                        text = "LIVE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MintSuccess,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun WatchlistStatsCard(
    totalCount: Int,
    priceDropCount: Int,
    activeFilter: WatchlistFilter = WatchlistFilter.ALL,
    onAllClick: () -> Unit = {},
    onPriceDropsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = PriceLensTheme.glassColors.surfaceGlass.copy(alpha = 0.65f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onAllClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = totalCount.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (activeFilter == WatchlistFilter.ALL) ElectricBlue else PriceLensTheme.glassColors.textPrimary
                )
                Text(
                    text = "Total Tracked",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (activeFilter == WatchlistFilter.ALL) FontWeight.Bold else FontWeight.Normal,
                    color = if (activeFilter == WatchlistFilter.ALL) ElectricBlue else PriceLensTheme.glassColors.textSecondary
                )
            }

            Box(
                modifier = Modifier
                    .height(36.dp)
                    .width(1.dp)
                    .background(PriceLensTheme.glassColors.specularBorder.copy(alpha = 0.4f))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onPriceDropsClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (priceDropCount > 0) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = MintSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = priceDropCount.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (priceDropCount > 0) MintSuccess else PriceLensTheme.glassColors.textPrimary
                    )
                }
                Text(
                    text = "Price Drops Active",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (activeFilter == WatchlistFilter.PRICE_DROPS) FontWeight.Bold else FontWeight.Normal,
                    color = if (priceDropCount > 0) MintSuccess else PriceLensTheme.glassColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun WatchlistItemCard(
    item: WatchlistItem,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val target = item.targetPrice
    val current = item.currentLowestPrice
    val isPriceDrop = target != null && current != null && current <= target

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        onClick = onClick,
        borderColor = if (isPriceDrop) MintSuccess.copy(alpha = 0.45f) else PriceLensTheme.glassColors.specularBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = item.productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PriceLensTheme.glassColors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    item.brand?.let { brand ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = brand,
                            style = MaterialTheme.typography.labelSmall,
                            color = PriceLensTheme.glassColors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PriceLensTheme.glassColors.surfaceGlass)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove from watchlist",
                        tint = AmberAlert,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pricing Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current Lowest
                Column {
                    Text(
                        text = "Current Best",
                        style = MaterialTheme.typography.labelSmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                    Text(
                        text = if (current != null) currencyFormatter.format(current) else "Checking...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPriceDrop) MintSuccess else PriceLensTheme.glassColors.textPrimary
                    )
                }

                // Target Alert Price
                target?.let { targetVal ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Target Alert",
                            style = MaterialTheme.typography.labelSmall,
                            color = PriceLensTheme.glassColors.textSecondary
                        )
                        Text(
                            text = currencyFormatter.format(targetVal),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                    }
                }
            }

            // Price Drop Notification Banner
            if (isPriceDrop) {
                val savings = target!! - current!!
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MintSuccess.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MintSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (savings > 0) {
                                "Target met! Price dropped by ${currencyFormatter.format(savings)}"
                            } else {
                                "Target price reached!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MintSuccess
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyWatchlistState(
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(ElectricBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Watchlist is Empty",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PriceLensTheme.glassColors.textPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Set price alerts on your favorite products across Amazon, Flipkart, and more to monitor discounts here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PriceLensTheme.glassColors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                LiquidGlassPrimaryButton(
                    text = "Browse Products",
                    onClick = onBrowseClick,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
        }
    }
}
