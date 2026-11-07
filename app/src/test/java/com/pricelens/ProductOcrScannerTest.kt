package com.pricelens

import com.pricelens.domain.model.DiscoverySource
import com.pricelens.domain.service.ProductOcrScanner
import com.pricelens.domain.usecase.ProcessImageOcrUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProductOcrScannerTest {

    private lateinit var scanner: ProductOcrScanner
    private lateinit var useCase: ProcessImageOcrUseCase

    @Before
    fun setUp() {
        scanner = ProductOcrScanner()
        useCase = ProcessImageOcrUseCase(scanner)
    }

    @Test
    fun `ocr scans sony xm5 text and yields editable suggestion`() {
        val rawText = "SONY Wireless Noise Canceling Stereo Headset WH-1000XM5 Made in Malaysia"
        val result = useCase(rawText, DiscoverySource.CAMERA_OCR)

        assertTrue(result.isSuccess)
        val identity = result.getOrNull()
        assertNotNull(identity)
        assertEquals("prod_sony_wh1000xm5", identity!!.resolvedProductId)
        assertEquals("Sony", identity.suggestedBrand)
        assertEquals("WH-1000XM5/B", identity.suggestedModel)
        assertEquals(DiscoverySource.CAMERA_OCR, identity.source)
        assertFalse("Requires explicit user confirmation before comparison", identity.isUserConfirmed)
    }

    @Test
    fun `ocr scans iphone text and yields editable suggestion`() {
        val rawText = "Apple iPhone 15 128GB Black Model A3090 MTP03HN/A"
        val result = useCase(rawText, DiscoverySource.IMAGE_GALLERY)

        assertTrue(result.isSuccess)
        val identity = result.getOrNull()
        assertNotNull(identity)
        assertEquals("prod_iphone_15_128", identity!!.resolvedProductId)
        assertEquals("Apple", identity.suggestedBrand)
        assertEquals(DiscoverySource.IMAGE_GALLERY, identity.source)
        assertFalse(identity.isUserConfirmed)
    }

    @Test
    fun `user can correct ocr misread suggestion before confirmation`() {
        // Simulating OCR error: "WH-1000XM5" detected, but user edits name to correct color/spec
        val rawText = "Sony WH-1000XM5"
        val identity = useCase(rawText).getOrThrow()

        assertEquals("Sony WH-1000XM5 Wireless Noise Cancelling Headphones", identity.suggestedName)
        assertFalse(identity.isUserConfirmed)

        // User edits the name to "Sony WH-1000XM5 Silver Edition"
        val corrected = identity.withCorrections(
            name = "Sony WH-1000XM5 Silver Edition",
            brand = "Sony",
            model = "WH-1000XM5/S"
        )
        assertEquals("Sony WH-1000XM5 Silver Edition", corrected.effectiveTitle)
        assertEquals("WH-1000XM5/S", corrected.editedModel)
        assertFalse("Editing alone does not confirm", corrected.isUserConfirmed)

        // User explicitly confirms
        val confirmed = corrected.confirm()
        assertTrue("Confirmation flag must be set", confirmed.isUserConfirmed)
        assertEquals("Sony WH-1000XM5 Silver Edition", confirmed.effectiveTitle)
    }

    @Test
    fun `generic brand and model label produces editable suggestion with moderate confidence`() {
        val rawText = "Asus Vivobook Model X515-BQ010T"
        val identity = useCase(rawText).getOrThrow()

        assertEquals("Asus", identity.suggestedBrand)
        assertNotNull(identity.suggestedModel)
        assertFalse(identity.isUserConfirmed)
        assertTrue(identity.confidenceScore > 0.8)
    }

    @Test
    fun `empty or blank ocr input returns failure`() {
        val resultEmpty = useCase("")
        assertTrue(resultEmpty.isFailure)

        val resultBlank = useCase("   ")
        assertTrue(resultBlank.isFailure)
    }
}
