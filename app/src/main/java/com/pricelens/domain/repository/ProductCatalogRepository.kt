package com.pricelens.domain.repository

import com.pricelens.domain.model.ProductSummary
import com.pricelens.domain.model.RetailerOffer

interface ProductCatalogRepository {
    suspend fun searchProducts(query: String): Result<List<ProductSummary>>
    suspend fun getProductOffers(productId: String): Result<List<RetailerOffer>>
    suspend fun getTrendingProducts(): Result<List<ProductSummary>>
}
