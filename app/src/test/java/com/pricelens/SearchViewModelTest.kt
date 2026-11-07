package com.pricelens

import androidx.lifecycle.SavedStateHandle
import com.pricelens.domain.model.PriceLensException
import com.pricelens.domain.model.ProductSummary
import com.pricelens.domain.repository.DashboardRepository
import com.pricelens.domain.repository.ProductCatalogRepository
import com.pricelens.domain.usecase.SaveRecentSearchUseCase
import com.pricelens.domain.usecase.SearchProductsUseCase
import com.pricelens.presentation.search.SearchUiEvent
import com.pricelens.presentation.search.SearchViewModel
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeDashboardRepository : DashboardRepository {
        val saved = mutableListOf<String>()
        override fun getRecentSearches(): Flow<List<com.pricelens.domain.model.RecentSearch>> = emptyFlow()
        override fun getWatchlistItems(): Flow<List<com.pricelens.domain.model.WatchlistItem>> = emptyFlow()
        override suspend fun saveRecentSearch(query: String) { saved.add(query) }
        override suspend fun clearRecentSearches() { saved.clear() }
    }

    private class FakeProductCatalogRepository : ProductCatalogRepository {
        var shouldFail: Boolean = false
        var failureException: Exception = PriceLensException.ProviderUnavailableException("Feed down")
        var productsToReturn: List<ProductSummary> = listOf(
            ProductSummary(
                id = "prod_sony_wh1000xm5",
                name = "Sony WH-1000XM5",
                brand = "Sony",
                lowestPrice = 26990.0,
                retailerCount = 3
            )
        )

        override suspend fun searchProducts(query: String): Result<List<ProductSummary>> {
            return if (shouldFail) {
                Result.failure(failureException)
            } else {
                Result.success(productsToReturn.filter { it.name.contains(query, ignoreCase = true) })
            }
        }

        override suspend fun getProductOffers(productId: String): Result<List<com.pricelens.domain.model.RetailerOffer>> =
            Result.success(emptyList())

        override suspend fun getProductComparison(productId: String): Result<com.pricelens.domain.model.ProductComparisonDetails> =
            Result.failure(NotImplementedError())

        override suspend fun getTrendingProducts(): Result<List<ProductSummary>> = Result.success(emptyList())
    }

    private lateinit var dashboardRepo: FakeDashboardRepository
    private lateinit var catalogRepo: FakeProductCatalogRepository
    private lateinit var searchUseCase: SearchProductsUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dashboardRepo = FakeDashboardRepository()
        catalogRepo = FakeProductCatalogRepository()
        val saveRecentSearchUseCase = SaveRecentSearchUseCase(dashboardRepo)
        searchUseCase = SearchProductsUseCase(catalogRepo, saveRecentSearchUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state with empty SavedStateHandle is blank and not loading`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = SearchViewModel(searchUseCase, savedStateHandle)

        val state = viewModel.uiState.value
        assertEquals("", state.query)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertTrue(state.products.isEmpty())
        assertFalse(state.isSubmitted)
    }

    @Test
    fun `initial query from SavedStateHandle automatically initiates search`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("q" to "Sony"))
        val viewModel = SearchViewModel(searchUseCase, savedStateHandle)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Sony", state.query)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(1, state.products.size)
        assertEquals("prod_sony_wh1000xm5", state.products[0].id)
        assertTrue(dashboardRepo.saved.contains("Sony"))
    }

    @Test
    fun `query changed updates state query without searching`() = runTest {
        val viewModel = SearchViewModel(searchUseCase, SavedStateHandle())

        viewModel.onEvent(SearchUiEvent.QueryChanged("Apple"))

        val state = viewModel.uiState.value
        assertEquals("Apple", state.query)
        assertFalse(state.isLoading)
        assertTrue(state.products.isEmpty())
    }

    @Test
    fun `submitting search loads matching products successfully`() = runTest {
        val viewModel = SearchViewModel(searchUseCase, SavedStateHandle())

        viewModel.onEvent(SearchUiEvent.SubmitSearch("Sony"))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(1, state.products.size)
        assertEquals(26990.0, state.products[0].lowestPrice!!, 0.01)
    }

    @Test
    fun `provider failure produces safe user error message`() = runTest {
        catalogRepo.shouldFail = true
        catalogRepo.failureException = PriceLensException.ProviderUnavailableException(
            message = "503 Internal Remote Error",
            safeUserMessage = "Pricing service is temporarily unavailable. Please try again."
        )

        val viewModel = SearchViewModel(searchUseCase, SavedStateHandle())
        viewModel.onEvent(SearchUiEvent.SubmitSearch("Sony"))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.products.isEmpty())
        assertEquals("Pricing service is temporarily unavailable. Please try again.", state.errorMessage)
    }

    @Test
    fun `retry event reruns search with current query`() = runTest {
        catalogRepo.shouldFail = true
        val viewModel = SearchViewModel(searchUseCase, SavedStateHandle())
        viewModel.onEvent(SearchUiEvent.SubmitSearch("Sony"))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.errorMessage != null)

        // Fix failure and retry
        catalogRepo.shouldFail = false
        viewModel.onEvent(SearchUiEvent.RetrySearch)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertEquals(1, state.products.size)
    }
}
