package com.pricelens

import com.pricelens.data.remote.adapter.SandboxRetailerAdapter
import com.pricelens.data.remote.datasource.ProductCatalogRemoteDataSourceImpl
import com.pricelens.data.repository.ProductCatalogRepositoryImpl
import com.pricelens.domain.model.ProductSummary
import com.pricelens.domain.model.RecentSearch
import com.pricelens.domain.model.WatchlistItem
import com.pricelens.domain.repository.DashboardRepository
import com.pricelens.domain.usecase.GetProductComparisonUseCase
import com.pricelens.domain.usecase.SaveRecentSearchUseCase
import com.pricelens.domain.usecase.SearchProductsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProductComparisonIntegrationTest {

    private class InMemoryDashboardRepository : DashboardRepository {
        val recordedSearches = mutableListOf<String>()
        override fun getRecentSearches(): Flow<List<RecentSearch>> = emptyFlow()
        override fun getWatchlistItems(): Flow<List<WatchlistItem>> = emptyFlow()
        override suspend fun saveRecentSearch(query: String) { recordedSearches.add(query) }
        override suspend fun clearRecentSearches() { recordedSearches.clear() }
    }

    private lateinit var sandboxAdapter: SandboxRetailerAdapter
    private lateinit var remoteDataSource: ProductCatalogRemoteDataSourceImpl
    private lateinit var catalogRepository: ProductCatalogRepositoryImpl
    private lateinit var dashboardRepository: InMemoryDashboardRepository
    private lateinit var searchUseCase: SearchProductsUseCase
    private lateinit var comparisonUseCase: GetProductComparisonUseCase

    @Before
    fun setUp() {
        sandboxAdapter = SandboxRetailerAdapter()
        remoteDataSource = ProductCatalogRemoteDataSourceImpl(sandboxAdapter, com.pricelens.domain.service.AlternativesService())
        catalogRepository = ProductCatalogRepositoryImpl(remoteDataSource)
        dashboardRepository = InMemoryDashboardRepository()
        val saveRecentSearchUseCase = SaveRecentSearchUseCase(dashboardRepository)
        searchUseCase = SearchProductsUseCase(catalogRepository, saveRecentSearchUseCase)
        comparisonUseCase = GetProductComparisonUseCase(catalogRepository)
    }

    @Test
    fun `end to end search to comparison flow for approved Sony headphones`() = runTest {
        // Step 1: User searches for "Sony WH-1000XM5"
        val searchResult = searchUseCase("Sony WH-1000XM5")
        assertTrue("Search should succeed", searchResult.isSuccess)

        val products = searchResult.getOrNull()
        assertNotNull(products)
        assertFalse("Should return matching products", products!!.isEmpty())

        val targetProduct: ProductSummary = products.first { it.id == "prod_sony_wh1000xm5" }
        assertEquals("Sony", targetProduct.brand)
        assertEquals("WH-1000XM5/B", targetProduct.modelNumber)
        assertEquals(26990.0, targetProduct.lowestPrice!!, 0.01)
        assertEquals("INR", targetProduct.currency)
        assertEquals(3, targetProduct.retailerCount)
        assertTrue(dashboardRepository.recordedSearches.contains("Sony WH-1000XM5"))

        // Step 2: User taps product to open comparison screen
        val comparisonResult = comparisonUseCase(targetProduct.id)
        assertTrue("Comparison retrieval should succeed", comparisonResult.isSuccess)

        val comparisonDetails = comparisonResult.getOrNull()
        assertNotNull(comparisonDetails)

        // Verify product metadata
        assertEquals("Sony WH-1000XM5 Wireless Noise Cancelling Headphones", comparisonDetails!!.name)
        assertEquals("Sony", comparisonDetails.brand)
        assertEquals("WH-1000XM5/B", comparisonDetails.modelNumber)
        assertTrue("Matching confidence score must meet 0.85 threshold", comparisonDetails.confidenceScore >= 0.85)

        // Verify truthful retailer offers
        assertEquals("Should have exactly 3 available retailer offers", 3, comparisonDetails.offers.size)

        val amazonOffer = comparisonDetails.offers.first { it.retailerId == "amazon_in" }
        assertEquals("Amazon India", amazonOffer.retailerName)
        assertEquals(26990.0, amazonOffer.price!!, 0.01)
        assertEquals("INR", amazonOffer.currency)
        assertTrue(amazonOffer.isAvailable)
        assertTrue(amazonOffer.productUrl.startsWith("https://www.amazon.in"))
        assertTrue("Timestamp should be recorded", amazonOffer.observedAtTimestamp > 0)

        val cromaOffer = comparisonDetails.offers.first { it.retailerId == "croma" }
        assertEquals("Croma", cromaOffer.retailerName)
        assertEquals(27490.0, cromaOffer.price!!, 0.01)

        val flipkartOffer = comparisonDetails.offers.first { it.retailerId == "flipkart" }
        assertEquals("Flipkart", flipkartOffer.retailerName)
        assertEquals(28999.0, flipkartOffer.price!!, 0.01)

        // Verify best offer is Amazon India at lowest price
        assertNotNull(comparisonDetails.lowestPriceOffer)
        assertEquals("amazon_in", comparisonDetails.lowestPriceOffer!!.retailerId)
        assertEquals(26990.0, comparisonDetails.lowestPriceOffer!!.price!!, 0.01)

        // Verify explicit unavailable states for retailers without offers
        assertEquals("Should list 2 unavailable retailers", 2, comparisonDetails.unavailableRetailers.size)

        val relianceUnavailable = comparisonDetails.unavailableRetailers.first { it.retailerId == "reliance_digital" }
        assertEquals("Reliance Digital", relianceUnavailable.retailerName)
        assertEquals("Temporarily out of stock", relianceUnavailable.reason)

        val vijayUnavailable = comparisonDetails.unavailableRetailers.first { it.retailerId == "vijay_sales" }
        assertEquals("Vijay Sales", vijayUnavailable.retailerName)
        assertTrue(vijayUnavailable.reason.contains("No active listing"))
    }

    @Test
    fun `end to end search to comparison flow for approved Apple iPhone 15`() = runTest {
        val searchResult = searchUseCase("iPhone 15")
        assertTrue(searchResult.isSuccess)
        val products = searchResult.getOrNull()!!
        val iPhone = products.first { it.id == "prod_iphone_15_128" }
        assertEquals("Apple", iPhone.brand)
        assertEquals(69900.0, iPhone.lowestPrice!!, 0.01)

        val comparisonResult = comparisonUseCase(iPhone.id)
        assertTrue(comparisonResult.isSuccess)
        val details = comparisonResult.getOrNull()!!

        assertEquals(4, details.offers.size) // Amazon, Flipkart, Croma, Reliance Digital
        assertEquals(69900.0, details.lowestPriceOffer!!.price!!, 0.01)
        assertEquals("amazon_in", details.lowestPriceOffer!!.retailerId)

        assertEquals(1, details.unavailableRetailers.size) // Vijay Sales
        assertEquals("vijay_sales", details.unavailableRetailers[0].retailerId)
        assertTrue(details.unavailableRetailers[0].reason.contains("unavailable"))
    }
}
