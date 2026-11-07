package com.pricelens.presentation.search

import com.pricelens.domain.model.ProductSummary

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val products: List<ProductSummary> = emptyList(),
    val errorMessage: String? = null,
    val isSubmitted: Boolean = false
) {
    val isEmpty: Boolean
        get() = isSubmitted && !isLoading && errorMessage == null && products.isEmpty()
}
