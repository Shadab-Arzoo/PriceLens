package com.pricelens.domain.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UniversalPriceFetcher @Inject constructor(
    private val extractor: UniversalProductExtractor
) {

    suspend fun fetchPrice(websiteUrl: String, priceSelector: String?): Double? = withContext(Dispatchers.IO) {
        try {
            val metadata = extractor.extractMetadata(websiteUrl)
            // If we got a valid price from the page
            if (metadata.price != null && metadata.price > 0) {
                return@withContext metadata.price
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
