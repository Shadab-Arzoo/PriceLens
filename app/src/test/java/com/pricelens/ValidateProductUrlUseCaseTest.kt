package com.pricelens

import com.pricelens.domain.usecase.UrlValidationResult
import com.pricelens.domain.usecase.ValidateProductUrlUseCase
import org.junit.Assert.assertEquals
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
    fun `valid amazon india url returns supported result`() {
        val result = useCase("https://www.amazon.in/Apple-iPhone-15-128-GB/dp/B0CHX1W1XY")
        assertTrue(result is UrlValidationResult.Supported)
        assertEquals("Amazon India", (result as UrlValidationResult.Supported).retailerName)
    }

    @Test
    fun `valid flipkart url returns supported result`() {
        val result = useCase("https://www.flipkart.com/apple-iphone-15-black-128-gb/p/itm6ac6485515ae4")
        assertTrue(result is UrlValidationResult.Supported)
        assertEquals("Flipkart", (result as UrlValidationResult.Supported).retailerName)
    }

    @Test
    fun `valid croma url returns supported result`() {
        val result = useCase("https://www.croma.com/apple-iphone-15-128gb-black-/p/300780")
        assertTrue(result is UrlValidationResult.Supported)
        assertEquals("Croma", (result as UrlValidationResult.Supported).retailerName)
    }

    @Test
    fun `valid reliance digital url returns supported result`() {
        val result = useCase("https://www.reliancedigital.in/apple-iphone-15-128-gb-black/p/493839343")
        assertTrue(result is UrlValidationResult.Supported)
        assertEquals("Reliance Digital", (result as UrlValidationResult.Supported).retailerName)
    }

    @Test
    fun `unsupported domain returns unsupported retailer result`() {
        val result = useCase("https://www.randomshop.com/item/12345")
        assertTrue(result is UrlValidationResult.UnsupportedRetailer)
        assertEquals("www.randomshop.com", (result as UrlValidationResult.UnsupportedRetailer).domain)
    }

    @Test
    fun `empty or blank url returns invalid url result`() {
        val emptyResult = useCase("")
        assertTrue(emptyResult is UrlValidationResult.InvalidUrl)

        val blankResult = useCase("    ")
        assertTrue(blankResult is UrlValidationResult.InvalidUrl)
    }
}
