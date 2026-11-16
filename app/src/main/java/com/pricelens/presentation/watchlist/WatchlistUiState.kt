package com.pricelens.presentation.watchlist

import com.pricelens.domain.model.WatchlistItem

enum class WatchlistFilter {
    ALL,
    PRICE_DROPS
}

enum class WatchlistSortOrder {
    LATEST,
    PRICE_ASC,
    PRICE_DESC,
    BIGGEST_SAVINGS
}

data class WatchlistUiState(
    val isLoading: Boolean = false,
    val items: List<WatchlistItem> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: WatchlistFilter = WatchlistFilter.ALL,
    val sortOrder: WatchlistSortOrder = WatchlistSortOrder.LATEST,
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
            val searched = if (searchQuery.isBlank()) baseList else {
                baseList.filter {
                    it.productName.contains(searchQuery, ignoreCase = true) ||
                        (it.brand?.contains(searchQuery, ignoreCase = true) == true)
                }
            }
            return when (sortOrder) {
                WatchlistSortOrder.LATEST -> searched
                WatchlistSortOrder.PRICE_ASC -> searched.sortedBy { it.currentLowestPrice ?: Double.MAX_VALUE }
                WatchlistSortOrder.PRICE_DESC -> searched.sortedByDescending { it.currentLowestPrice ?: 0.0 }
                WatchlistSortOrder.BIGGEST_SAVINGS -> searched.sortedByDescending { item ->
                    val target = item.targetPrice ?: 0.0
                    val current = item.currentLowestPrice ?: 0.0
                    maxOf(0.0, target - current)
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

    val totalPotentialSavings: Double
        get() = items.sumOf { item ->
            val target = item.targetPrice ?: 0.0
            val current = item.currentLowestPrice ?: 0.0
            if (target > 0.0 && current > 0.0 && current <= target) {
                target - current
            } else 0.0
        }
}
