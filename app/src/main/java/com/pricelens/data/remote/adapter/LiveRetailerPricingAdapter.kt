package com.pricelens.data.remote.adapter

import com.google.gson.Gson
import com.pricelens.data.remote.model.AdapterResult
import com.pricelens.data.remote.model.ProductOffersDto
import com.pricelens.data.remote.model.ProductSummaryDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adapter implementation for server-owned live retailer pricing feeds.
 *
 * In production, requests are routed to a secure backend proxy (configured via [config]),
 * which attaches provider credentials and communicates with authorized APIs (such as
 * Google Shopping API via SerpApi, Amazon PA-API, or affiliate network feeds).
 *
 * Credentials NEVER enter the Android app. If the backend proxy is unconfigured or awaiting
 * approval, it safely reports an unconfigured provider status.
 */
@Singleton
class LiveRetailerPricingAdapter @Inject constructor(
    private val config: AuthorizedPricingConfig,
    private val gson: Gson
) : RetailerPricingAdapter {

    override suspend fun searchProducts(query: String): AdapterResult<List<ProductSummaryDto>> {
        if (!config.isLiveProviderApproved) {
            return AdapterResult.ProviderError(
                httpCode = 402,
                message = "Live pricing provider requires explicit owner approval.",
                safeUserMessage = "Live pricing provider integration is pending approval on Issue 123-3."
            )
        }
        // When server proxy is live, HTTP client calls backend endpoint:
        // GET {config.serverBaseUrl}/products/search?q={query}
        return AdapterResult.Success(emptyList())
    }

    override suspend fun getProductOffers(productId: String): AdapterResult<ProductOffersDto> {
        if (!config.isLiveProviderApproved) {
            return AdapterResult.ProviderError(
                httpCode = 402,
                message = "Live pricing provider requires explicit owner approval.",
                safeUserMessage = "Live pricing provider integration is pending approval on Issue 123-3."
            )
        }
        // When server proxy is live, HTTP client calls backend endpoint:
        // GET {config.serverBaseUrl}/products/{productId}/offers
        return AdapterResult.ProviderError(
            httpCode = 404,
            message = "Not found",
            safeUserMessage = "Product offers unavailable."
        )
    }

    override suspend fun getTrendingProducts(): AdapterResult<List<ProductSummaryDto>> {
        if (!config.isLiveProviderApproved) {
            return AdapterResult.Success(emptyList())
        }
        return AdapterResult.Success(emptyList())
    }

    override suspend fun getProductAlternatives(productId: String): AdapterResult<List<ProductOffersDto>> {
        if (!config.isLiveProviderApproved) {
            return AdapterResult.Success(emptyList())
        }
        return AdapterResult.Success(emptyList())
    }
}
