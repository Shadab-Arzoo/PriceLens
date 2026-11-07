package com.pricelens.domain.usecase

import com.pricelens.domain.model.ProductSummary
import com.pricelens.domain.repository.ProductCatalogRepository
import javax.inject.Inject

class SearchProductsUseCase @Inject constructor(
    private val productCatalogRepository: ProductCatalogRepository,
    private val saveRecentSearchUseCase: SaveRecentSearchUseCase
) {
    suspend operator fun invoke(query: String): Result<List<ProductSummary>> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return Result.success(emptyList())
        }
        // Record recent search
        saveRecentSearchUseCase(trimmed)
        return productCatalogRepository.searchProducts(trimmed)
    }
}
