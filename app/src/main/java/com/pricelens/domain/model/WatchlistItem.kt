package com.pricelens.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class WatchlistItem(
    val productId: String,
    val productName: String,
    val brand: String? = null,
    val targetPrice: Double? = null,
    val currentLowestPrice: Double? = null,
    val currency: String = "INR",
    val addedAtTimestamp: Long,
    val websiteUrl: String? = null,
    val imageUrl: String? = null
)
