package com.pricelens.presentation.comparison

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricelens.domain.model.PriceLensException
import com.pricelens.domain.model.PriceObservation
import com.pricelens.domain.model.ProductComparisonDetails
import com.pricelens.domain.model.TargetPriceAlert
import com.pricelens.domain.repository.TargetPriceAlertRepository
import com.pricelens.domain.service.AlertEvaluator
import com.pricelens.domain.usecase.GetProductComparisonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductComparisonViewModel @Inject constructor(
    private val getProductComparisonUseCase: GetProductComparisonUseCase,
    private val targetPriceAlertRepository: TargetPriceAlertRepository,
    private val alertEvaluator: AlertEvaluator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialProductId: String = savedStateHandle.get<String>("productId").orEmpty()
    private val initialProductName: String = savedStateHandle.get<String>("productName").orEmpty()

    private val _uiState = MutableStateFlow(
        ProductComparisonUiState(
            productId = initialProductId,
            productName = initialProductName
        )
    )
    val uiState: StateFlow<ProductComparisonUiState> = _uiState.asStateFlow()

    private var alertsJob: Job? = null

    init {
        if (initialProductId.isNotBlank()) {
            loadComparisonDetails(initialProductId)
            observeAlerts(initialProductId)
        }
    }

    fun onEvent(event: ProductComparisonUiEvent) {
        when (event) {
            ProductComparisonUiEvent.RetryLoad,
            ProductComparisonUiEvent.RefreshOffers -> {
                loadComparisonDetails(_uiState.value.productId)
            }
            is ProductComparisonUiEvent.SelectAlternative -> {
                loadForProduct(event.productId, event.productName)
            }
            is ProductComparisonUiEvent.OpenTrackPriceDialog -> {
                val defaultPrice = event.currentPrice?.let { "%.0f".format(it) } ?: ""
                _uiState.update {
                    it.copy(
                        isTrackingDialogVisible = true,
                        trackingTargetRetailer = event.retailerName,
                        trackingTargetPrice = defaultPrice,
                        trackingWebsiteUrl = event.websiteUrl.orEmpty()
                    )
                }
            }
            ProductComparisonUiEvent.DismissTrackPriceDialog -> {
                _uiState.update {
                    it.copy(
                        isTrackingDialogVisible = false,
                        trackingTargetRetailer = null,
                        trackingTargetPrice = "",
                        trackingWebsiteUrl = ""
                    )
                }
            }
            is ProductComparisonUiEvent.TargetPriceChanged -> {
                _uiState.update { it.copy(trackingTargetPrice = event.targetPrice) }
            }
            is ProductComparisonUiEvent.TargetRetailerChanged -> {
                _uiState.update { it.copy(trackingTargetRetailer = event.retailerName) }
            }
            is ProductComparisonUiEvent.TargetWebsiteUrlChanged -> {
                _uiState.update { it.copy(trackingWebsiteUrl = event.url) }
            }
            ProductComparisonUiEvent.SubmitTrackPrice -> {
                submitTrackPrice()
            }
            is ProductComparisonUiEvent.DeletePriceAlert -> {
                deleteAlert(event.alertId)
            }
            ProductComparisonUiEvent.DismissBanner -> {
                _uiState.update { it.copy(userNotificationBanner = null) }
            }
        }
    }

    fun loadForProduct(id: String, name: String = "") {
        _uiState.update { it.copy(productId = id, productName = name) }
        loadComparisonDetails(id)
        observeAlerts(id)
    }

    private fun observeAlerts(id: String) {
        alertsJob?.cancel()
        alertsJob = viewModelScope.launch {
            targetPriceAlertRepository.observeAlerts(id).collectLatest { alerts ->
                _uiState.update { it.copy(activeAlerts = alerts) }
            }
        }
    }

    private fun submitTrackPrice() {
        val state = _uiState.value
        val targetPriceDouble = state.trackingTargetPrice.toDoubleOrNull()
        if (targetPriceDouble == null || targetPriceDouble <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid target price greater than 0") }
            return
        }

        val retailer = state.trackingTargetRetailer?.trim()?.ifBlank { null }
        val websiteUrl = state.trackingWebsiteUrl.trim().ifBlank { null }

        viewModelScope.launch {
            val alert = alertEvaluator.createAlert(
                productId = state.productId,
                targetPrice = targetPriceDouble,
                currency = state.comparisonDetails?.lowestPriceOffer?.currency ?: "INR",
                retailerName = retailer,
                websiteUrl = websiteUrl
            )
            targetPriceAlertRepository.save(alert)

            val retailerName = retailer ?: "Any Retailer / Outside Website"
            _uiState.update {
                it.copy(
                    isTrackingDialogVisible = false,
                    trackingTargetRetailer = null,
                    trackingTargetPrice = "",
                    trackingWebsiteUrl = "",
                    userNotificationBanner = "Tracking price on $retailerName at or below ₹${"%,.0f".format(targetPriceDouble)}"
                )
            }

            // Immediately evaluate against current live offers
            state.comparisonDetails?.let { details ->
                evaluateOffers(details, listOf(alert))
            }
        }
    }

    private fun deleteAlert(alertId: String) {
        viewModelScope.launch {
            targetPriceAlertRepository.deleteAlert(alertId)
            _uiState.update { it.copy(userNotificationBanner = "Price drop tracker removed.") }
        }
    }

    private fun loadComparisonDetails(id: String) {
        if (id.isBlank()) return

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = getProductComparisonUseCase(id)
            result.fold(
                onSuccess = { details ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            comparisonDetails = details,
                            productName = details.name,
                            errorMessage = null
                        )
                    }
                    val activeAlerts = targetPriceAlertRepository.getActiveAlerts(id)
                    evaluateOffers(details, activeAlerts)
                },
                onFailure = { error ->
                    val safeMessage = when (error) {
                        is PriceLensException -> error.safeUserMessage
                        else -> error.message ?: "Failed to load product price comparison. Please try again."
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = safeMessage
                        )
                    }
                }
            )
        }
    }

    private suspend fun evaluateOffers(
        details: ProductComparisonDetails,
        alerts: List<TargetPriceAlert>
    ) {
        for (alert in alerts) {
            for (offer in details.offers) {
                val offerPrice = offer.price
                if (offer.isAvailable && offerPrice != null) {
                    val obs = PriceObservation(
                        id = "obs_${offer.retailerName.lowercase().replace(" ", "_")}_${offerPrice.toLong()}",
                        productId = details.id,
                        retailerId = offer.retailerId,
                        retailerName = offer.retailerName,
                        price = offerPrice,
                        currency = offer.currency,
                        observedAt = System.currentTimeMillis()
                    )
                    targetPriceAlertRepository.evaluate(alert, obs)
                }
            }
        }
    }
}
