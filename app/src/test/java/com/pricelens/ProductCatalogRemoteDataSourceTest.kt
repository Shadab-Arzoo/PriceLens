package com.pricelens

import com.pricelens.data.remote.adapter.SandboxRetailerAdapter
import com.pricelens.data.remote.datasource.ProductCatalogRemoteDataSourceImpl
import com.pricelens.domain.model.PriceLensException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProductCatalogRemoteDataSourceTest {

    private lateinit var sandboxAdapter: SandboxRetailerAdapter
    private lateinit var remoteDataSource: ProductCatalogRemoteDataSourceImpl

    @Before
    fun setUp() {
        sandboxAdapter = SandboxRetailerAdapter()
        remoteDataSource = ProductCatalogRemoteDataSourceImpl(sandboxAdapter, com.pricelens.domain.service.AlternativesService())
    }

    @Test
    fun `searchProducts returns normalized real offers for valid query`() = runTest {
        val result = remoteDataSource.searchProducts("Sony")

        assertTrue(result.isSuccess)
        val products = result.getOrNull()
        assertNotNull(products)
        assertFalse(products!!.isEmpty())

        val sonyHeadphones = products.first { it.id == "prod_sony_wh1000xm5" }
        assertEquals("Sony", sonyHeadphones.brand)
        assertEquals("WH-1000XM5/B", sonyHeadphones.modelNumber)
        assertEquals(26990.0, sonyHeadphones.lowestPrice!!, 0.01)
        assertEquals("INR", sonyHeadphones.currency)
        assertEquals(3, sonyHeadphones.retailerCount)
    }

    @Test
    fun `searchProducts returns empty list for non-matching query without fabricating items`() = runTest {
        val result = remoteDataSource.searchProducts("NonExistentBrandXYZ123")

        assertTrue(result.isSuccess)
        val products = result.getOrNull()
        assertNotNull(products)
        assertTrue(products!!.isEmpty())
    }

    @Test
    fun `getProductComparison returns truthful offers and explicit unavailable retailers`() = runTest {
        val result = remoteDataSource.getProductComparison("prod_sony_wh1000xm5")

        assertTrue(result.isSuccess)
        val details = result.getOrNull()
        assertNotNull(details)
        assertEquals("prod_sony_wh1000xm5", details!!.id)
        assertEquals("Sony", details.brand)

        // Verify offers sorted lowest price first
        assertEquals(3, details.offers.size)
        assertEquals("amazon_in", details.offers[0].retailerId)
        assertEquals(26990.0, details.offers[0].price!!, 0.01)
        assertEquals("croma", details.offers[1].retailerId)
        assertEquals(27490.0, details.offers[1].price!!, 0.01)
        assertEquals("flipkart", details.offers[2].retailerId)
        assertEquals(28999.0, details.offers[2].price!!, 0.01)

        // Verify lowestPriceOffer
        assertNotNull(details.lowestPriceOffer)
        assertEquals("amazon_in", details.lowestPriceOffer!!.retailerId)
        assertEquals(26990.0, details.lowestPriceOffer!!.price!!, 0.01)

        // Verify explicit unavailable retailers are present with reasons
        assertEquals(2, details.unavailableRetailers.size)
        val reliance = details.unavailableRetailers.first { it.retailerId == "reliance_digital" }
        assertEquals("Reliance Digital", reliance.retailerName)
        assertEquals("Temporarily out of stock", reliance.reason)

        val vijaySales = details.unavailableRetailers.first { it.retailerId == "vijay_sales" }
        assertEquals("Vijay Sales", vijaySales.retailerName)
        assertTrue(vijaySales.reason.contains("No active listing"))
    }

    @Test
    fun `uncertain product match rejects merging and returns UncertainMatchException`() = runTest {
        // Product configured with confidence score 0.65 (< 0.85 threshold)
        val result = remoteDataSource.getProductComparison("prod_uncertain_match")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is PriceLensException.UncertainMatchException)
        assertTrue(
            (error as PriceLensException).safeUserMessage.contains("confidence", ignoreCase = true) ||
                    error.safeUserMessage.contains("verified", ignoreCase = true) ||
                    error.safeUserMessage.contains("matched", ignoreCase = true)
        )
    }

    @Test
    fun `provider error 503 produces safe user message`() = runTest {
        val result = remoteDataSource.searchProducts("simulate_provider_error")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is PriceLensException.ProviderUnavailableException)
        val safeMessage = (error as PriceLensException).safeUserMessage
        assertTrue(safeMessage.contains("temporarily unavailable", ignoreCase = true))
        assertFalse(safeMessage.contains("503")) // No raw HTTP codes leaked to end user
    }

    @Test
    fun `rate limit 429 produces safe rate limit message`() = runTest {
        val result = remoteDataSource.searchProducts("simulate_rate_limit")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is PriceLensException.RateLimitException)
        val safeMessage = (error as PriceLensException).safeUserMessage
        assertTrue(safeMessage.contains("limit reached", ignoreCase = true))
    }

    @Test
    fun `malformed provider response produces safe MalformedDataException`() = runTest {
        val result = remoteDataSource.searchProducts("simulate_malformed")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is PriceLensException.MalformedDataException)
        val safeMessage = (error as PriceLensException).safeUserMessage
        assertFalse(safeMessage.contains("Unexpected token")) // Cryptic parser trace not shown
    }

    @Test
    fun `network connection failure produces safe NetworkConnectionException`() = runTest {
        val result = remoteDataSource.searchProducts("simulate_network_error")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is PriceLensException.NetworkConnectionException)
        val safeMessage = (error as PriceLensException).safeUserMessage
        assertTrue(safeMessage.contains("network", ignoreCase = true) || safeMessage.contains("internet", ignoreCase = true))
    }
}
