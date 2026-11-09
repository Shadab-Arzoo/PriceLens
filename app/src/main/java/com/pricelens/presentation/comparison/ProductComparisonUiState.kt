package com.pricelens.presentation.comparison

import com.pricelens.domain.model.ProductComparisonDetails
import com.pricelens.domain.model.TargetPriceAlert

data class ProductComparisonUiState(
    val productId: String = "",
    val productName: String = "",
    val isLoading: Boolean = false,
    val comparisonDetails: ProductComparisonDetails? = null,
    val errorMessage: String? = null,
    val activeAlerts: List<TargetPriceAlert> = emptyList(),
    val isTrackingDialogVisible: Boolean = false,
    val trackingTargetRetailer: String? = null,
    val trackingTargetPrice: String = "",
    val trackingWebsiteUrl: String = "",
    val userNotificationBanner: String? = null
)
