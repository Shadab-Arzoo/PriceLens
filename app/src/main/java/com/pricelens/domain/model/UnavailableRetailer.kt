package com.pricelens.domain.model

import androidx.compose.runtime.Immutable

/**
 * Domain model representing a retailer that explicitly does not offer or has run out of stock for a product.
 */
@Immutable
data class UnavailableRetailer(
    val retailerId: String,
    val retailerName: String,
    val reason: String
)
