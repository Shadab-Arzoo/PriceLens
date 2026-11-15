package com.pricelens.presentation.watchlist

import com.pricelens.domain.model.WatchlistItem

enum class WatchlistFilter {
    ALL,
    PRICE_DROPS
}

data class WatchlistUiState(
    val isLoading: Boolean = false,
    val items: List<WatchlistItem> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: WatchlistFilter = WatchlistFilter.ALL,
    val notificationMessage: String? = null
) {
    val filteredItems: List<WatchlistItem>
        get() {
            val baseList = when (activeFilter) {
                WatchlistFilter.ALL -> items
                WatchlistFilter.PRICE_DROPS -> items.filter { item ->
                    val target = item.targetPrice
                    val current = item.currentLowestPrice
                    target != null && current != null && current <= target
                }
            }
            return if (searchQuery.isBlank()) baseList else {
                baseList.filter {
                    it.productName.contains(searchQuery, ignoreCase = true) ||
                        (it.brand?.contains(searchQuery, ignoreCase = true) == true)
                }
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
