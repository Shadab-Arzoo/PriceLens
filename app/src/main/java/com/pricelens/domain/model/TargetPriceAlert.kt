package com.pricelens.domain.model

data class TargetPriceAlert(
    val id: String,
    val productId: String,
    val targetPrice: Double,
    val currency: String,
    val isActive: Boolean = true,
    val lastEvaluatedObservationId: String? = null,
    val reachedAt: Long? = null,
    val retailerName: String? = null,
    val websiteUrl: String? = null
) {
    init {
        require(id.isNotBlank())
        require(productId.isNotBlank())
        require(targetPrice.isFinite() && targetPrice >= 0.0)
        require(currency.isNotBlank())
    }
}

data class NotificationRecord(
    val id: String,
    val alertId: String,
    val observationId: String,
    val createdAt: Long,
    val delivered: Boolean
)
