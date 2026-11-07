package com.pricelens.domain.service

import com.pricelens.domain.model.ConfirmedProductIdentity
import com.pricelens.domain.model.DiscoverySource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for analyzing OCR text / camera frames / barcode strings to identify
 * product candidates and produce editable identification suggestions.
 */
@Singleton
class ProductOcrScanner @Inject constructor() {

    // Known catalog identifiers for local high-confidence recognition
    private val knownSignatures = listOf(
        CatalogSignature(
            productId = "prod_sony_wh1000xm5",
            title = "Sony WH-1000XM5 Wireless Noise Cancelling Headphones",
            brand = "Sony",
            model = "WH-1000XM5/B",
            keywords = listOf("wh-1000xm5", "wh1000xm5", "sony xm5", "1000xm5")
        ),
        CatalogSignature(
            productId = "prod_bose_qc45",
            title = "Bose QuietComfort 45 Wireless Noise Cancelling Headphones",
            brand = "Bose",
            model = "866724-0100",
            keywords = listOf("quietcomfort 45", "qc45", "bose qc", "866724-0100")
        ),
        CatalogSignature(
            productId = "prod_sennheiser_m4",
            title = "Sennheiser Momentum 4 Wireless ANC Headphones",
            brand = "Sennheiser",
            model = "509180",
            keywords = listOf("momentum 4", "sennheiser m4", "509180")
        ),
        CatalogSignature(
            productId = "prod_iphone_15_128",
            title = "Apple iPhone 15 (128 GB) - Black",
            brand = "Apple",
            model = "A3090 / MTP03HN/A",
            keywords = listOf("iphone 15", "a3090", "mtp03hn/a", "mtp03hn")
        ),
        CatalogSignature(
            productId = "prod_samsung_s24",
            title = "Samsung Galaxy S24 5G (Onyx Black, 128 GB)",
            brand = "Samsung",
            model = "SM-S921BZKDINS",
            keywords = listOf("galaxy s24", "s24 5g", "sm-s921", "sm-s921bzkdins", "s921b")
        ),
        CatalogSignature(
            productId = "prod_oneplus_12",
            title = "OnePlus 12 5G (Silky Black, 256 GB)",
            brand = "OnePlus",
            model = "CPH2581",
            keywords = listOf("oneplus 12", "cph2581", "op12")
        ),
        CatalogSignature(
            productId = "prod_macbook_air_m2",
            title = "Apple MacBook Air M2 (13.6-inch, 8GB Unified Memory, 256GB SSD) - Midnight",
            brand = "Apple",
            model = "MLY33HN/A",
            keywords = listOf("macbook air m2", "mly33hn/a", "mly33", "macbook m2")
        ),
        CatalogSignature(
            productId = "prod_dell_xps13",
            title = "Dell XPS 13 9315 Laptop (Intel Core i7 12th Gen, 16GB, 512GB SSD)",
            brand = "Dell",
            model = "XPS9315-7001SLV",
            keywords = listOf("xps 13", "xps 9315", "xps9315", "dell 9315")
        )
    )

    private val knownBrands = listOf(
        "Apple", "Sony", "Samsung", "OnePlus", "Bose", "Sennheiser", "Dell", "HP", "Lenovo", "Asus"
    )

    /**
     * Scans raw OCR or label text and produces an editable [ConfirmedProductIdentity].
     */
    fun scanText(rawText: String, source: DiscoverySource = DiscoverySource.CAMERA_OCR): ConfirmedProductIdentity? {
        val trimmed = rawText.trim()
        if (trimmed.isBlank()) {
            return null
        }

        val lower = trimmed.lowercase()

        // 1. Check known signatures for high confidence match
        for (sig in knownSignatures) {
            val matchedKeyword = sig.keywords.any { lower.contains(it) }
            if (matchedKeyword) {
                return ConfirmedProductIdentity(
                    source = source,
                    rawInput = trimmed,
                    suggestedName = sig.title,
                    suggestedBrand = sig.brand,
                    suggestedModel = sig.model,
                    resolvedProductId = sig.productId,
                    confidenceScore = 0.96,
                    isUserConfirmed = false,
                    editedName = sig.title,
                    editedBrand = sig.brand,
                    editedModel = sig.model
                )
            }
        }

        // 2. Generic heuristic extraction
        val detectedBrand = knownBrands.firstOrNull { lower.contains(it.lowercase()) }

        // Look for model number patterns (e.g., uppercase alphanumerics with dash/slash)
        val modelMatch = "[A-Z0-9]{3,}-[A-Z0-9/]{2,}".toRegex().find(trimmed)
            ?: "[A-Z]{1,2}[0-9]{4,}".toRegex().find(trimmed)
        val detectedModel = modelMatch?.value

        // Construct candidate title
        val candidateName = when {
            detectedBrand != null && detectedModel != null -> "$detectedBrand Model $detectedModel"
            detectedBrand != null -> "$detectedBrand Product"
            detectedModel != null -> "Product Model $detectedModel"
            else -> trimmed.lines().firstOrNull()?.take(40) ?: trimmed.take(40)
        }

        return ConfirmedProductIdentity(
            source = source,
            rawInput = trimmed,
            suggestedName = candidateName,
            suggestedBrand = detectedBrand,
            suggestedModel = detectedModel,
            resolvedProductId = null,
            confidenceScore = if (detectedBrand != null && detectedModel != null) 0.88 else 0.70,
            isUserConfirmed = false,
            editedName = candidateName,
            editedBrand = detectedBrand,
            editedModel = detectedModel
        )
    }

    private data class CatalogSignature(
        val productId: String,
        val title: String,
        val brand: String,
        val model: String,
        val keywords: List<String>
    )
}
