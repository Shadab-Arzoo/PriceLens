package com.pricelens.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricelens.domain.repository.DashboardRepository
import com.pricelens.domain.usecase.GetDashboardDataUseCase
import com.pricelens.domain.usecase.SaveRecentSearchUseCase
import com.pricelens.domain.usecase.UrlValidationResult
import com.pricelens.domain.usecase.ValidateProductUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val saveRecentSearchUseCase: SaveRecentSearchUseCase,
    private val validateProductUrlUseCase: ValidateProductUrlUseCase,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
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
                _uiState.update {
                    it.copy(
                        isPasteUrlSheetOpen = true,
                        pasteUrlInput = "",
                        urlValidation = null
                    )
                }
            }
            DashboardUiEvent.ClosePasteUrlSheet -> {
                _uiState.update {
                    it.copy(
                        isPasteUrlSheetOpen = false,
                        pasteUrlInput = "",
                        urlValidation = null
                    )
                }
            }
            is DashboardUiEvent.PasteUrlInputChanged -> {
                val validation = if (event.url.isNotBlank()) {
                    validateProductUrlUseCase(event.url)
                } else {
                    null
                }
                _uiState.update {
                    it.copy(
                        pasteUrlInput = event.url,
                        urlValidation = validation
                    )
                }
            }
            DashboardUiEvent.SubmitPastedUrl -> {
                val currentInput = _uiState.value.pasteUrlInput
                val validation = validateProductUrlUseCase(currentInput)
                _uiState.update { it.copy(urlValidation = validation) }
            }
            DashboardUiEvent.RetryLoad -> {
                loadDashboardData()
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
