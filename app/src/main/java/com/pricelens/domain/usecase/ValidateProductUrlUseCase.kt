package com.pricelens.domain.usecase

import com.pricelens.domain.model.ConfirmedProductIdentity
import com.pricelens.domain.model.DiscoverySource
import java.net.URI
import javax.inject.Inject

sealed class UrlValidationResult {
    data class Supported(
        val retailerName: String,
        val normalizedUrl: String,
        val suggestedIdentity: ConfirmedProductIdentity? = null
    ) : UrlValidationResult()

    data class UnsupportedRetailer(
        val domain: String,
        val message: String = "PriceLens only compares prices via authorized retailer partner feeds. Web scraping is disabled."
    ) : UrlValidationResult()

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

    // Provider-approved mappings for catalog product resolution from supported URL identifiers
    private val urlToProductMappings = mapOf(
        "B09XS7JWHH" to CatalogMapping("prod_sony_wh1000xm5", "Sony WH-1000XM5 Wireless Noise Cancelling Headphones", "Sony", "WH-1000XM5/B"),
        "B0CHX1W1XY" to CatalogMapping("prod_iphone_15_128", "Apple iPhone 15 (128 GB) - Black", "Apple", "A3090 / MTP03HN/A"),
        "B0CS9Y34L1" to CatalogMapping("prod_samsung_s24", "Samsung Galaxy S24 5G (Onyx Black, 128 GB)", "Samsung", "SM-S921BZKDINS"),
        "B0B3CGS25C" to CatalogMapping("prod_macbook_air_m2", "Apple MacBook Air M2 (13.6-inch, 8GB, 256GB SSD) - Midnight", "Apple", "MLY33HN/A"),
        "B0B171Q435" to CatalogMapping("prod_bose_qc45", "Bose QuietComfort 45 Wireless Noise Cancelling Headphones", "Bose", "866724-0100"),
        "B0CSKPT55P" to CatalogMapping("prod_oneplus_12", "OnePlus 12 5G (Silky Black, 256 GB)", "OnePlus", "CPH2581"),

        "itm6ac6485515ae4" to CatalogMapping("prod_iphone_15_128", "Apple iPhone 15 (128 GB) - Black", "Apple", "A3090 / MTP03HN/A"),
        "itm543210" to CatalogMapping("prod_sony_wh1000xm5", "Sony WH-1000XM5 Wireless Noise Cancelling Headphones", "Sony", "WH-1000XM5/B"),
        "itmd5c9945037ef4" to CatalogMapping("prod_samsung_s24", "Samsung Galaxy S24 5G (Onyx Black, 128 GB)", "Samsung", "SM-S921BZKDINS"),

        "251390" to CatalogMapping("prod_sony_wh1000xm5", "Sony WH-1000XM5 Wireless Noise Cancelling Headphones", "Sony", "WH-1000XM5/B"),
        "300652" to CatalogMapping("prod_iphone_15_128", "Apple iPhone 15 (128 GB) - Black", "Apple", "A3090 / MTP03HN/A"),
        "300780" to CatalogMapping("prod_iphone_15_128", "Apple iPhone 15 (128 GB) - Black", "Apple", "A3090 / MTP03HN/A"),
        "256608" to CatalogMapping("prod_macbook_air_m2", "Apple MacBook Air M2 (13.6-inch, 8GB, 256GB SSD) - Midnight", "Apple", "MLY33HN/A"),

        "493839294" to CatalogMapping("prod_iphone_15_128", "Apple iPhone 15 (128 GB) - Black", "Apple", "A3090 / MTP03HN/A"),
        "493839343" to CatalogMapping("prod_iphone_15_128", "Apple iPhone 15 (128 GB) - Black", "Apple", "A3090 / MTP03HN/A"),
        "492849502" to CatalogMapping("prod_macbook_air_m2", "Apple MacBook Air M2 (13.6-inch, 8GB, 256GB SSD) - Midnight", "Apple", "MLY33HN/A"),

        "23491" to CatalogMapping("prod_samsung_s24", "Samsung Galaxy S24 5G (Onyx Black, 128 GB)", "Samsung", "SM-S921BZKDINS")
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
            val suggestedIdentity = extractProductIdentity(uri, matchingRetailer.second, urlWithScheme)
            UrlValidationResult.Supported(
                retailerName = matchingRetailer.second,
                normalizedUrl = urlWithScheme,
                suggestedIdentity = suggestedIdentity
            )
        } else {
            UrlValidationResult.UnsupportedRetailer(domain = host)
        }
    }

    private fun extractProductIdentity(
        uri: URI,
        retailerName: String,
        fullUrl: String
    ): ConfirmedProductIdentity? {
        val path = uri.path ?: return null

        // 1. Amazon ASIN extraction
        val amazonAsinMatch = "/dp/([A-Z0-9]{10})".toRegex().find(path)
            ?: "/gp/product/([A-Z0-9]{10})".toRegex().find(path)
        if (amazonAsinMatch != null) {
            val asin = amazonAsinMatch.groupValues[1]
            val mapping = urlToProductMappings[asin]
            return if (mapping != null) {
                ConfirmedProductIdentity(
                    source = DiscoverySource.PASTED_URL,
                    rawInput = fullUrl,
                    suggestedName = mapping.title,
                    suggestedBrand = mapping.brand,
                    suggestedModel = mapping.model,
                    resolvedProductId = mapping.productId,
                    confidenceScore = 0.98,
                    isUserConfirmed = false,
                    editedName = mapping.title,
                    editedBrand = mapping.brand,
                    editedModel = mapping.model
                )
            } else {
                val extractedTitle = extractSlugTitle(path) ?: "Amazon Product (ASIN: $asin)"
                ConfirmedProductIdentity(
                    source = DiscoverySource.PASTED_URL,
                    rawInput = fullUrl,
                    suggestedName = extractedTitle,
                    suggestedBrand = "Amazon Listing",
                    suggestedModel = asin,
                    resolvedProductId = null,
                    confidenceScore = 0.85,
                    isUserConfirmed = false,
                    editedName = extractedTitle,
                    editedBrand = "Amazon Listing",
                    editedModel = asin
                )
            }
        }

        // 2. Flipkart item ID extraction
        val flipkartMatch = "/p/([a-zA-Z0-9]+)".toRegex().find(path)
        if (flipkartMatch != null) {
            val itemId = flipkartMatch.groupValues[1]
            val mapping = urlToProductMappings[itemId]
            return if (mapping != null) {
                ConfirmedProductIdentity(
                    source = DiscoverySource.PASTED_URL,
                    rawInput = fullUrl,
                    suggestedName = mapping.title,
                    suggestedBrand = mapping.brand,
                    suggestedModel = mapping.model,
                    resolvedProductId = mapping.productId,
                    confidenceScore = 0.98,
                    isUserConfirmed = false,
                    editedName = mapping.title,
                    editedBrand = mapping.brand,
                    editedModel = mapping.model
                )
            } else {
                val extractedTitle = extractSlugTitle(path) ?: "Flipkart Item $itemId"
                ConfirmedProductIdentity(
                    source = DiscoverySource.PASTED_URL,
                    rawInput = fullUrl,
                    suggestedName = extractedTitle,
                    suggestedBrand = "Flipkart Listing",
                    suggestedModel = itemId,
                    resolvedProductId = null,
                    confidenceScore = 0.85,
                    isUserConfirmed = false,
                    editedName = extractedTitle,
                    editedBrand = "Flipkart Listing",
                    editedModel = itemId
                )
            }
        }

        // 3. Generic numeric ID extraction (Croma, Reliance Digital, Vijay Sales)
        val numericIdMatch = "([0-9]{4,})".toRegex().find(path)
        if (numericIdMatch != null) {
            val numId = numericIdMatch.groupValues[1]
            val mapping = urlToProductMappings[numId]
            if (mapping != null) {
                return ConfirmedProductIdentity(
                    source = DiscoverySource.PASTED_URL,
                    rawInput = fullUrl,
                    suggestedName = mapping.title,
                    suggestedBrand = mapping.brand,
                    suggestedModel = mapping.model,
                    resolvedProductId = mapping.productId,
                    confidenceScore = 0.98,
                    isUserConfirmed = false,
                    editedName = mapping.title,
                    editedBrand = mapping.brand,
                    editedModel = mapping.model
                )
            }
        }

        // Fallback: extract slug if available
        val fallbackTitle = extractSlugTitle(path)
        return if (!fallbackTitle.isNullOrBlank()) {
            ConfirmedProductIdentity(
                source = DiscoverySource.PASTED_URL,
                rawInput = fullUrl,
                suggestedName = fallbackTitle,
                suggestedBrand = retailerName,
                suggestedModel = null,
                resolvedProductId = null,
                confidenceScore = 0.75,
                isUserConfirmed = false,
                editedName = fallbackTitle,
                editedBrand = retailerName,
                editedModel = null
            )
        } else {
            null
        }
    }

    private fun extractSlugTitle(path: String): String? {
        val segments = path.split("/").filter { it.isNotBlank() }
        val slug = segments.firstOrNull { it != "p" && it != "dp" && it != "gp" && it.length > 3 } ?: return null
        val cleaned = slug.replace("-", " ").replace("_", " ").trim()
        return cleaned.split(" ")
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }

    private data class CatalogMapping(
        val productId: String,
        val title: String,
        val brand: String,
        val model: String
    )
}
