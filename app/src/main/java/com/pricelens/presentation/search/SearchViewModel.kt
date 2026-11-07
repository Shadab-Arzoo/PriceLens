package com.pricelens.presentation.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricelens.domain.model.PriceLensException
import com.pricelens.domain.usecase.SearchProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchProductsUseCase: SearchProductsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialQuery: String = savedStateHandle.get<String>("q").orEmpty()

    private val _uiState = MutableStateFlow(SearchUiState(query = initialQuery))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        if (initialQuery.isNotBlank()) {
            performSearch(initialQuery)
        }
    }

    fun onEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.QueryChanged -> {
                _uiState.update { it.copy(query = event.query) }
            }
            is SearchUiEvent.SubmitSearch -> {
                performSearch(event.query)
            }
            SearchUiEvent.RetrySearch -> {
                performSearch(_uiState.value.query)
            }
            SearchUiEvent.ClearQuery -> {
                _uiState.update {
                    it.copy(
                        query = "",
                        products = emptyList(),
                        errorMessage = null,
                        isSubmitted = false
                    )
                }
            }
        }
    }

    private fun performSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        _uiState.update {
            it.copy(
                query = trimmed,
                isLoading = true,
                errorMessage = null,
                isSubmitted = true
            )
        }

        viewModelScope.launch {
            val result = searchProductsUseCase(trimmed)
            result.fold(
                onSuccess = { products ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            products = products,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    val safeMessage = when (error) {
                        is PriceLensException -> error.safeUserMessage
                        else -> error.message ?: "An unexpected error occurred while searching. Please try again."
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            products = emptyList(),
                            errorMessage = safeMessage
                        )
                    }
                }
            )
        }
    }
}
