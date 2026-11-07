package com.pricelens.presentation.comparison

sealed interface ProductComparisonUiEvent {
    data object RetryLoad : ProductComparisonUiEvent
    data object RefreshOffers : ProductComparisonUiEvent
    data class SelectAlternative(val productId: String, val productName: String) : ProductComparisonUiEvent
}
