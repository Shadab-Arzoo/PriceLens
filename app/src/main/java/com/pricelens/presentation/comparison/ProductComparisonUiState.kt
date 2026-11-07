package com.pricelens.presentation.comparison

import com.pricelens.domain.model.ProductComparisonDetails

data class ProductComparisonUiState(
    val productId: String = "",
    val productName: String = "",
    val isLoading: Boolean = false,
    val comparisonDetails: ProductComparisonDetails? = null,
    val errorMessage: String? = null
)
