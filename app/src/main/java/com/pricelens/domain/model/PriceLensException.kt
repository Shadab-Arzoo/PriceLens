package com.pricelens.domain.model

/**
 * Domain exceptions providing safe, user-friendly error messages for UI presentation.
 *
 * Ensures technical or provider failure details (such as HTTP codes, raw JSON syntax errors,
 * or stack traces) never display directly to end-users as cryptic messages.
 */
sealed class PriceLensException(
    message: String,
    val safeUserMessage: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class ProviderUnavailableException(
        message: String,
        safeUserMessage: String = "Pricing service is temporarily unavailable. Please try again."
    ) : PriceLensException(message, safeUserMessage)

    class RateLimitException(
        message: String,
        safeUserMessage: String = "Search limit reached. Please wait a moment before trying again."
    ) : PriceLensException(message, safeUserMessage)

    class MalformedDataException(
        message: String,
        safeUserMessage: String = "Pricing data could not be parsed safely. We are looking into this."
    ) : PriceLensException(message, safeUserMessage)

    class UncertainMatchException(
        message: String,
        safeUserMessage: String = "Product offers could not be matched with high confidence. Prices will not be merged silently."
    ) : PriceLensException(message, safeUserMessage)

    class NetworkConnectionException(
        message: String,
        safeUserMessage: String = "Unable to connect to pricing network. Please check your internet connection."
    ) : PriceLensException(message, safeUserMessage)

    class ProductNotFoundException(
        message: String,
        safeUserMessage: String = "Product offers could not be found for the selected item."
    ) : PriceLensException(message, safeUserMessage)
}
