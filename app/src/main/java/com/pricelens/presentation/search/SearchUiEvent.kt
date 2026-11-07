package com.pricelens.presentation.search

sealed interface SearchUiEvent {
    data class QueryChanged(val query: String) : SearchUiEvent
    data class SubmitSearch(val query: String) : SearchUiEvent
    data object RetrySearch : SearchUiEvent
    data object ClearQuery : SearchUiEvent
}
