package com.pricelens.domain.model

data class ProductSummary(
    val id: String,
    val name: String,
    val brand: String? = null,
    val modelNumber: String? = null,
    val imageUrl: String? = null,
    val lowestPrice: Double? = null,
    val currency: String = "INR",
    val retailerCount: Int = 0
)
