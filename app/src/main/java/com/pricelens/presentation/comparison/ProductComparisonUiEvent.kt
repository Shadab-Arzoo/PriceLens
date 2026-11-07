package com.pricelens.presentation.comparison

sealed interface ProductComparisonUiEvent {
    data object RetryLoad : ProductComparisonUiEvent
    data object RefreshOffers : ProductComparisonUiEvent
}
