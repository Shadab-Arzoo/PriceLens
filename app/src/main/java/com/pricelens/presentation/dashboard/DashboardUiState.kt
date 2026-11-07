package com.pricelens.presentation.dashboard

import com.pricelens.domain.model.ProductSummary
import com.pricelens.domain.model.RecentSearch
import com.pricelens.domain.model.WatchlistItem
import com.pricelens.domain.usecase.UrlValidationResult

data class DashboardUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val recentSearches: List<RecentSearch> = emptyList(),
    val watchlistItems: List<WatchlistItem> = emptyList(),
    val trendingProducts: List<ProductSummary> = emptyList(),
    val errorMessage: String? = null,
    val isPasteUrlSheetOpen: Boolean = false,
    val pasteUrlInput: String = "",
    val urlValidation: UrlValidationResult? = null
)
