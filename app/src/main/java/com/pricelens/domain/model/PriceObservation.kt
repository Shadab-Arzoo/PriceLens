package com.pricelens.domain.model

/**
 * An immutable observation received from an authorized pricing source.
 * It is never used as a fallback or seed for historical commerce data.
 */
data class PriceObservation(
    val id: String,
    val productId: String,
    val retailerId: String,
    val retailerName: String,
    val price: Double,
    val currency: String,
    val observedAt: Long
) {
    init {
        require(id.isNotBlank())
        require(productId.isNotBlank())
        require(retailerId.isNotBlank())
        require(retailerName.isNotBlank())
        require(price.isFinite() && price >= 0.0)
        require(currency.isNotBlank())
        require(observedAt > 0L)
    }
}
