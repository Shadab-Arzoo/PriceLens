package com.pricelens.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pricelens.R
import com.pricelens.core.designsystem.ElectricBlue
import com.pricelens.core.designsystem.MintSuccess
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.DashboardSkeleton
import com.pricelens.core.designsystem.components.EmptyStateCard
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.designsystem.components.LiquidGlassPrimaryButton
import com.pricelens.core.designsystem.components.PriceLensSearchBar
import com.pricelens.core.designsystem.components.PriceLensTopBar
import com.pricelens.domain.model.RecentSearch
import com.pricelens.domain.model.WatchlistItem
import com.pricelens.presentation.pasteurl.PasteUrlBottomSheet
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel,
    onNavigateToSearch: (String) -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToComparison: (productId: String, productName: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToWatchlist = onNavigateToWatchlist,
        onNavigateToComparison = onNavigateToComparison,
        modifier = modifier
    )
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToComparison: (productId: String, productName: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var dismissedClipboardUrl by rememberSaveable { mutableStateOf<String?>(null) }
    val clipboardText = remember(clipboardManager) {
        clipboardManager.getText()?.text?.trim().orEmpty()
    }
    val isClipboardProductUrl = remember(clipboardText) {
        val lower = clipboardText.lowercase()
        (lower.startsWith("http://") || lower.startsWith("https://")) &&
            (lower.contains("amazon.") || lower.contains("amzn.") ||
             lower.contains("flipkart.") || lower.contains("myntra.") ||
             lower.contains("ajio.") || lower.contains("croma.") ||
             lower.contains("tatacliq.") || lower.contains("jiomart."))
    }

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
                    onWatchlistClick = onNavigateToWatchlist
                )
            }

            // 2. Search Input Bar
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

            // 2b. Auto-Detected Clipboard URL Banner
            if (isClipboardProductUrl && clipboardText != dismissedClipboardUrl) {
                item(key = "clipboard_detected_banner") {
                    ClipboardDetectedBanner(
                        url = clipboardText,
                        onCompare = {
                            onEvent(DashboardUiEvent.OpenPasteUrlSheet)
                            onEvent(DashboardUiEvent.PasteUrlInputChanged(clipboardText))
                        },
                        onDismiss = { dismissedClipboardUrl = clipboardText }
                    )
                }
            }

            // 3. Hero Action: Compare by Store Link (Direct Link-First Experience)
            item {
                PasteLinkHeroCard(
                    onOpenPasteSheet = { onEvent(DashboardUiEvent.OpenPasteUrlSheet) }
                )
            }

            // 4. Loading State with Fluid Skeleton
            if (uiState.isLoading) {
                item {
                    DashboardSkeleton()
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
                        onViewWatchlist = onNavigateToWatchlist,
                        onProductClick = onNavigateToComparison
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
                confirmedIdentity = uiState.confirmedIdentity,
                onUrlChange = { onEvent(DashboardUiEvent.PasteUrlInputChanged(it)) },
                onIdentityChange = { name, brand, model ->
                    onEvent(DashboardUiEvent.ConfirmedIdentityChanged(name, brand, model))
                },
                onSubmitConfirmedUrl = { confirmed ->
                    onEvent(DashboardUiEvent.ConfirmProductAndNavigate(confirmed))
                    if (!confirmed.resolvedProductId.isNullOrBlank()) {
                        onNavigateToComparison(confirmed.resolvedProductId, confirmed.effectiveTitle)
                    } else {
                        onNavigateToSearch(confirmed.effectiveTitle)
                    }
                },
                onSubmitUrl = {
                    onEvent(DashboardUiEvent.SubmitPastedUrl)
                    if (uiState.urlValidation is com.pricelens.domain.usecase.UrlValidationResult.Supported) {
                        val supported = uiState.urlValidation as com.pricelens.domain.usecase.UrlValidationResult.Supported
                        val identity = supported.suggestedIdentity
                        if (identity != null && !identity.resolvedProductId.isNullOrBlank()) {
                            onEvent(DashboardUiEvent.ClosePasteUrlSheet)
                            onNavigateToComparison(identity.resolvedProductId, identity.effectiveTitle)
                        } else {
                            onEvent(DashboardUiEvent.ClosePasteUrlSheet)
                            onNavigateToSearch(supported.normalizedUrl)
                        }
                    }
                },
                onDismiss = { onEvent(DashboardUiEvent.ClosePasteUrlSheet) }
            )
        }
    }
}

/**
 * Premier Hero Card for Pasting Links and Instant Comparison
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PasteLinkHeroCard(
    onOpenPasteSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconPulse = rememberInfiniteTransition(label = "heroIconPulse")
    val pulseScale by iconPulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(22.dp),
        onClick = onOpenPasteSheet,
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                            }
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(ElectricBlue, Color(0xFF4F46E5))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Compare by Product Link",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textPrimary
                        )
                        Text(
                            text = "Paste any URL from 5 verified Indian retailers",
                            style = MaterialTheme.typography.bodySmall,
                            color = PriceLensTheme.glassColors.textSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ElectricBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Link Input",
                        tint = ElectricBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Simulated interactive paste bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PriceLensTheme.glassColors.surfaceHighlight)
                    .border(1.dp, PriceLensTheme.glassColors.specularBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "amazon.in, flipkart.com, croma.com…",
                    style = MaterialTheme.typography.bodySmall,
                    color = PriceLensTheme.glassColors.textTertiary
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PriceLensTheme.glassColors.primaryGradientBrush)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Paste & Track",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Supported store pills
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Amazon", "Flipkart", "Croma", "Reliance Digital", "Vijay Sales").forEach { store ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.6f))
                            .border(0.5.dp, PriceLensTheme.glassColors.specularBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = store,
                            style = MaterialTheme.typography.labelSmall,
                            color = PriceLensTheme.glassColors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
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
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                        shape = RoundedCornerShape(14.dp),
                        elevation = 1.dp,
                        onClick = { onSelectSearch(search.query) }
                    ) {
                        Text(
                            text = search.query,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
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
private fun ClipboardDetectedBanner(
    url: String,
    onCompare: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        backgroundColor = ElectricBlue.copy(alpha = 0.12f),
        borderColor = ElectricBlue.copy(alpha = 0.45f),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ElectricBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Product link in clipboard",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PriceLensTheme.glassColors.textPrimary
                )
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = PriceLensTheme.glassColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            LiquidGlassPrimaryButton(
                text = "Compare",
                onClick = onCompare,
                modifier = Modifier.height(36.dp)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(30.dp)
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

@Composable
private fun WatchlistSection(
    watchlistItems: List<WatchlistItem>,
    onViewWatchlist: () -> Unit,
    onProductClick: (productId: String, productName: String) -> Unit,
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
                text = stringResource(R.string.section_watchlist),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PriceLensTheme.glassColors.textPrimary
            )
            if (watchlistItems.isNotEmpty()) {
                Text(
                    text = "${watchlistItems.size} items",
                    style = MaterialTheme.typography.labelSmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )
            }
        }

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
                        onClick = { onProductClick(item.productId, item.productName) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = item.productName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PriceLensTheme.glassColors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
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
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PriceLensTheme.glassColors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
                                    color = MintSuccess
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
