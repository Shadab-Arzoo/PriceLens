package com.pricelens

import com.pricelens.domain.model.DiscoverySource
import com.pricelens.domain.usecase.UrlValidationResult
import com.pricelens.domain.usecase.ValidateProductUrlUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateProductUrlUseCaseTest {

    private lateinit var useCase: ValidateProductUrlUseCase

    @Before
    fun setUp() {
        useCase = ValidateProductUrlUseCase()
    }

    @Test
    fun `valid amazon india url returns supported result with extracted product identity`() {
        val result = useCase("https://www.amazon.in/Apple-iPhone-15-128-GB/dp/B0CHX1W1XY")
        assertTrue(result is UrlValidationResult.Supported)
        val supported = result as UrlValidationResult.Supported
        assertEquals("Amazon India", supported.retailerName)

        val identity = supported.suggestedIdentity
        assertNotNull(identity)
        assertEquals("prod_iphone_15_128", identity!!.resolvedProductId)
        assertEquals("Apple", identity.suggestedBrand)
        assertEquals("Apple iPhone 15 (128 GB) - Black", identity.suggestedName)
        assertEquals(DiscoverySource.PASTED_URL, identity.source)
        assertFalse("Explicit user confirmation must be required before comparison", identity.isUserConfirmed)
    }

    @Test
    fun `valid flipkart url returns supported result with parsed product identity`() {
        val result = useCase("https://www.flipkart.com/apple-iphone-15-black-128-gb/p/itm6ac6485515ae4")
        assertTrue(result is UrlValidationResult.Supported)
        val supported = result as UrlValidationResult.Supported
        assertEquals("Flipkart", supported.retailerName)

        val identity = supported.suggestedIdentity
        assertNotNull(identity)
        assertEquals("prod_iphone_15_128", identity!!.resolvedProductId)
        assertFalse(identity.isUserConfirmed)
    }

    @Test
    fun `valid croma url returns supported result with parsed product identity`() {
        val result = useCase("https://www.croma.com/apple-iphone-15-128gb-black-/p/300780")
        assertTrue(result is UrlValidationResult.Supported)
        val supported = result as UrlValidationResult.Supported
        assertEquals("Croma", supported.retailerName)
        assertNotNull(supported.suggestedIdentity)
        assertEquals("prod_iphone_15_128", supported.suggestedIdentity!!.resolvedProductId)
    }

    @Test
    fun `valid reliance digital url returns supported result with parsed product identity`() {
        val result = useCase("https://www.reliancedigital.in/apple-iphone-15-128-gb-black/p/493839343")
        assertTrue(result is UrlValidationResult.Supported)
        val supported = result as UrlValidationResult.Supported
        assertEquals("Reliance Digital", supported.retailerName)
        assertNotNull(supported.suggestedIdentity)
        assertEquals("prod_iphone_15_128", supported.suggestedIdentity!!.resolvedProductId)
    }

    @Test
    fun `valid vijay sales url returns supported result with parsed product identity`() {
        val result = useCase("https://www.vijaysales.com/samsung-galaxy-s24-5g-128-gb-onyx-black/23491")
        assertTrue(result is UrlValidationResult.Supported)
        val supported = result as UrlValidationResult.Supported
        assertEquals("Vijay Sales", supported.retailerName)
        assertNotNull(supported.suggestedIdentity)
        assertEquals("prod_samsung_s24", supported.suggestedIdentity!!.resolvedProductId)
    }

    @Test
    fun `unsupported domain returns unsupported retailer explaining web scraping prohibition`() {
        val result = useCase("https://www.unauthorizedstore.com/item/12345")
        assertTrue(result is UrlValidationResult.UnsupportedRetailer)
        val unsupported = result as UrlValidationResult.UnsupportedRetailer
        assertEquals("www.unauthorizedstore.com", unsupported.domain)
        assertTrue(
            "Limitation message must explain authorized partner feeds and prohibit web scraping",
            unsupported.message.contains("authorized retailer partner feeds") &&
                unsupported.message.contains("scraping is disabled")
        )
    }

    @Test
    fun `user can edit and confirm product identity from pasted url`() {
        val result = useCase("https://www.amazon.in/dp/B09XS7JWHH") as UrlValidationResult.Supported
        val identity = result.suggestedIdentity!!
        assertEquals("prod_sony_wh1000xm5", identity.resolvedProductId)
        assertFalse(identity.isUserConfirmed)

        // User edits title/brand
        val corrected = identity.withCorrections(
            name = "Sony WH-1000XM5 Custom Edition",
            brand = "Sony Audio",
            model = "WH-1000XM5/B"
        )
        assertEquals("Sony WH-1000XM5 Custom Edition", corrected.effectiveTitle)
        assertEquals("Sony Audio", corrected.editedBrand)
        assertFalse(corrected.isUserConfirmed)

        // User explicitly confirms
        val confirmed = corrected.confirm()
        assertTrue(confirmed.isUserConfirmed)
        assertEquals("Sony WH-1000XM5 Custom Edition", confirmed.effectiveTitle)
    }

    @Test
    fun `empty or blank url returns invalid url result`() {
        val emptyResult = useCase("")
        assertTrue(emptyResult is UrlValidationResult.InvalidUrl)

        val blankResult = useCase("    ")
        assertTrue(blankResult is UrlValidationResult.InvalidUrl)
    }
}
