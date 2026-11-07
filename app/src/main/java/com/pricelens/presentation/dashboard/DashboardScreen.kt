package com.pricelens.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pricelens.R
import com.pricelens.core.designsystem.ElectricBlue
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.EmptyStateCard
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.designsystem.components.PriceLensSearchBar
import com.pricelens.core.designsystem.components.PriceLensTopBar
import com.pricelens.domain.model.RecentSearch
import com.pricelens.domain.model.WatchlistItem
import com.pricelens.presentation.pasteurl.PasteUrlBottomSheet

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToComparison: (productId: String, productName: String) -> Unit = { _, _ -> },
    onNavigateToOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToScan = onNavigateToScan,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToWatchlist = onNavigateToWatchlist,
        onNavigateToComparison = onNavigateToComparison,
        onNavigateToOnboarding = onNavigateToOnboarding,
        modifier = modifier
    )
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToComparison: (productId: String, productName: String) -> Unit = { _, _ -> },
    onNavigateToOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = PriceLensTheme.glassColors.backgroundBrush)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Top Bar
            item {
                PriceLensTopBar(
                    title = stringResource(R.string.dashboard_title),
                    subtitle = stringResource(R.string.dashboard_subtitle),
                    onWatchlistClick = onNavigateToWatchlist,
                    onTourClick = onNavigateToOnboarding
                )
            }

            // 2. Search Input
            item {
                PriceLensSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { onEvent(DashboardUiEvent.SearchQueryChanged(it)) },
                    onSearch = { query ->
                        if (query.isNotBlank()) {
                            onEvent(DashboardUiEvent.SubmitSearch(query))
                            onNavigateToSearch(query)
                        }
                    }
                )
            }

            // 3. Quick Action Buttons (Scan & Paste URL)
            item {
                QuickActionsRow(
                    onScanClick = onNavigateToScan,
                    onPasteUrlClick = { onEvent(DashboardUiEvent.OpenPasteUrlSheet) }
                )
            }

            // 4. Loading State
            if (uiState.isLoading) {
                item {
                    DashboardLoadingState()
                }
            }

            // 5. Error State
            if (uiState.errorMessage != null && !uiState.isLoading) {
                item {
                    DashboardErrorState(
                        errorMessage = uiState.errorMessage,
                        onRetry = { onEvent(DashboardUiEvent.RetryLoad) }
                    )
                }
            }

            // 6. Main Content Sections
            if (!uiState.isLoading && uiState.errorMessage == null) {
                // Section: Recent Searches
                item {
                    RecentSearchesSection(
                        recentSearches = uiState.recentSearches,
                        onSelectSearch = { query ->
                            onEvent(DashboardUiEvent.RecentSearchSelected(query))
                            onNavigateToSearch(query)
                        },
                        onClearSearches = { onEvent(DashboardUiEvent.ClearRecentSearches) }
                    )
                }

                // Section: Watchlist / Tracked Price Alerts
                item {
                    WatchlistSection(
                        watchlistItems = uiState.watchlistItems,
                        onViewWatchlist = onNavigateToWatchlist
                    )
                }

                // Section: Trending Comparisons
                item {
                    TrendingSection(
                        trendingProducts = uiState.trendingProducts,
                        onProductClick = onNavigateToComparison
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Paste URL Modal Bottom Sheet
        if (uiState.isPasteUrlSheetOpen) {
            PasteUrlBottomSheet(
                urlInput = uiState.pasteUrlInput,
                validationResult = uiState.urlValidation,
                onUrlChange = { onEvent(DashboardUiEvent.PasteUrlInputChanged(it)) },
                onSubmitUrl = {
                    onEvent(DashboardUiEvent.SubmitPastedUrl)
                    // If supported, trigger search/comparison with normalized URL
                    if (uiState.urlValidation is com.pricelens.domain.usecase.UrlValidationResult.Supported) {
                        val supported = uiState.urlValidation as com.pricelens.domain.usecase.UrlValidationResult.Supported
                        onEvent(DashboardUiEvent.ClosePasteUrlSheet)
                        onNavigateToSearch(supported.normalizedUrl)
                    }
                },
                onDismiss = { onEvent(DashboardUiEvent.ClosePasteUrlSheet) }
            )
        }
    }
}

@Composable
private fun QuickActionsRow(
    onScanClick: () -> Unit,
    onPasteUrlClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        QuickActionCard(
            title = stringResource(R.string.scan_barcode_title),
            subtitle = "Barcode & Camera OCR",
            icon = Icons.Default.QrCodeScanner,
            onClick = onScanClick,
            modifier = Modifier.weight(1f)
        )

        QuickActionCard(
            title = stringResource(R.string.paste_url_title),
            subtitle = "Supported Indian Stores",
            icon = Icons.Default.Link,
            onClick = onPasteUrlClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = ElectricBlue.copy(alpha = 0.12f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PriceLensTheme.glassColors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = PriceLensTheme.glassColors.textSecondary
            )
        }
    }
}

@Composable
private fun RecentSearchesSection(
    recentSearches: List<RecentSearch>,
    onSelectSearch: (String) -> Unit,
    onClearSearches: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.section_recent_searches),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PriceLensTheme.glassColors.textPrimary
            )

            if (recentSearches.isNotEmpty()) {
                IconButton(onClick = onClearSearches) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear all searches",
                        tint = PriceLensTheme.glassColors.textTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (recentSearches.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.History,
                title = stringResource(R.string.empty_recent_title),
                subtitle = stringResource(R.string.empty_recent_subtitle)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentSearches, key = { it.query }) { search ->
                    GlassCard(
                        shape = RoundedCornerShape(12.dp),
                        elevation = 1.dp,
                        onClick = { onSelectSearch(search.query) }
                    ) {
                        Text(
                            text = search.query,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PriceLensTheme.glassColors.textPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchlistSection(
    watchlistItems: List<WatchlistItem>,
    onViewWatchlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.section_watchlist),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PriceLensTheme.glassColors.textPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (watchlistItems.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.BookmarkBorder,
                title = stringResource(R.string.empty_watchlist_title),
                subtitle = stringResource(R.string.empty_watchlist_subtitle),
                actionLabel = "Browse Products",
                onActionClick = onViewWatchlist
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                watchlistItems.take(3).forEach { item ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onViewWatchlist
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.productName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                                item.brand?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PriceLensTheme.glassColors.textSecondary
                                    )
                                }
                            }
                            item.currentLowestPrice?.let { price ->
                                Text(
                                    text = "₹%,.0f".format(price),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue
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
private fun TrendingSection(
    trendingProducts: List<com.pricelens.domain.model.ProductSummary>,
    onProductClick: (productId: String, productName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.section_trending),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PriceLensTheme.glassColors.textPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (trendingProducts.isEmpty()) {
            EmptyStateCard(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                title = stringResource(R.string.empty_trending_title),
                subtitle = stringResource(R.string.empty_trending_subtitle)
            )
        } else {
            val currencyFormatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "IN"))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                trendingProducts.forEach { product ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onProductClick(product.id, product.name) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PriceLensTheme.glassColors.textPrimary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${product.brand.orEmpty()} • ${product.retailerCount} stores compared",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PriceLensTheme.glassColors.textSecondary
                                )
                            }
                            if (product.lowestPrice != null) {
                                Text(
                                    text = currencyFormatter.format(product.lowestPrice),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = com.pricelens.core.designsystem.MintSuccess
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
private fun DashboardLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = ElectricBlue,
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.state_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = PriceLensTheme.glassColors.textSecondary
            )
        }
    }
}

@Composable
private fun DashboardErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        EmptyStateCard(
            icon = Icons.Default.ErrorOutline,
            title = stringResource(R.string.state_error_title),
            subtitle = errorMessage,
            actionLabel = stringResource(R.string.state_error_retry),
            onActionClick = onRetry
        )
    }
}
