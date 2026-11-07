package com.pricelens.domain.model

/**
 * Domain model representing comprehensive comparison details for a product across Indian retailers.
 */
data class ProductComparisonDetails(
    val id: String,
    val name: String,
    val brand: String? = null,
    val modelNumber: String? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val specifications: Map<String, String> = emptyMap(),
    val offers: List<RetailerOffer> = emptyList(),
    val unavailableRetailers: List<UnavailableRetailer> = emptyList(),
    val priceHistory: List<PriceHistoryPoint> = emptyList(),
    val lowestPriceOffer: RetailerOffer? = null,
    val confidenceScore: Double = 1.0,
    val lastRefreshedTimestamp: Long = 0L
)
