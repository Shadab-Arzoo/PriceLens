package com.pricelens.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class RetailerOffer(
    val retailerId: String,
    val retailerName: String,
    val price: Double?,
    val currency: String = "INR",
    val isAvailable: Boolean,
    val productUrl: String,
    val observedAtTimestamp: Long
)
