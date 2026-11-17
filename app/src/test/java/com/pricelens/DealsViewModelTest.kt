package com.pricelens

import com.pricelens.domain.model.ProductAlternative
import com.pricelens.domain.model.ProductComparisonDetails
import com.pricelens.domain.model.ProductSummary
import com.pricelens.domain.model.RetailerOffer
import com.pricelens.domain.model.TargetPriceAlert
import com.pricelens.domain.repository.ProductCatalogRepository
import com.pricelens.domain.repository.TargetPriceAlertRepository
import com.pricelens.presentation.deals.BudgetFilter
import com.pricelens.presentation.deals.DealCategory
import com.pricelens.presentation.deals.DealSortOrder
import com.pricelens.presentation.deals.DealsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DealsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeProductCatalogRepository : ProductCatalogRepository {
        var products: List<ProductSummary> = listOf(
            ProductSummary(
                id = "prod_iphone_16",
                name = "Apple iPhone 16 (128GB)",
                brand = "Apple",
                lowestPrice = 74999.0,
                highestPrice = 79900.0,
                lowestPriceRetailer = "Amazon India",
                retailerCount = 5
            ),
            ProductSummary(
                id = "prod_sony_xm5",
                name = "Sony WH-1000XM5 ANC Headphones",
                brand = "Sony",
                lowestPrice = 26990.0,
                highestPrice = 34990.0,
                lowestPriceRetailer = "Flipkart",
                retailerCount = 4
            ),
            ProductSummary(
                id = "prod_macbook_m3",
                name = "Apple MacBook Air M3 (16GB)",
                brand = "Apple",
                lowestPrice = 104990.0,
                highestPrice = 114900.0,
                lowestPriceRetailer = "Croma",
                retailerCount = 5
            )
        )

        override suspend fun searchProducts(query: String): Result<List<ProductSummary>> {
            return Result.success(products)
        }

        override suspend fun getProductOffers(productId: String): Result<List<RetailerOffer>> = Result.success(emptyList())
        override suspend fun getProductComparison(productId: String): Result<ProductComparisonDetails> = Result.failure(Exception())
        override suspend fun getTrendingProducts(): Result<List<ProductSummary>> = Result.success(products)
        override suspend fun getProductAlternatives(productId: String): Result<List<ProductAlternative>> = Result.success(emptyList())
    }

    private class FakeAlertRepository : TargetPriceAlertRepository {
        val savedAlerts = mutableListOf<TargetPriceAlert>()
        override suspend fun save(alert: TargetPriceAlert) { savedAlerts.add(alert) }
        override suspend fun getActiveAlerts(productId: String): List<TargetPriceAlert> = savedAlerts.filter { it.productId == productId }
        override suspend fun deleteAlert(alertId: String) { savedAlerts.removeAll { it.id == alertId } }
        override fun observeAlerts(productId: String): Flow<List<TargetPriceAlert>> = emptyFlow()
        override suspend fun evaluate(alert: TargetPriceAlert, observation: com.pricelens.domain.model.PriceObservation): TargetPriceAlert = alert
        override suspend fun notifications(alertId: String): List<com.pricelens.domain.model.NotificationRecord> = emptyList()
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads deals correctly and sets spotlight deal`() = runTest {
        val catalogRepo = FakeProductCatalogRepository()
        val alertRepo = FakeAlertRepository()
        val viewModel = DealsViewModel(catalogRepo, alertRepo)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.deals.size)
        assertNotNull(state.spotlightDeal)
        assertEquals("prod_sony_xm5", state.spotlightDeal?.productId)
    }

    @Test
    fun `filtering by category filters deals correctly`() = runTest {
        val catalogRepo = FakeProductCatalogRepository()
        val viewModel = DealsViewModel(catalogRepo)

        advanceUntilIdle()
        viewModel.onCategorySelected(DealCategory.AUDIO)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.deals.size)
        assertEquals("Sony WH-1000XM5 ANC Headphones", state.deals.first().title)
    }

    @Test
    fun `filtering by budget works correctly`() = runTest {
        val catalogRepo = FakeProductCatalogRepository()
        val viewModel = DealsViewModel(catalogRepo)

        advanceUntilIdle()
        viewModel.onBudgetSelected(BudgetFilter.UNDER_50K)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.deals.size)
        assertEquals("prod_sony_xm5", state.deals.first().productId)
    }

    @Test
    fun `sorting by price orders deals ascending`() = runTest {
        val catalogRepo = FakeProductCatalogRepository()
        val viewModel = DealsViewModel(catalogRepo)

        advanceUntilIdle()
        viewModel.onSortSelected(DealSortOrder.PRICE_LOW_TO_HIGH)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("prod_sony_xm5", state.deals[0].productId)
        assertEquals("prod_iphone_16", state.deals[1].productId)
        assertEquals("prod_macbook_m3", state.deals[2].productId)
    }

    @Test
    fun `quickSetAlert saves alert to repository and posts feedback`() = runTest {
        val catalogRepo = FakeProductCatalogRepository()
        val alertRepo = FakeAlertRepository()
        val viewModel = DealsViewModel(catalogRepo, alertRepo)

        advanceUntilIdle()
        val deal = viewModel.uiState.value.deals.first()
        viewModel.quickSetAlert(deal, 25000.0)
        advanceUntilIdle()

        assertEquals(1, alertRepo.savedAlerts.size)
        assertEquals(25000.0, alertRepo.savedAlerts.first().targetPrice, 0.01)
        assertTrue(viewModel.uiState.value.notificationMessage?.contains("Price Alert set") == true)
    }

    @Test
    fun `search query filters deals by text matching title or store`() = runTest {
        val catalogRepo = FakeProductCatalogRepository()
        val viewModel = DealsViewModel(catalogRepo)

        advanceUntilIdle()
        viewModel.onSearchQueryChanged("MacBook")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.deals.size)
        assertEquals("Apple MacBook Air M3 (16GB)", state.deals.first().title)
    }

    @Test
    fun `min discount percentage filters deals appropriately`() = runTest {
        val catalogRepo = FakeProductCatalogRepository()
        val viewModel = DealsViewModel(catalogRepo)

        advanceUntilIdle()
        viewModel.onMinDiscountChanged(20)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.deals.size)
        assertEquals("prod_sony_xm5", state.deals.first().productId)
    }
}
