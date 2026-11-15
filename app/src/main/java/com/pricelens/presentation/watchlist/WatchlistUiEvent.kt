package com.pricelens.presentation.watchlist

sealed interface WatchlistUiEvent {
    data class RemoveItem(val productId: String) : WatchlistUiEvent
    data class SearchQueryChanged(val query: String) : WatchlistUiEvent
    data object ClearNotificationMessage : WatchlistUiEvent
}
