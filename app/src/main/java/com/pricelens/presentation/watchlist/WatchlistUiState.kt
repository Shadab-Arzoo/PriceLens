package com.pricelens.presentation.watchlist

import com.pricelens.domain.model.WatchlistItem

data class WatchlistUiState(
    val isLoading: Boolean = false,
    val items: List<WatchlistItem> = emptyList(),
    val searchQuery: String = "",
    val notificationMessage: String? = null
) {
    val filteredItems: List<WatchlistItem>
        get() = if (searchQuery.isBlank()) items else {
            items.filter {
                it.productName.contains(searchQuery, ignoreCase = true) ||
                    (it.brand?.contains(searchQuery, ignoreCase = true) == true)
            }
        }

    val totalTrackedCount: Int
        get() = items.size

    val priceDropCount: Int
        get() = items.count { item ->
            val target = item.targetPrice
            val current = item.currentLowestPrice
            target != null && current != null && current <= target
        }
}
