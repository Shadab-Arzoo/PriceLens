package com.pricelens.data.repository

import com.pricelens.data.remote.datasource.ProductCatalogRemoteDataSource
import com.pricelens.domain.model.ProductComparisonDetails
import com.pricelens.domain.model.ProductSummary
import com.pricelens.domain.model.RetailerOffer
import com.pricelens.domain.repository.ProductCatalogRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductCatalogRepositoryImpl @Inject constructor(
    private val remoteDataSource: ProductCatalogRemoteDataSource
) : ProductCatalogRepository {

    override suspend fun searchProducts(query: String): Result<List<ProductSummary>> {
        return remoteDataSource.searchProducts(query)
    }

    override suspend fun getProductOffers(productId: String): Result<List<RetailerOffer>> {
        return remoteDataSource.getProductOffers(productId)
    }

    override suspend fun getProductComparison(productId: String): Result<ProductComparisonDetails> {
        return remoteDataSource.getProductComparison(productId)
    }

    override suspend fun getTrendingProducts(): Result<List<ProductSummary>> {
        return remoteDataSource.getTrendingProducts()
    }
}
