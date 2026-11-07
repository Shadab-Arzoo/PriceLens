package com.pricelens

import com.pricelens.domain.model.ProductSummary
import com.pricelens.domain.model.RecentSearch
import com.pricelens.domain.model.RetailerOffer
import com.pricelens.domain.model.WatchlistItem
import com.pricelens.domain.repository.DashboardRepository
import com.pricelens.domain.repository.ProductCatalogRepository
import com.pricelens.domain.usecase.GetDashboardDataUseCase
import com.pricelens.domain.usecase.SaveRecentSearchUseCase
import com.pricelens.domain.usecase.UrlValidationResult
import com.pricelens.domain.usecase.ValidateProductUrlUseCase
import com.pricelens.presentation.dashboard.DashboardUiEvent
import com.pricelens.presentation.dashboard.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class TestDashboardRepository : DashboardRepository {
        val searchesFlow = MutableStateFlow<List<RecentSearch>>(emptyList())
        val watchlistFlow = MutableStateFlow<List<WatchlistItem>>(emptyList())
        val savedSearches = mutableListOf<String>()

        override fun getRecentSearches(): Flow<List<RecentSearch>> = searchesFlow
        override fun getWatchlistItems(): Flow<List<WatchlistItem>> = watchlistFlow

        override suspend fun saveRecentSearch(query: String) {
            savedSearches.add(query)
            searchesFlow.value = listOf(RecentSearch(query, System.currentTimeMillis()))
        }

        override suspend fun clearRecentSearches() {
            savedSearches.clear()
            searchesFlow.value = emptyList()
        }
    }

    private class TestProductCatalogRepository : ProductCatalogRepository {
        override suspend fun searchProducts(query: String): Result<List<ProductSummary>> = Result.success(emptyList())
        override suspend fun getProductOffers(productId: String): Result<List<RetailerOffer>> = Result.success(emptyList())
        override suspend fun getProductComparison(productId: String): Result<com.pricelens.domain.model.ProductComparisonDetails> =
            Result.failure(NotImplementedError())
        override suspend fun getTrendingProducts(): Result<List<ProductSummary>> = Result.success(emptyList())
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
    fun `initial load sets loading to false and loads empty states correctly`() = runTest {
        val dashboardRepo = TestDashboardRepository()
        val catalogRepo = TestProductCatalogRepository()
        val getDashboardDataUseCase = GetDashboardDataUseCase(dashboardRepo, catalogRepo)
        val saveRecentSearchUseCase = SaveRecentSearchUseCase(dashboardRepo)
        val validateProductUrlUseCase = ValidateProductUrlUseCase()

        val viewModel = DashboardViewModel(
            getDashboardDataUseCase = getDashboardDataUseCase,
            saveRecentSearchUseCase = saveRecentSearchUseCase,
            validateProductUrlUseCase = validateProductUrlUseCase,
            dashboardRepository = dashboardRepo
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertTrue(state.recentSearches.isEmpty())
        assertTrue(state.watchlistItems.isEmpty())
        assertTrue(state.trendingProducts.isEmpty())
    }

    @Test
    fun `search query change updates ui state`() = runTest {
        val dashboardRepo = TestDashboardRepository()
        val catalogRepo = TestProductCatalogRepository()
        val viewModel = DashboardViewModel(
            getDashboardDataUseCase = GetDashboardDataUseCase(dashboardRepo, catalogRepo),
            saveRecentSearchUseCase = SaveRecentSearchUseCase(dashboardRepo),
            validateProductUrlUseCase = ValidateProductUrlUseCase(),
            dashboardRepository = dashboardRepo
        )

        viewModel.onEvent(DashboardUiEvent.SearchQueryChanged("Pixel 9"))

        assertEquals("Pixel 9", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `submitting search saves query and clears input`() = runTest {
        val dashboardRepo = TestDashboardRepository()
        val catalogRepo = TestProductCatalogRepository()
        val viewModel = DashboardViewModel(
            getDashboardDataUseCase = GetDashboardDataUseCase(dashboardRepo, catalogRepo),
            saveRecentSearchUseCase = SaveRecentSearchUseCase(dashboardRepo),
            validateProductUrlUseCase = ValidateProductUrlUseCase(),
            dashboardRepository = dashboardRepo
        )

        advanceUntilIdle()

        viewModel.onEvent(DashboardUiEvent.SubmitSearch("MacBook Air M3"))
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.searchQuery)
        assertEquals(1, dashboardRepo.savedSearches.size)
        assertEquals("MacBook Air M3", dashboardRepo.savedSearches[0])
    }

    @Test
    fun `paste url sheet open and input validation works`() = runTest {
        val dashboardRepo = TestDashboardRepository()
        val catalogRepo = TestProductCatalogRepository()
        val viewModel = DashboardViewModel(
            getDashboardDataUseCase = GetDashboardDataUseCase(dashboardRepo, catalogRepo),
            saveRecentSearchUseCase = SaveRecentSearchUseCase(dashboardRepo),
            validateProductUrlUseCase = ValidateProductUrlUseCase(),
            dashboardRepository = dashboardRepo
        )

        advanceUntilIdle()

        viewModel.onEvent(DashboardUiEvent.OpenPasteUrlSheet)
        assertTrue(viewModel.uiState.value.isPasteUrlSheetOpen)

        viewModel.onEvent(DashboardUiEvent.PasteUrlInputChanged("https://www.amazon.in/dp/B0CHX1W1XY"))
        val validation = viewModel.uiState.value.urlValidation
        assertTrue(validation is UrlValidationResult.Supported)
        assertEquals("Amazon India", (validation as UrlValidationResult.Supported).retailerName)

        viewModel.onEvent(DashboardUiEvent.ClosePasteUrlSheet)
        assertFalse(viewModel.uiState.value.isPasteUrlSheetOpen)
    }
}
