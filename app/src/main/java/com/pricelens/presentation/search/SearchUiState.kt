package com.pricelens.presentation.search

import androidx.compose.runtime.Immutable
import com.pricelens.domain.model.ProductSummary

enum class SearchSortOrder(val label: String) {
    RELEVANCE("Relevance"),
    PRICE_LOW_TO_HIGH("Lowest Price 🏷️"),
    PRICE_HIGH_TO_LOW("Price: High to Low"),
    MOST_RETAILERS("Most Stores 🏪")
}

enum class SearchPriceRange(val label: String) {
    ALL("All Prices"),
    UNDER_10K("Under ₹10k"),
    FROM_10K_TO_30K("₹10k - ₹30k"),
    FROM_30K_TO_70K("₹30k - ₹70k"),
    ABOVE_70K("₹70k+")
}

@Immutable
data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val products: List<ProductSummary> = emptyList(),
    val sortOrder: SearchSortOrder = SearchSortOrder.RELEVANCE,
    val selectedStoreFilter: String = "All Stores",
    val selectedPriceRange: SearchPriceRange = SearchPriceRange.ALL,
    val errorMessage: String? = null,
    val isSubmitted: Boolean = false
) {
    val sortedProducts: List<ProductSummary>
        get() {
            var list = products

            // Filter by store
            if (selectedStoreFilter != "All Stores") {
                list = list.filter {
                    it.lowestPriceRetailer?.contains(selectedStoreFilter, ignoreCase = true) == true
                }
            }

            // Filter by price range
            list = when (selectedPriceRange) {
                SearchPriceRange.ALL -> list
                SearchPriceRange.UNDER_10K -> list.filter { (it.lowestPrice ?: 0.0) < 10000.0 }
                SearchPriceRange.FROM_10K_TO_30K -> list.filter {
                    val p = it.lowestPrice ?: 0.0
                    p >= 10000.0 && p < 30000.0
                }
                SearchPriceRange.FROM_30K_TO_70K -> list.filter {
                    val p = it.lowestPrice ?: 0.0
                    p >= 30000.0 && p < 70000.0
                }
                SearchPriceRange.ABOVE_70K -> list.filter { (it.lowestPrice ?: 0.0) >= 70000.0 }
            }

            return when (sortOrder) {
                SearchSortOrder.RELEVANCE -> list
                SearchSortOrder.PRICE_LOW_TO_HIGH -> list.sortedBy { it.lowestPrice ?: Double.MAX_VALUE }
                SearchSortOrder.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.lowestPrice ?: 0.0 }
                SearchSortOrder.MOST_RETAILERS -> list.sortedByDescending { it.retailerCount }
            }
        }

    val isEmpty: Boolean
        get() = isSubmitted && !isLoading && errorMessage == null && sortedProducts.isEmpty()
}
