package com.pricelens.presentation.watchlist

sealed interface WatchlistUiEvent {
    data class RemoveItem(val productId: String) : WatchlistUiEvent
    data class SearchQueryChanged(val query: String) : WatchlistUiEvent
    data class FilterChanged(val filter: WatchlistFilter) : WatchlistUiEvent
    data object ClearNotificationMessage : WatchlistUiEvent
}
