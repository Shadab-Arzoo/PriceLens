package com.pricelens

import androidx.lifecycle.SavedStateHandle
import com.pricelens.domain.model.NotificationRecord
import com.pricelens.domain.model.PriceLensException
import com.pricelens.domain.model.PriceObservation
import com.pricelens.domain.model.ProductComparisonDetails
import com.pricelens.domain.model.ProductSummary
import com.pricelens.domain.model.RetailerOffer
import com.pricelens.domain.model.TargetPriceAlert
import com.pricelens.domain.model.UnavailableRetailer
import com.pricelens.domain.repository.ProductCatalogRepository
import com.pricelens.domain.repository.TargetPriceAlertRepository
import com.pricelens.domain.service.AlertEvaluator
import com.pricelens.domain.usecase.GetProductComparisonUseCase
import com.pricelens.presentation.comparison.ProductComparisonUiEvent
import com.pricelens.presentation.comparison.ProductComparisonViewModel
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductComparisonViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeProductCatalogRepository : ProductCatalogRepository {
        var shouldFail: Boolean = false
        var failureException: Exception = PriceLensException.ProviderUnavailableException("Feed down")

        val sampleDetails = ProductComparisonDetails(
            id = "prod_sony_wh1000xm5",
            name = "Sony WH-1000XM5 Wireless Headphones",
            brand = "Sony",
            modelNumber = "WH-1000XM5/B",
            description = "Noise cancelling headphones",
            specifications = mapOf("Driver" to "30mm", "Battery" to "30h"),
            offers = listOf(
                RetailerOffer(
                    retailerId = "amazon_in",
                    retailerName = "Amazon India",
                    price = 26990.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://amazon.in/dp/B09XS7JWHH",
                    observedAtTimestamp = 1700000000000L
                ),
                RetailerOffer(
                    retailerId = "croma",
                    retailerName = "Croma",
                    price = 27490.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://croma.com/p/251390",
                    observedAtTimestamp = 1700000000000L
                )
            ),
            unavailableRetailers = listOf(
                UnavailableRetailer(
                    retailerId = "reliance_digital",
                    retailerName = "Reliance Digital",
                    reason = "Out of stock"
                )
            ),
            lowestPriceOffer = RetailerOffer(
                retailerId = "amazon_in",
                retailerName = "Amazon India",
                price = 26990.0,
                currency = "INR",
                isAvailable = true,
                productUrl = "https://amazon.in/dp/B09XS7JWHH",
                observedAtTimestamp = 1700000000000L
            ),
            confidenceScore = 0.98,
            lastRefreshedTimestamp = 1700000000000L
        )

        override suspend fun searchProducts(query: String): Result<List<ProductSummary>> = Result.success(emptyList())
        override suspend fun getProductOffers(productId: String): Result<List<RetailerOffer>> = Result.success(emptyList())

        override suspend fun getProductComparison(productId: String): Result<ProductComparisonDetails> {
            return if (shouldFail) {
                Result.failure(failureException)
            } else {
                Result.success(sampleDetails)
            }
        }

        override suspend fun getTrendingProducts(): Result<List<ProductSummary>> = Result.success(emptyList())

        override suspend fun getProductAlternatives(productId: String): Result<List<com.pricelens.domain.model.ProductAlternative>> =
            Result.success(emptyList())
    }

    private class FakeTargetPriceAlertRepository : TargetPriceAlertRepository {
        val alerts = mutableListOf<TargetPriceAlert>()
        private val alertsFlow = MutableStateFlow<List<TargetPriceAlert>>(emptyList())

        override suspend fun save(alert: TargetPriceAlert) {
            alerts.removeAll { it.id == alert.id }
            alerts.add(alert)
            alertsFlow.value = alerts.toList()
        }

        override suspend fun getActiveAlerts(productId: String): List<TargetPriceAlert> =
            alerts.filter { it.productId == productId && it.isActive }

        override suspend fun deleteAlert(alertId: String) {
            alerts.removeAll { it.id == alertId }
            alertsFlow.value = alerts.toList()
        }

        override fun observeAlerts(productId: String): Flow<List<TargetPriceAlert>> =
            alertsFlow.asStateFlow()

        override suspend fun evaluate(
            alert: TargetPriceAlert,
            observation: PriceObservation
        ): TargetPriceAlert = alert

        override suspend fun notifications(alertId: String): List<NotificationRecord> = emptyList()
    }

    private lateinit var catalogRepo: FakeProductCatalogRepository
    private lateinit var alertRepo: FakeTargetPriceAlertRepository
    private lateinit var evaluator: AlertEvaluator
    private lateinit var getComparisonUseCase: GetProductComparisonUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        catalogRepo = FakeProductCatalogRepository()
        alertRepo = FakeTargetPriceAlertRepository()
        evaluator = AlertEvaluator()
        getComparisonUseCase = GetProductComparisonUseCase(catalogRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load with productId loads details and lowest price offer correctly`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("productId" to "prod_sony_wh1000xm5"))
        val viewModel = ProductComparisonViewModel(
            getComparisonUseCase, alertRepo, evaluator, savedStateHandle
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertNotNull(state.comparisonDetails)
        assertEquals("Sony WH-1000XM5 Wireless Headphones", state.comparisonDetails!!.name)

        // Verify lowest price offer
        assertNotNull(state.comparisonDetails!!.lowestPriceOffer)
        assertEquals(26990.0, state.comparisonDetails!!.lowestPriceOffer!!.price!!, 0.01)
        assertEquals("Amazon India", state.comparisonDetails!!.lowestPriceOffer!!.retailerName)

        // Verify unavailable retailers
        assertEquals(1, state.comparisonDetails!!.unavailableRetailers.size)
        assertEquals("Reliance Digital", state.comparisonDetails!!.unavailableRetailers[0].retailerName)
    }

    @Test
    fun `setting outside website price tracker saves alert and updates ui banner`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("productId" to "prod_sony_wh1000xm5"))
        val viewModel = ProductComparisonViewModel(
            getComparisonUseCase, alertRepo, evaluator, savedStateHandle
        )
        advanceUntilIdle()

        // Open dialog for outside website tracking
        viewModel.onEvent(
            ProductComparisonUiEvent.OpenTrackPriceDialog(
                retailerName = "Tata CLiQ",
                currentPrice = 27000.0,
                websiteUrl = "https://www.tatacliq.com/item"
            )
        )
        assertTrue(viewModel.uiState.value.isTrackingDialogVisible)
        assertEquals("Tata CLiQ", viewModel.uiState.value.trackingTargetRetailer)

        // Change target price and submit
        viewModel.onEvent(ProductComparisonUiEvent.TargetPriceChanged("24999"))
        viewModel.onEvent(ProductComparisonUiEvent.SubmitTrackPrice)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isTrackingDialogVisible)
        assertNotNull(viewModel.uiState.value.userNotificationBanner)
        assertTrue(viewModel.uiState.value.userNotificationBanner!!.contains("Tata CLiQ"))
        assertEquals(1, alertRepo.alerts.size)
        assertEquals("Tata CLiQ", alertRepo.alerts[0].retailerName)
        assertEquals(24999.0, alertRepo.alerts[0].targetPrice, 0.01)
    }

    @Test
    fun `deleting price alert removes it from repository`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("productId" to "prod_sony_wh1000xm5"))
        val viewModel = ProductComparisonViewModel(
            getComparisonUseCase, alertRepo, evaluator, savedStateHandle
        )
        advanceUntilIdle()

        viewModel.onEvent(
            ProductComparisonUiEvent.OpenTrackPriceDialog(
                retailerName = "Reliance Digital",
                currentPrice = 28000.0
            )
        )
        viewModel.onEvent(ProductComparisonUiEvent.TargetPriceChanged("25000"))
        viewModel.onEvent(ProductComparisonUiEvent.SubmitTrackPrice)
        advanceUntilIdle()
        assertEquals(1, alertRepo.alerts.size)

        val alertId = alertRepo.alerts[0].id
        viewModel.onEvent(ProductComparisonUiEvent.DeletePriceAlert(alertId))
        advanceUntilIdle()

        assertEquals(0, alertRepo.alerts.size)
    }

    @Test
    fun `provider error displays safe error message`() = runTest {
        catalogRepo.shouldFail = true
        catalogRepo.failureException = PriceLensException.ProviderUnavailableException(
            message = "503 Partner Service Down",
            safeUserMessage = "Pricing provider service is temporarily offline. Please try again."
        )

        val savedStateHandle = SavedStateHandle(mapOf("productId" to "prod_sony_wh1000xm5"))
        val viewModel = ProductComparisonViewModel(
            getComparisonUseCase, alertRepo, evaluator, savedStateHandle
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.comparisonDetails)
        assertEquals("Pricing provider service is temporarily offline. Please try again.", state.errorMessage)
    }

    @Test
    fun `uncertain match error safely surfaces without silent merging`() = runTest {
        catalogRepo.shouldFail = true
        catalogRepo.failureException = PriceLensException.UncertainMatchException(
            message = "Low confidence match: 0.65 < 0.85"
        )

        val savedStateHandle = SavedStateHandle(mapOf("productId" to "prod_uncertain"))
        val viewModel = ProductComparisonViewModel(
            getComparisonUseCase, alertRepo, evaluator, savedStateHandle
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.comparisonDetails)
        assertTrue(state.errorMessage!!.contains("confidence", ignoreCase = true) || state.errorMessage!!.contains("merged", ignoreCase = true))
    }

    @Test
    fun `retry event reloads comparison details after failure is resolved`() = runTest {
        catalogRepo.shouldFail = true
        val savedStateHandle = SavedStateHandle(mapOf("productId" to "prod_sony_wh1000xm5"))
        val viewModel = ProductComparisonViewModel(
            getComparisonUseCase, alertRepo, evaluator, savedStateHandle
        )

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.errorMessage != null)

        catalogRepo.shouldFail = false
        viewModel.onEvent(ProductComparisonUiEvent.RetryLoad)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertNotNull(state.comparisonDetails)
        assertEquals("Sony WH-1000XM5 Wireless Headphones", state.comparisonDetails!!.name)
    }

    @Test
    fun `selecting alternative switches comparison details to chosen product`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("productId" to "prod_sony_wh1000xm5"))
        val viewModel = ProductComparisonViewModel(
            getComparisonUseCase, alertRepo, evaluator, savedStateHandle
        )
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.comparisonDetails)

        viewModel.onEvent(ProductComparisonUiEvent.SelectAlternative("prod_bose_qc45", "Bose QC45"))
        advanceUntilIdle()

        assertEquals("prod_bose_qc45", viewModel.uiState.value.productId)
    }
}
