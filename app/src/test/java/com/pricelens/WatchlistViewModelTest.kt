package com.pricelens

import com.pricelens.domain.model.NotificationRecord
import com.pricelens.domain.model.PriceObservation
import com.pricelens.domain.model.RecentSearch
import com.pricelens.domain.model.TargetPriceAlert
import com.pricelens.domain.model.WatchlistItem
import com.pricelens.domain.repository.DashboardRepository
import com.pricelens.domain.repository.TargetPriceAlertRepository
import com.pricelens.presentation.watchlist.WatchlistFilter
import com.pricelens.presentation.watchlist.WatchlistSortOrder
import com.pricelens.presentation.watchlist.WatchlistUiEvent
import com.pricelens.presentation.watchlist.WatchlistViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchlistViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeDashboardRepository : DashboardRepository {
        val itemsFlow = MutableStateFlow<List<WatchlistItem>>(emptyList())
        override fun getRecentSearches(): Flow<List<RecentSearch>> = MutableStateFlow(emptyList())
        override fun getWatchlistItems(): Flow<List<WatchlistItem>> = itemsFlow.asStateFlow()
        override suspend fun saveRecentSearch(query: String) {}
        override suspend fun clearRecentSearches() {}
        override suspend fun addToWatchlist(item: WatchlistItem) {
            itemsFlow.value = itemsFlow.value + item
        }
        override suspend fun removeFromWatchlist(productId: String) {
            itemsFlow.value = itemsFlow.value.filter { it.productId != productId }
        }
    }

    private class FakeTargetPriceAlertRepository : TargetPriceAlertRepository {
        val alerts = mutableListOf<TargetPriceAlert>()
        override fun observeAlerts(productId: String): Flow<List<TargetPriceAlert>> = MutableStateFlow(emptyList())
        override suspend fun getActiveAlerts(productId: String): List<TargetPriceAlert> = alerts.filter { it.productId == productId }
        override suspend fun save(alert: TargetPriceAlert) { alerts.add(alert) }
        override suspend fun deleteAlert(alertId: String) { alerts.removeAll { it.id == alertId } }
        override suspend fun evaluate(alert: TargetPriceAlert, observation: PriceObservation): TargetPriceAlert = alert
        override suspend fun notifications(alertId: String): List<NotificationRecord> = emptyList()
    }

    private lateinit var dashboardRepo: FakeDashboardRepository
    private lateinit var alertRepo: FakeTargetPriceAlertRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dashboardRepo = FakeDashboardRepository()
        alertRepo = FakeTargetPriceAlertRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads watchlist items from repository`() = runTest {
        val sampleItem = WatchlistItem(
            productId = "prod_1",
            productName = "Product 1",
            targetPrice = 1000.0,
            currentLowestPrice = 900.0,
            currency = "INR",
            addedAtTimestamp = 1700000000000L
        )
        dashboardRepo.itemsFlow.value = listOf(sampleItem)

        val viewModel = WatchlistViewModel(dashboardRepo, alertRepo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.totalTrackedCount)
        assertEquals(1, state.priceDropCount)
        assertEquals(1, state.filteredItems.size)
    }

    @Test
    fun `switching filter to PRICE_DROPS filters out items where target price is not met`() = runTest {
        val dropItem = WatchlistItem(
            productId = "prod_1",
            productName = "Discounted Item",
            targetPrice = 1000.0,
            currentLowestPrice = 850.0,
            currency = "INR",
            addedAtTimestamp = 1700000000000L
        )
        val regularItem = WatchlistItem(
            productId = "prod_2",
            productName = "Regular Item",
            targetPrice = 500.0,
            currentLowestPrice = 750.0,
            currency = "INR",
            addedAtTimestamp = 1700000000000L
        )
        dashboardRepo.itemsFlow.value = listOf(dropItem, regularItem)

        val viewModel = WatchlistViewModel(dashboardRepo, alertRepo)
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.totalTrackedCount)
        assertEquals(1, viewModel.uiState.value.priceDropCount)

        viewModel.onEvent(WatchlistUiEvent.FilterChanged(WatchlistFilter.PRICE_DROPS))
        assertEquals(1, viewModel.uiState.value.filteredItems.size)
        assertEquals("prod_1", viewModel.uiState.value.filteredItems.first().productId)

        viewModel.onEvent(WatchlistUiEvent.FilterChanged(WatchlistFilter.ALL))
        assertEquals(2, viewModel.uiState.value.filteredItems.size)
    }

    @Test
    fun `removing item deletes from watchlist and sets notification message`() = runTest {
        val sampleItem = WatchlistItem(
            productId = "prod_1",
            productName = "Product 1",
            targetPrice = 1000.0,
            currentLowestPrice = 900.0,
            currency = "INR",
            addedAtTimestamp = 1700000000000L
        )
        dashboardRepo.itemsFlow.value = listOf(sampleItem)

        val viewModel = WatchlistViewModel(dashboardRepo, alertRepo)
        advanceUntilIdle()

        viewModel.onEvent(WatchlistUiEvent.RemoveItem("prod_1"))
        advanceUntilIdle()

        assertTrue(dashboardRepo.itemsFlow.value.isEmpty())
        assertEquals("Removed product from watchlist", viewModel.uiState.value.notificationMessage)
    }

    @Test
    fun `sorting items by price works correctly`() = runTest {
        val cheapItem = WatchlistItem(
            productId = "prod_cheap",
            productName = "Cheap Product",
            targetPrice = 200.0,
            currentLowestPrice = 150.0,
            currency = "INR",
            addedAtTimestamp = 1700000000000L
        )
        val expensiveItem = WatchlistItem(
            productId = "prod_expensive",
            productName = "Expensive Product",
            targetPrice = 2000.0,
            currentLowestPrice = 1500.0,
            currency = "INR",
            addedAtTimestamp = 1700000000000L
        )
        dashboardRepo.itemsFlow.value = listOf(expensiveItem, cheapItem)

        val viewModel = WatchlistViewModel(dashboardRepo, alertRepo)
        advanceUntilIdle()

        viewModel.onEvent(WatchlistUiEvent.SortOrderChanged(WatchlistSortOrder.PRICE_ASC))
        assertEquals("prod_cheap", viewModel.uiState.value.filteredItems.first().productId)

        viewModel.onEvent(WatchlistUiEvent.SortOrderChanged(WatchlistSortOrder.PRICE_DESC))
        assertEquals("prod_expensive", viewModel.uiState.value.filteredItems.first().productId)
    }

    @Test
    fun `sorting by biggest savings ranks items by savings correctly`() = runTest {
        val smallSavingItem = WatchlistItem(
            productId = "prod_small",
            productName = "Small Drop",
            targetPrice = 1000.0,
            currentLowestPrice = 950.0, // savings = 50
            currency = "INR",
            addedAtTimestamp = 1700000000000L
        )
        val bigSavingItem = WatchlistItem(
            productId = "prod_big",
            productName = "Big Drop",
            targetPrice = 2000.0,
            currentLowestPrice = 1200.0, // savings = 800
            currency = "INR",
            addedAtTimestamp = 1700000000000L
        )
        dashboardRepo.itemsFlow.value = listOf(smallSavingItem, bigSavingItem)

        val viewModel = WatchlistViewModel(dashboardRepo, alertRepo)
        advanceUntilIdle()

        assertEquals(850.0, viewModel.uiState.value.totalPotentialSavings, 0.01)

        viewModel.onEvent(WatchlistUiEvent.SortOrderChanged(WatchlistSortOrder.BIGGEST_SAVINGS))
        assertEquals("prod_big", viewModel.uiState.value.filteredItems.first().productId)
        assertEquals("prod_small", viewModel.uiState.value.filteredItems.last().productId)
    }
}
