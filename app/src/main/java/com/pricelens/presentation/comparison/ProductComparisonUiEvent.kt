package com.pricelens.presentation.comparison

sealed interface ProductComparisonUiEvent {
    data object RetryLoad : ProductComparisonUiEvent
    data object RefreshOffers : ProductComparisonUiEvent
    data class SelectAlternative(val productId: String, val productName: String) : ProductComparisonUiEvent
    data class OpenTrackPriceDialog(
        val retailerName: String? = null,
        val currentPrice: Double? = null,
        val websiteUrl: String? = null
    ) : ProductComparisonUiEvent
    data object DismissTrackPriceDialog : ProductComparisonUiEvent
    data class TargetPriceChanged(val targetPrice: String) : ProductComparisonUiEvent
    data class TargetRetailerChanged(val retailerName: String) : ProductComparisonUiEvent
    data class TargetWebsiteUrlChanged(val url: String) : ProductComparisonUiEvent
    data object SubmitTrackPrice : ProductComparisonUiEvent
    data class DeletePriceAlert(val alertId: String) : ProductComparisonUiEvent
    data object DismissBanner : ProductComparisonUiEvent
}
