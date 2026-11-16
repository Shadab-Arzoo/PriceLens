package com.pricelens.presentation.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricelens.domain.repository.DashboardRepository
import com.pricelens.domain.repository.TargetPriceAlertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val targetPriceAlertRepository: TargetPriceAlertRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState(isLoading = true))
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    init {
        loadWatchlist()
    }

    private fun loadWatchlist() {
        viewModelScope.launch {
            dashboardRepository.getWatchlistItems().collectLatest { items ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = items
                    )
                }
            }
        }
    }

    fun onEvent(event: WatchlistUiEvent) {
        when (event) {
            is WatchlistUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is WatchlistUiEvent.FilterChanged -> {
                _uiState.update { it.copy(activeFilter = event.filter) }
            }
            is WatchlistUiEvent.SortOrderChanged -> {
                _uiState.update { it.copy(sortOrder = event.sortOrder) }
            }
            is WatchlistUiEvent.RemoveItem -> {
                viewModelScope.launch {
                    dashboardRepository.removeFromWatchlist(event.productId)
                    val activeAlerts = targetPriceAlertRepository.getActiveAlerts(event.productId)
                    activeAlerts.forEach { targetPriceAlertRepository.deleteAlert(it.id) }
                    _uiState.update { it.copy(notificationMessage = "Removed product from watchlist") }
                }
            }
            is WatchlistUiEvent.ClearNotificationMessage -> {
                _uiState.update { it.copy(notificationMessage = null) }
            }
        }
    }
}
