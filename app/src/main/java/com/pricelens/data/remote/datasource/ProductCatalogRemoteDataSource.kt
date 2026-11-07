package com.pricelens.data.remote.datasource

import com.pricelens.domain.model.ProductComparisonDetails
import com.pricelens.domain.model.ProductSummary
import com.pricelens.domain.model.RetailerOffer

interface ProductCatalogRemoteDataSource {
    suspend fun searchProducts(query: String): Result<List<ProductSummary>>
    suspend fun getProductOffers(productId: String): Result<List<RetailerOffer>>
    suspend fun getProductComparison(productId: String): Result<ProductComparisonDetails>
    suspend fun getTrendingProducts(): Result<List<ProductSummary>>
}
