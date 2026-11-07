package com.pricelens.presentation.comparison

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricelens.domain.model.PriceLensException
import com.pricelens.domain.usecase.GetProductComparisonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductComparisonViewModel @Inject constructor(
    private val getProductComparisonUseCase: GetProductComparisonUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: String = savedStateHandle.get<String>("productId").orEmpty()
    private val productName: String = savedStateHandle.get<String>("productName").orEmpty()

    private val _uiState = MutableStateFlow(
        ProductComparisonUiState(
            productId = productId,
            productName = productName
        )
    )
    val uiState: StateFlow<ProductComparisonUiState> = _uiState.asStateFlow()

    init {
        if (productId.isNotBlank()) {
            loadComparisonDetails(productId)
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
        }
    }

    fun loadForProduct(id: String, name: String = "") {
        _uiState.update { it.copy(productId = id, productName = name) }
        loadComparisonDetails(id)
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
}
