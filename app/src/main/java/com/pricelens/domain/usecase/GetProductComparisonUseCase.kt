package com.pricelens.domain.usecase

import com.pricelens.domain.model.ProductComparisonDetails
import com.pricelens.domain.repository.ProductCatalogRepository
import javax.inject.Inject

class GetProductComparisonUseCase @Inject constructor(
    private val productCatalogRepository: ProductCatalogRepository
) {
    suspend operator fun invoke(productId: String): Result<ProductComparisonDetails> {
        val trimmed = productId.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Product ID cannot be blank"))
        }
        return productCatalogRepository.getProductComparison(trimmed)
    }
}
