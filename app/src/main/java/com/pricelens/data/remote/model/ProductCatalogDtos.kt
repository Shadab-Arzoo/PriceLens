package com.pricelens.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Data transfer object representing a normalized product summary from a pricing source.
 */
data class ProductSummaryDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("brand")
    val brand: String? = null,
    @SerializedName("model_number")
    val modelNumber: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("lowest_price")
    val lowestPrice: Double? = null,
    @SerializedName("currency")
    val currency: String = "INR",
    @SerializedName("retailer_count")
    val retailerCount: Int = 0
)

/**
 * Data transfer object representing a single retailer's offer.
 */
data class RetailerOfferDto(
    @SerializedName("retailer_id")
    val retailerId: String,
    @SerializedName("retailer_name")
    val retailerName: String,
    @SerializedName("price")
    val price: Double?,
    @SerializedName("currency")
    val currency: String = "INR",
    @SerializedName("is_available")
    val isAvailable: Boolean,
    @SerializedName("product_url")
    val productUrl: String,
    @SerializedName("observed_at_timestamp")
    val observedAtTimestamp: Long,
    @SerializedName("condition")
    val condition: String = "NEW",
    @SerializedName("seller_rating")
    val sellerRating: Double? = null,
    @SerializedName("delivery_info")
    val deliveryInfo: String? = null
)

/**
 * Data transfer object representing an explicitly unavailable retailer for a product.
 */
data class UnavailableRetailerDto(
    @SerializedName("retailer_id")
    val retailerId: String,
    @SerializedName("retailer_name")
    val retailerName: String,
    @SerializedName("reason")
    val reason: String
)

/**
 * Data transfer object representing complete product comparison details returned by a server adapter.
 */
data class ProductOffersDto(
    @SerializedName("product_id")
    val productId: String,
    @SerializedName("product_title")
    val productTitle: String,
    @SerializedName("brand")
    val brand: String? = null,
    @SerializedName("model_number")
    val modelNumber: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("specifications")
    val specifications: Map<String, String> = emptyMap(),
    @SerializedName("offers")
    val offers: List<RetailerOfferDto> = emptyList(),
    @SerializedName("unavailable_retailers")
    val unavailableRetailers: List<UnavailableRetailerDto> = emptyList(),
    @SerializedName("confidence_score")
    val confidenceScore: Double = 1.0,
    @SerializedName("last_refreshed_timestamp")
    val lastRefreshedTimestamp: Long = 0L
)

/**
 * Discriminated result type for retailer adapter operations.
 */
sealed class AdapterResult<out T> {
    data class Success<T>(val data: T) : AdapterResult<T>()
    data class ProviderError(
        val httpCode: Int,
        val message: String,
        val safeUserMessage: String
    ) : AdapterResult<Nothing>()
    data class MalformedResponse(
        val message: String,
        val safeUserMessage: String = "Received invalid or unparseable data from pricing provider."
    ) : AdapterResult<Nothing>()
    data class NetworkError(
        val message: String,
        val safeUserMessage: String = "Unable to connect to pricing service. Please check your network connection."
    ) : AdapterResult<Nothing>()
}
