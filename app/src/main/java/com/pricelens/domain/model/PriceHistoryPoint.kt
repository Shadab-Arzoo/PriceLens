package com.pricelens.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class PriceHistoryPoint(
    val timestamp: Long,
    val price: Double,
    val currency: String = "INR",
    val retailerName: String
)
