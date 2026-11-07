package com.pricelens

import com.pricelens.domain.service.AlternativesService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetProductAlternativesUseCaseTest {

    private lateinit var service: AlternativesService

    @Before
    fun setUp() {
        service = AlternativesService()
    }

    @Test
    fun `alternatives find cheaper smartphone and explain meaningful spec differences`() {
        // iPhone 15 specs & price (₹69,900)
        val targetId = "prod_iphone_15_128"
        val targetTitle = "Apple iPhone 15 (128 GB) - Black"
        val targetCategory = "Smartphone"
        val targetPrice = 69900.0
        val targetSpecs = mapOf(
            "Display" to "6.1-inch Super Retina XDR 60Hz",
            "Processor" to "A16 Bionic chip",
            "Battery" to "3349 mAh",
            "Storage" to "128 GB"
        )

        // Candidates: Samsung Galaxy S24 (₹62,999) & OnePlus 12 (₹59,999) & Sony Headphones (diff category)
        val candidates = listOf(
            AlternativesService.CandidateProduct(
                id = "prod_samsung_s24",
                title = "Samsung Galaxy S24 5G (128 GB)",
                brand = "Samsung",
                modelNumber = "SM-S921BZKDINS",
                imageUrl = null,
                category = "Smartphone",
                lowestPrice = 62999.0,
                specifications = mapOf(
                    "Display" to "6.2-inch Dynamic AMOLED 2X 120Hz",
                    "Processor" to "Snapdragon 8 Gen 3",
                    "Battery" to "4000 mAh",
                    "Storage" to "128 GB"
                )
            ),
            AlternativesService.CandidateProduct(
                id = "prod_oneplus_12",
                title = "OnePlus 12 5G (256 GB)",
                brand = "OnePlus",
                modelNumber = "CPH2581",
                imageUrl = null,
                category = "Smartphone",
                lowestPrice = 59999.0,
                specifications = mapOf(
                    "Display" to "6.82-inch 2K ProXDR 120Hz LTPO",
                    "Processor" to "Snapdragon 8 Gen 3",
                    "Battery" to "5400 mAh with 100W SuperVOOC",
                    "Storage" to "256 GB"
                )
            ),
            AlternativesService.CandidateProduct(
                id = "prod_sony_wh1000xm5",
                title = "Sony WH-1000XM5",
                brand = "Sony",
                modelNumber = "WH-1000XM5/B",
                imageUrl = null,
                category = "Headphones",
                lowestPrice = 26990.0,
                specifications = mapOf("Battery Life" to "Up to 30 hours")
            )
        )

        val alternatives = service.findAlternatives(
            targetId = targetId,
            targetTitle = targetTitle,
            targetCategory = targetCategory,
            targetPrice = targetPrice,
            targetSpecs = targetSpecs,
            candidates = candidates
        )

        // 1. Category filtering: Headphones must not appear as alternative to smartphone
        assertEquals(2, alternatives.size)
        assertTrue(alternatives.all { it.productId != "prod_sony_wh1000xm5" })

        // 2. Both candidates are cheaper than iPhone 15
        assertTrue(alternatives.all { it.isCheaper })

        // 3. Cheaper alternatives ranking: OnePlus 12 (₹59,999 -> ₹9,901 savings) ranked #1
        val topAlternative = alternatives.first()
        assertEquals("prod_oneplus_12", topAlternative.productId)
        assertEquals(-9901.0, topAlternative.priceDifference!!, 0.01)

        // 4. Meaningful Differences explanation
        val batteryDiff = topAlternative.meaningfulDifferences.firstOrNull { it.category.contains("Battery") }
        assertNotNull(batteryDiff)
        assertTrue("Battery difference must be flagged as advantage", batteryDiff!!.isAdvantage)
        assertTrue("Explanation must mention battery advantage", batteryDiff.explanation.contains("+2051") || batteryDiff.explanation.contains("5400"))

        val displayDiff = topAlternative.meaningfulDifferences.firstOrNull { it.category.contains("Display") }
        assertNotNull(displayDiff)
        assertTrue("120Hz display must be marked as advantage", displayDiff!!.isAdvantage)

        val priceDiff = topAlternative.meaningfulDifferences.firstOrNull { it.category == "Price" }
        assertNotNull(priceDiff)
        assertTrue("Lower price must be marked as advantage", priceDiff!!.isAdvantage)
        assertTrue(priceDiff.explanation.contains("Cheaper by"))
    }

    @Test
    fun `alternatives find lighter headphone alternative and lower price`() {
        val targetSpecs = mapOf(
            "Battery Life" to "Up to 30 hours",
            "Weight" to "250g",
            "Driver Unit" to "30 mm"
        )
        val candidates = listOf(
            AlternativesService.CandidateProduct(
                id = "prod_bose_qc45",
                title = "Bose QuietComfort 45",
                brand = "Bose",
                modelNumber = "866724-0100",
                imageUrl = null,
                category = "Headphones",
                lowestPrice = 24990.0,
                specifications = mapOf(
                    "Battery Life" to "Up to 24 hours",
                    "Weight" to "240g",
                    "Driver Unit" to "35 mm TriPort"
                )
            )
        )

        val alternatives = service.findAlternatives(
            targetId = "prod_sony_wh1000xm5",
            targetTitle = "Sony WH-1000XM5",
            targetCategory = "Headphones",
            targetPrice = 26990.0,
            targetSpecs = targetSpecs,
            candidates = candidates
        )

        assertEquals(1, alternatives.size)
        val bose = alternatives.first()
        assertTrue(bose.isCheaper)
        assertEquals(-2000.0, bose.priceDifference!!, 0.01)

        val weightDiff = bose.meaningfulDifferences.firstOrNull { it.category.contains("Weight") }
        assertNotNull(weightDiff)
        assertTrue("Lighter weight must be marked as advantage", weightDiff!!.isAdvantage)
    }

    @Test
    fun `same product is excluded from alternatives list`() {
        val candidates = listOf(
            AlternativesService.CandidateProduct(
                id = "prod_iphone_15_128",
                title = "Apple iPhone 15",
                brand = "Apple",
                modelNumber = "A3090",
                imageUrl = null,
                category = "Smartphone",
                lowestPrice = 69900.0,
                specifications = emptyMap()
            )
        )

        val alternatives = service.findAlternatives(
            targetId = "prod_iphone_15_128",
            targetTitle = "Apple iPhone 15",
            targetCategory = "Smartphone",
            targetPrice = 69900.0,
            targetSpecs = emptyMap(),
            candidates = candidates
        )

        assertTrue(alternatives.isEmpty())
    }
}
