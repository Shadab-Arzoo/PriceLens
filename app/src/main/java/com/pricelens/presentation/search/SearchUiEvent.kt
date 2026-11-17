package com.pricelens.presentation.search

sealed interface SearchUiEvent {
    data class QueryChanged(val query: String) : SearchUiEvent
    data class SubmitSearch(val query: String) : SearchUiEvent
    data class SortOrderChanged(val sortOrder: SearchSortOrder) : SearchUiEvent
    data class StoreFilterChanged(val store: String) : SearchUiEvent
    data class PriceRangeChanged(val range: SearchPriceRange) : SearchUiEvent
    data object RetrySearch : SearchUiEvent
    data object ClearQuery : SearchUiEvent
}
