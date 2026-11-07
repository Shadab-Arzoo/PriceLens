package com.pricelens.data.remote.adapter

import com.pricelens.data.remote.model.AdapterResult
import com.pricelens.data.remote.model.ProductOffersDto
import com.pricelens.data.remote.model.ProductSummaryDto

/**
 * Server-owned adapter contract for communicating with authorized pricing/affiliate sources.
 *
 * All implementations run either on the server or simulate server-side normalized feeds.
 * Sensitive provider credentials must never enter client code or application bundles.
 */
interface RetailerPricingAdapter {

    /**
     * Searches products across authorized retailer partner feeds.
     */
    suspend fun searchProducts(query: String): AdapterResult<List<ProductSummaryDto>>

    /**
     * Fetches current real offers, prices, and unavailable states for a specific product.
     */
    suspend fun getProductOffers(productId: String): AdapterResult<ProductOffersDto>

    /**
     * Retrieves trending products with verified price observations.
     */
    suspend fun getTrendingProducts(): AdapterResult<List<ProductSummaryDto>>

    /**
     * Retrieves verified catalog alternatives for a given product ID.
     */
    suspend fun getProductAlternatives(productId: String): AdapterResult<List<ProductOffersDto>>
}
