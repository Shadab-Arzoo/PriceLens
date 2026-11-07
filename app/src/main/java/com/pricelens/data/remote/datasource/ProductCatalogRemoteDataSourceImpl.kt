package com.pricelens.data.remote.datasource

import com.pricelens.data.remote.adapter.RetailerPricingAdapter
import com.pricelens.data.remote.model.AdapterResult
import com.pricelens.data.remote.model.ProductOffersDto
import com.pricelens.data.remote.model.ProductSummaryDto
import com.pricelens.data.remote.model.RetailerOfferDto
import com.pricelens.data.remote.model.UnavailableRetailerDto
import com.pricelens.domain.model.PriceLensException
import com.pricelens.domain.model.ProductComparisonDetails
import com.pricelens.domain.model.ProductSummary
import com.pricelens.domain.model.RetailerOffer
import com.pricelens.domain.model.UnavailableRetailer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductCatalogRemoteDataSourceImpl @Inject constructor(
    private val pricingAdapter: RetailerPricingAdapter
) : ProductCatalogRemoteDataSource {

    override suspend fun searchProducts(query: String): Result<List<ProductSummary>> {
        return when (val result = pricingAdapter.searchProducts(query)) {
            is AdapterResult.Success -> {
                Result.success(result.data.map { it.toDomain() })
            }
            is AdapterResult.ProviderError -> {
                val exception = when (result.httpCode) {
                    429 -> PriceLensException.RateLimitException(
                        message = result.message,
                        safeUserMessage = result.safeUserMessage
                    )
                    else -> PriceLensException.ProviderUnavailableException(
                        message = result.message,
                        safeUserMessage = result.safeUserMessage
                    )
                }
                Result.failure(exception)
            }
            is AdapterResult.MalformedResponse -> {
                Result.failure(
                    PriceLensException.MalformedDataException(
                        message = result.message,
                        safeUserMessage = result.safeUserMessage
                    )
                )
            }
            is AdapterResult.NetworkError -> {
                Result.failure(
                    PriceLensException.NetworkConnectionException(
                        message = result.message,
                        safeUserMessage = result.safeUserMessage
                    )
                )
            }
        }
    }

    override suspend fun getProductOffers(productId: String): Result<List<RetailerOffer>> {
        return when (val result = pricingAdapter.getProductOffers(productId)) {
            is AdapterResult.Success -> {
                val offers = result.data.offers
                    .filter { it.isAvailable }
                    .map { it.toDomain() }
                    .sortedBy { it.price ?: Double.MAX_VALUE }
                Result.success(offers)
            }
            is AdapterResult.ProviderError -> {
                val exception = when (result.httpCode) {
                    404 -> PriceLensException.ProductNotFoundException(result.message, result.safeUserMessage)
                    422 -> PriceLensException.UncertainMatchException(result.message, result.safeUserMessage)
                    429 -> PriceLensException.RateLimitException(result.message, result.safeUserMessage)
                    else -> PriceLensException.ProviderUnavailableException(result.message, result.safeUserMessage)
                }
                Result.failure(exception)
            }
            is AdapterResult.MalformedResponse -> {
                Result.failure(PriceLensException.MalformedDataException(result.message, result.safeUserMessage))
            }
            is AdapterResult.NetworkError -> {
                Result.failure(PriceLensException.NetworkConnectionException(result.message, result.safeUserMessage))
            }
        }
    }

    override suspend fun getProductComparison(productId: String): Result<ProductComparisonDetails> {
        return when (val result = pricingAdapter.getProductOffers(productId)) {
            is AdapterResult.Success -> {
                val dto = result.data
                if (dto.confidenceScore < 0.85) {
                    Result.failure(
                        PriceLensException.UncertainMatchException(
                            message = "Matching confidence score ${dto.confidenceScore} is below required threshold 0.85"
                        )
                    )
                } else {
                    Result.success(dto.toDomain())
                }
            }
            is AdapterResult.ProviderError -> {
                val exception = when (result.httpCode) {
                    404 -> PriceLensException.ProductNotFoundException(result.message, result.safeUserMessage)
                    422 -> PriceLensException.UncertainMatchException(result.message, result.safeUserMessage)
                    429 -> PriceLensException.RateLimitException(result.message, result.safeUserMessage)
                    else -> PriceLensException.ProviderUnavailableException(result.message, result.safeUserMessage)
                }
                Result.failure(exception)
            }
            is AdapterResult.MalformedResponse -> {
                Result.failure(PriceLensException.MalformedDataException(result.message, result.safeUserMessage))
            }
            is AdapterResult.NetworkError -> {
                Result.failure(PriceLensException.NetworkConnectionException(result.message, result.safeUserMessage))
            }
        }
    }

    override suspend fun getTrendingProducts(): Result<List<ProductSummary>> {
        return when (val result = pricingAdapter.getTrendingProducts()) {
            is AdapterResult.Success -> {
                Result.success(result.data.map { it.toDomain() })
            }
            is AdapterResult.ProviderError -> {
                Result.failure(PriceLensException.ProviderUnavailableException(result.message, result.safeUserMessage))
            }
            is AdapterResult.MalformedResponse -> {
                Result.failure(PriceLensException.MalformedDataException(result.message, result.safeUserMessage))
            }
            is AdapterResult.NetworkError -> {
                Result.failure(PriceLensException.NetworkConnectionException(result.message, result.safeUserMessage))
            }
        }
    }

    private fun ProductSummaryDto.toDomain(): ProductSummary =
        ProductSummary(
            id = id,
            name = name,
            brand = brand,
            modelNumber = modelNumber,
            imageUrl = imageUrl,
            lowestPrice = lowestPrice,
            currency = currency,
            retailerCount = retailerCount
        )

    private fun RetailerOfferDto.toDomain(): RetailerOffer =
        RetailerOffer(
            retailerId = retailerId,
            retailerName = retailerName,
            price = price,
            currency = currency,
            isAvailable = isAvailable,
            productUrl = productUrl,
            observedAtTimestamp = observedAtTimestamp
        )

    private fun UnavailableRetailerDto.toDomain(): UnavailableRetailer =
        UnavailableRetailer(
            retailerId = retailerId,
            retailerName = retailerName,
            reason = reason
        )

    private fun ProductOffersDto.toDomain(): ProductComparisonDetails {
        val domainOffers = offers
            .map { it.toDomain() }
            .sortedBy { it.price ?: Double.MAX_VALUE }
        val domainUnavailable = unavailableRetailers.map { it.toDomain() }
        val lowestOffer = domainOffers.firstOrNull { it.isAvailable && it.price != null }

        return ProductComparisonDetails(
            id = productId,
            name = productTitle,
            brand = brand,
            modelNumber = modelNumber,
            imageUrl = imageUrl,
            description = description,
            specifications = specifications,
            offers = domainOffers,
            unavailableRetailers = domainUnavailable,
            lowestPriceOffer = lowestOffer,
            confidenceScore = confidenceScore,
            lastRefreshedTimestamp = lastRefreshedTimestamp
        )
    }
}
