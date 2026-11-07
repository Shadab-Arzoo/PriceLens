package com.pricelens.domain.model

/**
 * Domain model representing a retailer that explicitly does not offer or has run out of stock for a product.
 */
data class UnavailableRetailer(
    val retailerId: String,
    val retailerName: String,
    val reason: String
)
