package com.pricelens.data.repository

import com.pricelens.domain.model.ProductSummary
import com.pricelens.domain.model.RetailerOffer
import com.pricelens.domain.repository.ProductCatalogRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductCatalogRepositoryImpl @Inject constructor() : ProductCatalogRepository {

    override suspend fun searchProducts(query: String): Result<List<ProductSummary>> {
        // Ready for authorized provider adapter in Stage 2.
        // Returns empty list when no live provider is connected; strictly never fabricates mock items or prices.
        return Result.success(emptyList())
    }

    override suspend fun getProductOffers(productId: String): Result<List<RetailerOffer>> {
        // Real offers will be populated from authorized partner feeds once connected.
        return Result.success(emptyList())
    }

    override suspend fun getTrendingProducts(): Result<List<ProductSummary>> {
        // Trending comparisons require real historical observation data; returns empty until observations exist.
        return Result.success(emptyList())
    }
}
