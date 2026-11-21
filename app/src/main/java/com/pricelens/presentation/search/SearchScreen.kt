package com.pricelens.presentation.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.ui.unit.sp
import com.pricelens.core.designsystem.AmberAlert
import com.pricelens.core.designsystem.CyanAccent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pricelens.R
import com.pricelens.core.designsystem.ElectricBlue
import com.pricelens.core.designsystem.LocalThemeToggle
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.EmptyStateCard
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.designsystem.components.PriceLensSearchBar
import com.pricelens.core.designsystem.components.SkeletonBox
import com.pricelens.core.designsystem.components.ThemeToggleSwitch
import com.pricelens.core.util.PriceLensFormatters
import com.pricelens.domain.model.ProductSummary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SearchRoute(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit,
    onProductClick: (productId: String, productName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onEvent = androidx.compose.runtime.remember(viewModel) { { event: SearchUiEvent -> viewModel.onEvent(event) } }

    SearchScreen(
        uiState = uiState,
        onEvent = onEvent,
        onNavigateBack = onNavigateBack,
        onProductClick = onProductClick,
        modifier = modifier
    )
}

@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onEvent: (SearchUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onProductClick: (productId: String, productName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedProducts = androidx.compose.runtime.remember(
        uiState.products,
        uiState.selectedStoreFilter,
        uiState.selectedPriceRange,
        uiState.sortOrder
    ) {
        uiState.sortedProducts
    }
    val isResultsEmpty = uiState.isSubmitted && !uiState.isLoading && uiState.errorMessage == null && sortedProducts.isEmpty()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header with Back Button
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
                    text = stringResource(R.string.search_screen_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PriceLensTheme.glassColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )

                ThemeToggleSwitch(
                    isDark = PriceLensTheme.isDark,
                    onToggle = LocalThemeToggle.current
                )
            }

            // Search Bar
            Box(modifier = Modifier.padding(bottom = 12.dp)) {
                PriceLensSearchBar(
                    query = uiState.query,
                    onQueryChange = { onEvent(SearchUiEvent.QueryChanged(it)) },
                    onSearch = { onEvent(SearchUiEvent.SubmitSearch(it)) }
                )
            }

            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading -> {
                        SearchLoadingState()
                    }
                    uiState.errorMessage != null -> {
                        SearchErrorState(
                            errorMessage = uiState.errorMessage,
                            onRetry = { onEvent(SearchUiEvent.RetrySearch) }
                        )
                    }
                    isResultsEmpty -> {
                        SearchEmptyState(query = uiState.query)
                    }
                    !uiState.isSubmitted && uiState.products.isEmpty() -> {
                        SearchSuggestionsSection(
                            onSelectQuery = {
                                onEvent(SearchUiEvent.QueryChanged(it))
                                onEvent(SearchUiEvent.SubmitSearch(it))
                            }
                        )
                    }
                    else -> {
                        SearchResultsList(
                            products = sortedProducts,
                            totalCount = sortedProducts.size,
                            currentSortOrder = uiState.sortOrder,
                            onSortChange = { onEvent(SearchUiEvent.SortOrderChanged(it)) },
                            selectedStoreFilter = uiState.selectedStoreFilter,
                            onStoreFilterChange = { onEvent(SearchUiEvent.StoreFilterChanged(it)) },
                            selectedPriceRange = uiState.selectedPriceRange,
                            onPriceRangeChange = { onEvent(SearchUiEvent.PriceRangeChanged(it)) },
                            onProductClick = onProductClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun SearchSuggestionsSection(
    onSelectQuery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val trendingSearches = listOf(
        "Apple iPhone 16",
        "MacBook Air M3",
        "Sony WH-1000XM5",
        "Samsung Galaxy S24",
        "Apple AirPods Pro 2",
        "OnePlus 12 5G",
        "Bose QuietComfort Ultra",
        "Dell XPS 13 OLED"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Trending Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = AmberAlert,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "TRENDING SEARCHES IN INDIA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = PriceLensTheme.glassColors.textSecondary
            )
        }

        // Trending Chips FlowRow
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            trendingSearches.forEach { search ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder),
                    modifier = Modifier.clickable { onSelectQuery(search) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = search,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = PriceLensTheme.glassColors.textPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Store Coverage Tip Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "🏪 Real-Time Multi-Retailer Compare",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PriceLensTheme.glassColors.textPrimary
                )
                Text(
                    text = "Search any gadget or consumer product to compare prices across Amazon India, Flipkart, Croma, Reliance Digital, and Vijay Sales simultaneously with verified partner discounts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun SearchResultsList(
    products: List<ProductSummary>,
    totalCount: Int,
    currentSortOrder: SearchSortOrder,
    onSortChange: (SearchSortOrder) -> Unit,
    selectedStoreFilter: String,
    onStoreFilterChange: (String) -> Unit,
    selectedPriceRange: SearchPriceRange,
    onPriceRangeChange: (SearchPriceRange) -> Unit,
    onProductClick: (productId: String, productName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "search_header_and_sort") {
            Column(
                modifier = Modifier.padding(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$totalCount products found",
                        style = MaterialTheme.typography.bodySmall,
                        color = PriceLensTheme.glassColors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    if (selectedStoreFilter != "All Stores" || selectedPriceRange != SearchPriceRange.ALL) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f),
                            modifier = Modifier.clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onStoreFilterChange("All Stores")
                                onPriceRangeChange(SearchPriceRange.ALL)
                            }
                        ) {
                            Text(
                                text = "Reset Filters ✕",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.ambientGlow,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Sort Options
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SearchSortOrder.entries, key = { it.name }) { order ->
                        val isSelected = currentSortOrder == order
                        val chipBg by animateColorAsState(
                            targetValue = if (isSelected) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.2f)
                            else PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.6f),
                            animationSpec = tween(durationMillis = 200),
                            label = "sortChipBg"
                        )
                        val chipText by animateColorAsState(
                            targetValue = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textSecondary,
                            animationSpec = tween(durationMillis = 200),
                            label = "sortChipText"
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(chipBg)
                                .border(
                                    1.dp,
                                    if (isSelected) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.6f)
                                    else PriceLensTheme.glassColors.specularBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSortChange(order)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = order.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = chipText
                            )
                        }
                    }
                }

                // Store Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val storeList = listOf("All Stores", "Amazon India", "Flipkart", "Croma", "Reliance Digital", "Vijay Sales")
                    items(storeList, key = { it }) { store ->
                        val isSelected = selectedStoreFilter == store
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.18f) else PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder),
                            modifier = Modifier.clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onStoreFilterChange(store)
                            }
                        ) {
                            Text(
                                text = store,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Price Range Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SearchPriceRange.entries, key = { it.name }) { range ->
                        val isSelected = selectedPriceRange == range
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.18f) else PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder),
                            modifier = Modifier.clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPriceRangeChange(range)
                            }
                        ) {
                            Text(
                                text = range.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        items(
            items = products,
            key = { it.id }
        ) { product ->
            ProductSummaryCard(
                product = product,
                onClick = { onProductClick(product.id, product.name) },
                modifier = Modifier.animateItem()
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProductSummaryCard(
    product: ProductSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spreadSavings = if (product.highestPrice != null && product.lowestPrice != null && product.highestPrice > product.lowestPrice) {
        product.highestPrice - product.lowestPrice
    } else 0.0

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image Thumbnail
            com.pricelens.core.designsystem.components.ProductThumbnailImage(
                imageUrl = product.imageUrl,
                productName = product.name,
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!product.brand.isNullOrBlank()) {
                    Text(
                        text = product.brand.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = PriceLensTheme.glassColors.textTertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PriceLensTheme.glassColors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Price and Store Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (product.lowestPrice != null) {
                        Text(
                            text = PriceLensFormatters.formatInr(product.lowestPrice),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PriceLensTheme.glassColors.ambientGlow
                        )
                    }

                    if (product.lowestPriceRetailer != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = product.lowestPriceRetailer,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.ambientGlow,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Retailer count & spread savings
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (product.retailerCount > 0) {
                        Text(
                            text = "${product.retailerCount} stores",
                            style = MaterialTheme.typography.labelSmall,
                            color = PriceLensTheme.glassColors.textSecondary
                        )
                    }

                    if (spreadSavings > 0) {
                        Text(
                            text = "• Save up to ${PriceLensFormatters.formatInr(spreadSavings)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = PriceLensTheme.glassColors.ambientGlow
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "View comparison",
                tint = PriceLensTheme.glassColors.textTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SearchLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(4) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonBox(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        height = 56.dp,
                        width = 56.dp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        SkeletonBox(height = 16.dp, width = 180.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        SkeletonBox(height = 12.dp, width = 120.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        SkeletonBox(height = 18.dp, width = 85.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchErrorState(
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

@Composable
private fun SearchEmptyState(
    query: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        EmptyStateCard(
            icon = Icons.Default.SearchOff,
            title = stringResource(R.string.search_empty_title),
            subtitle = if (query.isNotBlank()) {
                "No authorized offers found for \"$query\". Check the spelling or try searching another model."
            } else {
                stringResource(R.string.search_empty_subtitle)
            }
        )
    }
}
