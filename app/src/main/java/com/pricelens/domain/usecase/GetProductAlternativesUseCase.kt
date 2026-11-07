package com.pricelens.domain.usecase

import com.pricelens.domain.model.ProductAlternative
import com.pricelens.domain.repository.ProductCatalogRepository
import javax.inject.Inject

/**
 * Use case to retrieve ranked product alternatives based on normalized catalog specifications
 * and price comparison.
 */
class GetProductAlternativesUseCase @Inject constructor(
    private val productCatalogRepository: ProductCatalogRepository
) {
    suspend operator fun invoke(productId: String): Result<List<ProductAlternative>> {
        val trimmed = productId.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Product ID cannot be blank"))
        }
        return productCatalogRepository.getProductAlternatives(trimmed)
    }
}
