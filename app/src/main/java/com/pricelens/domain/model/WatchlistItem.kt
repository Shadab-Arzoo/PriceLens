package com.pricelens.domain.model

data class WatchlistItem(
    val productId: String,
    val productName: String,
    val brand: String? = null,
    val targetPrice: Double? = null,
    val currentLowestPrice: Double? = null,
    val currency: String = "INR",
    val addedAtTimestamp: Long
)
