package com.pricelens.domain.usecase

import java.net.URI
import javax.inject.Inject

sealed class UrlValidationResult {
    data class Supported(val retailerName: String, val normalizedUrl: String) : UrlValidationResult()
    data class UnsupportedRetailer(val domain: String) : UrlValidationResult()
    data class InvalidUrl(val reason: String) : UrlValidationResult()
}

class ValidateProductUrlUseCase @Inject constructor() {

    private val supportedRetailers = listOf(
        "amazon.in" to "Amazon India",
        "flipkart.com" to "Flipkart",
        "croma.com" to "Croma",
        "reliancedigital.in" to "Reliance Digital",
        "vijaysales.com" to "Vijay Sales"
    )

    operator fun invoke(rawUrl: String): UrlValidationResult {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) {
            return UrlValidationResult.InvalidUrl("URL cannot be empty.")
        }

        val urlWithScheme = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "https://$trimmed"
        } else {
            trimmed
        }

        val uri = try {
            URI(urlWithScheme)
        } catch (e: Exception) {
            return UrlValidationResult.InvalidUrl("Malformed URL format.")
        }

        val host = uri.host?.lowercase() ?: return UrlValidationResult.InvalidUrl("Invalid domain name.")

        val matchingRetailer = supportedRetailers.firstOrNull { (domain, _) ->
            host == domain || host.endsWith(".$domain")
        }

        return if (matchingRetailer != null) {
            UrlValidationResult.Supported(
                retailerName = matchingRetailer.second,
                normalizedUrl = urlWithScheme
            )
        } else {
            UrlValidationResult.UnsupportedRetailer(domain = host)
        }
    }
}
