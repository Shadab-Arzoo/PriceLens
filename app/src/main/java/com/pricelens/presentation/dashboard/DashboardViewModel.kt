package com.pricelens.presentation.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pricelens.core.preferences.UserPreferencesRepository
import com.pricelens.data.notification.AndroidPriceDropNotificationSender
import com.pricelens.data.worker.PriceCheckWorker
import com.pricelens.domain.model.WatchlistItem
import com.pricelens.domain.repository.DashboardRepository
import com.pricelens.domain.repository.TargetPriceAlertRepository
import com.pricelens.domain.service.AlertEvaluator
import com.pricelens.domain.service.UniversalProductExtractor
import com.pricelens.domain.usecase.GetDashboardDataUseCase
import com.pricelens.domain.usecase.SaveRecentSearchUseCase
import com.pricelens.domain.usecase.UrlValidationResult
import com.pricelens.domain.usecase.ValidateProductUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val saveRecentSearchUseCase: SaveRecentSearchUseCase,
    private val validateProductUrlUseCase: ValidateProductUrlUseCase,
    private val dashboardRepository: DashboardRepository,
    private val targetPriceAlertRepository: TargetPriceAlertRepository? = null,
    private val alertEvaluator: AlertEvaluator = AlertEvaluator(),
    private val universalProductExtractor: UniversalProductExtractor = UniversalProductExtractor(),
    private val userPreferencesRepository: UserPreferencesRepository? = null,
    private val notificationSender: AndroidPriceDropNotificationSender? = null,
    @ApplicationContext private val appContext: Context? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            experimentalFlags = userPreferencesRepository?.getExperimentalFlags() ?: UserPreferencesRepository.ExperimentalFlags()
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private var extractJob: Job? = null

    init {
        loadDashboardData()
        observeExperimentalFlags()
    }

    private fun observeExperimentalFlags() {
        userPreferencesRepository?.let { repo ->
            viewModelScope.launch {
                repo.experimentalFlagsFlow.collectLatest { flags ->
                    _uiState.update { it.copy(experimentalFlags = flags) }
                }
            }
        }
    }

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is DashboardUiEvent.SubmitSearch -> {
                if (event.query.isNotBlank()) {
                    viewModelScope.launch {
                        saveRecentSearchUseCase(event.query)
                        _uiState.update { it.copy(searchQuery = "") }
                    }
                }
            }
            is DashboardUiEvent.RecentSearchSelected -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is DashboardUiEvent.ClearRecentSearches -> {
                viewModelScope.launch {
                    dashboardRepository.clearRecentSearches()
                }
            }
            DashboardUiEvent.OpenPasteUrlSheet -> {
                extractJob?.cancel()
                _uiState.update {
                    it.copy(
                        isPasteUrlSheetOpen = true,
                        pasteUrlInput = "",
                        urlValidation = null,
                        confirmedIdentity = null,
                        extractedMetadata = null,
                        isExtractingMetadata = false
                    )
                }
            }
            DashboardUiEvent.ClosePasteUrlSheet -> {
                extractJob?.cancel()
                _uiState.update {
                    it.copy(
                        isPasteUrlSheetOpen = false,
                        pasteUrlInput = "",
                        urlValidation = null,
                        confirmedIdentity = null,
                        extractedMetadata = null,
                        isExtractingMetadata = false
                    )
                }
            }
            is DashboardUiEvent.PasteUrlInputChanged -> {
                val validation = if (event.url.isNotBlank()) {
                    validateProductUrlUseCase(event.url)
                } else {
                    null
                }
                val suggested = (validation as? UrlValidationResult.Supported)?.suggestedIdentity
                _uiState.update {
                    it.copy(
                        pasteUrlInput = event.url,
                        urlValidation = validation,
                        confirmedIdentity = suggested,
                        extractedMetadata = null,
                        isExtractingMetadata = event.url.isNotBlank() && (event.url.contains("http") || event.url.contains("."))
                    )
                }

                extractJob?.cancel()
                if (event.url.isNotBlank() && (event.url.contains("http") || event.url.contains("."))) {
                    extractJob = viewModelScope.launch {
                        delay(300)
                        val meta = universalProductExtractor.extractMetadata(event.url)
                        _uiState.update {
                            it.copy(
                                extractedMetadata = meta,
                                isExtractingMetadata = false
                            )
                        }
                    }
                }
            }
            is DashboardUiEvent.ConfirmedIdentityChanged -> {
                _uiState.update { current ->
                    current.copy(
                        confirmedIdentity = current.confirmedIdentity?.withCorrections(event.name, event.brand, event.model)
                    )
                }
            }
            is DashboardUiEvent.ConfirmProductAndNavigate -> {
                _uiState.update {
                    it.copy(
                        isPasteUrlSheetOpen = false,
                        confirmedIdentity = event.identity.confirm()
                    )
                }
            }
            is DashboardUiEvent.TrackUniversalProduct -> {
                viewModelScope.launch {
                    val productId = "web_" + UUID.randomUUID().toString().take(8)
                    if (targetPriceAlertRepository != null) {
                        val alert = alertEvaluator.createAlert(
                            productId = productId,
                            targetPrice = event.targetPrice,
                            currency = event.metadata.currency,
                            retailerName = event.metadata.retailerName,
                            websiteUrl = event.metadata.url,
                            priceSelector = event.metadata.detectedSelector
                        )
                        targetPriceAlertRepository.save(alert)
                    }

                    val watchlistItem = WatchlistItem(
                        productId = productId,
                        productName = event.metadata.title,
                        brand = event.metadata.retailerName,
                        targetPrice = event.targetPrice,
                        currentLowestPrice = event.metadata.price,
                        currency = event.metadata.currency,
                        addedAtTimestamp = System.currentTimeMillis(),
                        websiteUrl = event.metadata.url,
                        imageUrl = event.metadata.imageUrl
                    )
                    dashboardRepository.addToWatchlist(watchlistItem)

                    _uiState.update {
                        it.copy(
                            isPasteUrlSheetOpen = false,
                            pasteUrlInput = "",
                            extractedMetadata = null,
                            isExtractingMetadata = false
                        )
                    }
                }
            }
            DashboardUiEvent.SubmitPastedUrl -> {
                val currentInput = _uiState.value.pasteUrlInput
                val validation = validateProductUrlUseCase(currentInput)
                val suggested = (validation as? UrlValidationResult.Supported)?.suggestedIdentity
                _uiState.update {
                    it.copy(
                        urlValidation = validation,
                        confirmedIdentity = it.confirmedIdentity ?: suggested
                    )
                }
            }
            DashboardUiEvent.OpenLabsSheet -> {
                _uiState.update { it.copy(isLabsSheetOpen = true) }
            }
            DashboardUiEvent.CloseLabsSheet -> {
                _uiState.update { it.copy(isLabsSheetOpen = false) }
            }
            is DashboardUiEvent.ToggleLabsMaster -> {
                userPreferencesRepository?.setExperimentalMasterEnabled(event.enabled)
            }
            is DashboardUiEvent.ToggleLabsFlag -> {
                userPreferencesRepository?.setExperimentalFlag(event.key, event.enabled)
            }
            DashboardUiEvent.TriggerPriceSync -> {
                appContext?.let { ctx ->
                    val workRequest = OneTimeWorkRequestBuilder<PriceCheckWorker>().build()
                    WorkManager.getInstance(ctx).enqueue(workRequest)
                }
            }
            DashboardUiEvent.TriggerTestNotification -> {
                viewModelScope.launch {
                    notificationSender?.sendSampleAlert()
                }
            }
            is DashboardUiEvent.ImportWatchlist -> {
                viewModelScope.launch {
                    importWatchlistFromJson(event.json)
                }
            }
            DashboardUiEvent.RetryLoad -> {
                loadDashboardData()
            }
        }
    }

    private suspend fun importWatchlistFromJson(json: String) {
        if (json.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Watchlist import data is empty.") }
            return
        }
        try {
            val gson = Gson()
            val type = object : TypeToken<List<WatchlistItem>>() {}.type
            val items: List<WatchlistItem>? = gson.fromJson(json, type)
            if (items == null) {
                _uiState.update { it.copy(errorMessage = "Invalid watchlist JSON format.") }
                return
            }
            for (item in items) {
                dashboardRepository.addToWatchlist(item)
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(errorMessage = "Failed to import watchlist: ${e.message ?: "Invalid JSON"}")
            }
        }
    }

    private fun loadDashboardData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            getDashboardDataUseCase()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to load dashboard data."
                        )
                    }
                }
                .collect { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recentSearches = data.recentSearches,
                            watchlistItems = data.watchlistItems,
                            trendingProducts = data.trendingProducts,
                            errorMessage = null
                        )
                    }
                }
        }
    }
}
