package com.pricelens.presentation.search

import com.pricelens.domain.model.ProductSummary

enum class SearchSortOrder {
    RELEVANCE,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    MOST_RETAILERS
}

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val products: List<ProductSummary> = emptyList(),
    val sortOrder: SearchSortOrder = SearchSortOrder.RELEVANCE,
    val errorMessage: String? = null,
    val isSubmitted: Boolean = false
) {
    val sortedProducts: List<ProductSummary>
        get() = when (sortOrder) {
            SearchSortOrder.RELEVANCE -> products
            SearchSortOrder.PRICE_LOW_TO_HIGH -> products.sortedBy { it.lowestPrice ?: Double.MAX_VALUE }
            SearchSortOrder.PRICE_HIGH_TO_LOW -> products.sortedByDescending { it.lowestPrice ?: 0.0 }
            SearchSortOrder.MOST_RETAILERS -> products.sortedByDescending { it.retailerCount }
        }

    val isEmpty: Boolean
        get() = isSubmitted && !isLoading && errorMessage == null && products.isEmpty()
}
