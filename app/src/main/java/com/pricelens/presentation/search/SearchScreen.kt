package com.pricelens.presentation.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pricelens.R
import com.pricelens.core.designsystem.ElectricBlue
import com.pricelens.core.designsystem.LocalThemeToggle
import com.pricelens.core.designsystem.MintSuccess
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.EmptyStateCard
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.designsystem.components.PriceLensSearchBar
import com.pricelens.core.designsystem.components.SkeletonBox
import com.pricelens.core.designsystem.components.ThemeToggleSwitch
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

    SearchScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
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
                    uiState.isEmpty -> {
                        SearchEmptyState(query = uiState.query)
                    }
                    else -> {
                        SearchResultsList(
                            products = uiState.sortedProducts,
                            totalCount = uiState.products.size,
                            currentSortOrder = uiState.sortOrder,
                            onSortChange = { onEvent(SearchUiEvent.SortOrderChanged(it)) },
                            onProductClick = onProductClick
                        )
                    }
                }
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
    onProductClick: (productId: String, productName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "search_header_and_sort") {
            Column(
                modifier = Modifier.padding(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Found $totalCount products across 5 retailers",
                    style = MaterialTheme.typography.bodySmall,
                    color = PriceLensTheme.glassColors.textSecondary,
                    fontWeight = FontWeight.Medium
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val sortOptions = listOf(
                        SearchSortOrder.RELEVANCE to "Relevance",
                        SearchSortOrder.PRICE_LOW_TO_HIGH to "Lowest Price 🏷️",
                        SearchSortOrder.PRICE_HIGH_TO_LOW to "Price: High to Low",
                        SearchSortOrder.MOST_RETAILERS to "Most Stores 🏪"
                    )
                    items(sortOptions, key = { it.first }) { (order, label) ->
                        val isSelected = currentSortOrder == order
                        val chipBg by animateColorAsState(
                            targetValue = if (isSelected) ElectricBlue.copy(alpha = 0.18f)
                            else PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.6f),
                            animationSpec = tween(durationMillis = 200),
                            label = "sortChipBg"
                        )
                        val chipText by animateColorAsState(
                            targetValue = if (isSelected) ElectricBlue else PriceLensTheme.glassColors.textSecondary,
                            animationSpec = tween(durationMillis = 200),
                            label = "sortChipText"
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(chipBg)
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricBlue.copy(alpha = 0.6f)
                                    else PriceLensTheme.glassColors.specularBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onSortChange(order) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = chipText
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
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

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
            // Product Icon Container with Liquid Glass treatment
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(14.dp),
                color = ElectricBlue.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    PriceLensTheme.glassColors.specularBorder
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
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

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Lowest Price
                    if (product.lowestPrice != null) {
                        Text(
                            text = currencyFormatter.format(product.lowestPrice),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MintSuccess
                        )
                    }

                    // Retailer Count Pill
                    if (product.retailerCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = PriceLensTheme.glassColors.surfaceHighlight
                        ) {
                            Text(
                                text = "${product.retailerCount} stores",
                                style = MaterialTheme.typography.labelSmall,
                                color = PriceLensTheme.glassColors.textSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
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
