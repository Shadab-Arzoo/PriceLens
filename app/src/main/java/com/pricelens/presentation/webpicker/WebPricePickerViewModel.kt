package com.pricelens.presentation.webpicker

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricelens.domain.model.TargetPriceAlert
import com.pricelens.domain.model.WatchlistItem
import com.pricelens.domain.repository.DashboardRepository
import com.pricelens.domain.repository.TargetPriceAlertRepository
import com.pricelens.domain.service.AlertEvaluator
import com.pricelens.domain.service.UniversalProductExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject

data class WebPricePickerUiState(
    val url: String = "",
    val domain: String = "",
    val retailerName: String = "",
    val pageTitle: String = "",
    val isInspectMode: Boolean = true, // Default to inspector mode so user can tap right away
    val isLoadingPage: Boolean = true,
    val pageLoadProgress: Int = 0,
    val selectedRawText: String? = null,
    val selectedPrice: Double? = null,
    val selectedSelector: String? = null,
    val targetPriceInput: String = "",
    val productNameInput: String = "",
    val isSaving: Boolean = false,
    val isSavedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class WebPricePickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dashboardRepository: DashboardRepository,
    private val targetPriceAlertRepository: TargetPriceAlertRepository,
    private val alertEvaluator: AlertEvaluator,
    private val extractor: UniversalProductExtractor
) : ViewModel() {

    private val initialUrl: String = savedStateHandle.get<String>("url")?.let {
        runCatching { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }.getOrDefault(it)
    }.orEmpty()

    private val initialName: String = savedStateHandle.get<String>("name")?.let {
        runCatching { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }.getOrDefault(it)
    }.orEmpty()

    private val _uiState = MutableStateFlow(
        WebPricePickerUiState(
            url = extractor.normalizeUrl(initialUrl),
            domain = extractor.extractDomain(initialUrl),
            retailerName = extractor.formatRetailerName(extractor.extractDomain(initialUrl)),
            pageTitle = initialName,
            productNameInput = initialName
        )
    )
    val uiState: StateFlow<WebPricePickerUiState> = _uiState.asStateFlow()

    fun toggleInspectMode(enabled: Boolean) {
        _uiState.update { it.copy(isInspectMode = enabled) }
    }

    fun onPageLoadProgress(progress: Int) {
        _uiState.update {
            it.copy(
                pageLoadProgress = progress,
                isLoadingPage = progress < 100
            )
        }
    }

    fun onPageLoaded(title: String, url: String) {
        val domain = extractor.extractDomain(url)
        val retailer = extractor.formatRetailerName(domain)
        _uiState.update { current ->
            current.copy(
                isLoadingPage = false,
                pageLoadProgress = 100,
                pageTitle = title.ifBlank { current.pageTitle },
                productNameInput = if (current.productNameInput.isBlank()) title else current.productNameInput,
                url = url,
                domain = domain,
                retailerName = retailer
            )
        }
    }

    fun onElementSelected(rawText: String, selector: String, pageTitle: String, currentUrl: String) {
        val parsed = extractor.parsePrice(rawText)
        _uiState.update { current ->
            val suggestedTarget = if (parsed != null && parsed > 0) {
                // Default target price suggestion: 5% below current price rounded
                val discounted = parsed * 0.95
                "%.0f".format(discounted)
            } else {
                current.targetPriceInput
            }

            current.copy(
                selectedRawText = rawText.take(120),
                selectedPrice = parsed,
                selectedSelector = selector,
                targetPriceInput = suggestedTarget,
                productNameInput = if (current.productNameInput.isBlank()) pageTitle else current.productNameInput,
                url = currentUrl.ifBlank { current.url },
                errorMessage = null
            )
        }
    }

    fun onTargetPriceChanged(targetPrice: String) {
        _uiState.update { it.copy(targetPriceInput = targetPrice) }
    }

    fun onProductNameChanged(name: String) {
        _uiState.update { it.copy(productNameInput = name) }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedRawText = null,
                selectedPrice = null,
                selectedSelector = null,
                targetPriceInput = ""
            )
        }
    }

    fun confirmTrackPrice() {
        val state = _uiState.value
        val targetDouble = state.targetPriceInput.toDoubleOrNull()
        if (targetDouble == null || targetDouble <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid target alert price greater than 0.") }
            return
        }

        val name = state.productNameInput.ifBlank { state.pageTitle }.ifBlank { "Tracked Product" }
        val productId = "web_" + UUID.randomUUID().toString().take(8)

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                // 1. Create and save TargetPriceAlert
                val alert = alertEvaluator.createAlert(
                    productId = productId,
                    targetPrice = targetDouble,
                    currency = "INR",
                    retailerName = state.retailerName,
                    websiteUrl = state.url,
                    priceSelector = state.selectedSelector
                )
                targetPriceAlertRepository.save(alert)

                // 2. Add to Watchlist
                val watchlistItem = WatchlistItem(
                    productId = productId,
                    productName = name,
                    brand = state.retailerName,
                    targetPrice = targetDouble,
                    currentLowestPrice = state.selectedPrice,
                    currency = "INR",
                    addedAtTimestamp = System.currentTimeMillis(),
                    websiteUrl = state.url
                )
                dashboardRepository.addToWatchlist(watchlistItem)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSavedSuccessfully = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Failed to save tracked product."
                    )
                }
            }
        }
    }
}
