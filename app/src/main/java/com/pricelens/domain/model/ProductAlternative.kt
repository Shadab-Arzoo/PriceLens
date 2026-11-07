package com.pricelens.domain.model

/**
 * Domain model representing an alternative product with specification comparison
 * and price difference explanations.
 */
data class ProductAlternative(
    val productId: String,
    val title: String,
    val brand: String? = null,
    val modelNumber: String? = null,
    val imageUrl: String? = null,
    val lowestPrice: Double? = null,
    val currency: String = "INR",
    val currentProductPrice: Double? = null,
    val priceDifference: Double? = null, // Negative means alternative is cheaper (e.g., -6901.0)
    val isCheaper: Boolean = false,
    val percentageDifference: Double? = null,
    val meaningfulDifferences: List<MeaningfulDifference> = emptyList(),
    val specifications: Map<String, String> = emptyMap(),
    val rankingScore: Double = 0.0
)

/**
 * Represents a specific, user-visible meaningful difference between the current product
 * and an alternative.
 */
data class MeaningfulDifference(
    val category: String, // e.g., "Price", "Battery", "Display", "Processor", "Weight"
    val currentProductValue: String,
    val alternativeValue: String,
    val explanation: String,
    val isAdvantage: Boolean // true if the alternative holds an advantage
)
