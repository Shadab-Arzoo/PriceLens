package com.pricelens.domain.model

data class PriceHistoryPoint(
    val timestamp: Long,
    val price: Double,
    val currency: String = "INR",
    val retailerName: String
)
