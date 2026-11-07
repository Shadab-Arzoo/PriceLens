package com.pricelens.domain.service

import com.pricelens.domain.model.MeaningfulDifference
import com.pricelens.domain.model.ProductAlternative
import com.pricelens.domain.model.ProductSummary
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for computing and ranking product alternatives based on normalized catalog specifications
 * and price differences.
 */
@Singleton
class AlternativesService @Inject constructor() {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    /**
     * Identifies, compares, and ranks alternatives for a target product from a list of candidate products.
     */
    fun findAlternatives(
        targetId: String,
        targetTitle: String,
        targetCategory: String?,
        targetPrice: Double?,
        targetSpecs: Map<String, String>,
        candidates: List<CandidateProduct>
    ): List<ProductAlternative> {
        val detectedCategory = targetCategory ?: inferCategory(targetTitle, targetSpecs)

        val alternatives = candidates
            .filter { it.id != targetId }
            .filter { candidate ->
                val candidateCategory = candidate.category ?: inferCategory(candidate.title, candidate.specifications)
                candidateCategory.equals(detectedCategory, ignoreCase = true)
            }
            .map { candidate ->
                buildAlternative(
                    candidate = candidate,
                    targetTitle = targetTitle,
                    targetPrice = targetPrice,
                    targetSpecs = targetSpecs
                )
            }

        // Rank by ranking score descending (cheaper alternatives with spec advantages first)
        return alternatives.sortedWith(
            compareByDescending<ProductAlternative> { it.isCheaper }
                .thenByDescending { it.rankingScore }
        )
    }

    private fun buildAlternative(
        candidate: CandidateProduct,
        targetTitle: String,
        targetPrice: Double?,
        targetSpecs: Map<String, String>
    ): ProductAlternative {
        val candidatePrice = candidate.lowestPrice
        val priceDiff = if (targetPrice != null && candidatePrice != null) {
            candidatePrice - targetPrice
        } else {
            null
        }

        val isCheaper = priceDiff != null && priceDiff < 0.0
        val percentageDiff = if (targetPrice != null && priceDiff != null && targetPrice > 0) {
            (priceDiff / targetPrice) * 100.0
        } else {
            null
        }

        val differences = mutableListOf<MeaningfulDifference>()

        // 1. Price Difference Explanation
        if (targetPrice != null && candidatePrice != null) {
            if (isCheaper) {
                val savings = kotlin.math.abs(priceDiff!!)
                differences.add(
                    MeaningfulDifference(
                        category = "Price",
                        currentProductValue = currencyFormatter.format(targetPrice),
                        alternativeValue = currencyFormatter.format(candidatePrice),
                        explanation = "Cheaper by ${currencyFormatter.format(savings)} (${String.format(Locale.US, "%.1f", kotlin.math.abs(percentageDiff!!))}% savings)",
                        isAdvantage = true
                    )
                )
            } else if (priceDiff != null && priceDiff > 0.0) {
                val extra = priceDiff
                differences.add(
                    MeaningfulDifference(
                        category = "Price",
                        currentProductValue = currencyFormatter.format(targetPrice),
                        alternativeValue = currencyFormatter.format(candidatePrice),
                        explanation = "${currencyFormatter.format(extra)} higher price",
                        isAdvantage = false
                    )
                )
            }
        }

        // 2. Specification Differences
        val allKeys = (targetSpecs.keys + candidate.specifications.keys).distinct()
        for (key in allKeys) {
            val targetVal = targetSpecs[key]
            val altVal = candidate.specifications[key]

            if (targetVal != null && altVal != null && !targetVal.equals(altVal, ignoreCase = true)) {
                val (isAdvantage, explanation) = analyzeSpecDifference(key, targetVal, altVal)
                differences.add(
                    MeaningfulDifference(
                        category = key,
                        currentProductValue = targetVal,
                        alternativeValue = altVal,
                        explanation = explanation,
                        isAdvantage = isAdvantage
                    )
                )
            } else if (targetVal == null && altVal != null) {
                differences.add(
                    MeaningfulDifference(
                        category = key,
                        currentProductValue = "Not specified",
                        alternativeValue = altVal,
                        explanation = "Includes $key: $altVal",
                        isAdvantage = true
                    )
                )
            }
        }

        // Compute Ranking Score
        var score = 50.0
        if (isCheaper && percentageDiff != null) {
            score += kotlin.math.abs(percentageDiff) * 2.0
        }
        score += differences.count { it.isAdvantage } * 10.0
        score -= differences.count { !it.isAdvantage && it.category != "Price" } * 5.0

        return ProductAlternative(
            productId = candidate.id,
            title = candidate.title,
            brand = candidate.brand,
            modelNumber = candidate.modelNumber,
            imageUrl = candidate.imageUrl,
            lowestPrice = candidate.lowestPrice,
            currency = candidate.currency,
            currentProductPrice = targetPrice,
            priceDifference = priceDiff,
            isCheaper = isCheaper,
            percentageDifference = percentageDiff,
            meaningfulDifferences = differences,
            specifications = candidate.specifications,
            rankingScore = score
        )
    }

    private fun analyzeSpecDifference(key: String, targetVal: String, altVal: String): Pair<Boolean, String> {
        val lowerKey = key.lowercase()

        // Battery / Battery Life
        if (lowerKey.contains("battery")) {
            val targetNum = extractNumber(targetVal)
            val altNum = extractNumber(altVal)
            if (targetNum != null && altNum != null) {
                val diff = altNum - targetNum
                return if (diff > 0) {
                    Pair(true, "+$diff capacity/runtime ($altVal vs $targetVal)")
                } else {
                    Pair(false, "$altVal vs $targetVal")
                }
            }
        }

        // Display / Refresh Rate
        if (lowerKey.contains("display") || lowerKey.contains("screen")) {
            val targetHz = extractHz(targetVal)
            val altHz = extractHz(altVal)
            if (altHz != null && targetHz != null && altHz > targetHz) {
                return Pair(true, "Higher refresh rate: ${altHz}Hz vs ${targetHz}Hz")
            }
            if (altVal.contains("120Hz", ignoreCase = true) && !targetVal.contains("120Hz", ignoreCase = true)) {
                return Pair(true, "Smoother 120Hz display vs standard refresh rate")
            }
        }

        // Weight (lower is usually advantageous for portable devices)
        if (lowerKey.contains("weight")) {
            val targetWeight = extractWeightGrams(targetVal)
            val altWeight = extractWeightGrams(altVal)
            if (targetWeight != null && altWeight != null) {
                val diff = targetWeight - altWeight
                return if (diff > 0) {
                    Pair(true, "Lighter build (${altVal} vs ${targetVal})")
                } else {
                    Pair(false, "${altVal} vs ${targetVal}")
                }
            }
        }

        // Default: informative comparison
        return Pair(false, "$altVal (vs $targetVal)")
    }

    private fun extractNumber(str: String): Int? {
        val match = "\\d+".toRegex().find(str)
        return match?.value?.toIntOrNull()
    }

    private fun extractHz(str: String): Int? {
        val match = "(\\d+)\\s*Hz".toRegex(RegexOption.IGNORE_CASE).find(str)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun extractWeightGrams(str: String): Double? {
        if (str.contains("kg", ignoreCase = true)) {
            val num = "([0-9.]+)".toRegex().find(str)?.groupValues?.get(1)?.toDoubleOrNull()
            return num?.let { it * 1000.0 }
        }
        val num = "([0-9.]+)".toRegex().find(str)?.groupValues?.get(1)?.toDoubleOrNull()
        return num
    }

    private fun inferCategory(title: String, specs: Map<String, String>): String {
        val t = title.lowercase()
        return when {
            t.contains("phone") || t.contains("galaxy") || t.contains("iphone") || t.contains("5g") || specs.containsKey("Connector") -> "Smartphone"
            t.contains("headphone") || t.contains("wh-") || t.contains("earphone") || specs.containsKey("Driver Unit") -> "Headphones"
            t.contains("macbook") || t.contains("laptop") || t.contains("xps") || specs.containsKey("Memory") -> "Laptop"
            else -> "Electronics"
        }
    }

    /**
     * Internal candidate projection for alternative evaluation.
     */
    data class CandidateProduct(
        val id: String,
        val title: String,
        val brand: String?,
        val modelNumber: String?,
        val imageUrl: String?,
        val category: String?,
        val lowestPrice: Double?,
        val currency: String = "INR",
        val specifications: Map<String, String>
    )
}
