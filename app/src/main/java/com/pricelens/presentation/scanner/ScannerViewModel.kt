package com.pricelens.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricelens.domain.model.DiscoverySource
import com.pricelens.domain.usecase.ProcessImageOcrUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val processImageOcrUseCase: ProcessImageOcrUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun onEvent(event: ScannerUiEvent) {
        when (event) {
            ScannerUiEvent.CameraPermissionGranted -> {
                _uiState.update {
                    it.copy(
                        hasCameraPermission = true,
                        isPermissionDenied = false,
                        errorMessage = null
                    )
                }
            }
            ScannerUiEvent.CameraPermissionDenied -> {
                _uiState.update {
                    it.copy(
                        hasCameraPermission = false,
                        isPermissionDenied = true
                    )
                }
            }
            is ScannerUiEvent.ImageSelected -> {
                _uiState.update {
                    it.copy(
                        selectedImageUri = event.uriString,
                        isProcessingOcr = true,
                        errorMessage = null
                    )
                }
                // Simulate extracting OCR text from chosen image (e.g., from filename/metadata or image stream)
                val inferredText = inferTextFromUri(event.uriString)
                processOcr(inferredText, DiscoverySource.IMAGE_GALLERY)
            }
            is ScannerUiEvent.ProcessOcrText -> {
                _uiState.update {
                    it.copy(
                        isProcessingOcr = true,
                        rawOcrText = event.text,
                        errorMessage = null
                    )
                }
                processOcr(event.text, DiscoverySource.CAMERA_OCR)
            }
            is ScannerUiEvent.OcrSuggestionEdited -> {
                _uiState.update { state ->
                    val updated = state.ocrSuggestion?.withCorrections(event.name, event.brand, event.model)
                    state.copy(ocrSuggestion = updated)
                }
            }
            is ScannerUiEvent.ConfirmProduct -> {
                _uiState.update {
                    it.copy(
                        isConfirmed = true,
                        ocrSuggestion = event.identity.confirm()
                    )
                }
            }
            ScannerUiEvent.ResetScan -> {
                _uiState.update {
                    it.copy(
                        selectedImageUri = null,
                        rawOcrText = null,
                        ocrSuggestion = null,
                        isConfirmed = false,
                        errorMessage = null,
                        isProcessingOcr = false
                    )
                }
            }
        }
    }

    private fun processOcr(rawText: String, source: DiscoverySource) {
        viewModelScope.launch {
            val result = processImageOcrUseCase(rawText, source)
            result.fold(
                onSuccess = { identity ->
                    _uiState.update {
                        it.copy(
                            isProcessingOcr = false,
                            ocrSuggestion = identity,
                            rawOcrText = rawText,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isProcessingOcr = false,
                            ocrSuggestion = null,
                            errorMessage = error.message ?: "OCR identification could not recognize a product."
                        )
                    }
                }
            )
        }
    }

    private fun inferTextFromUri(uriString: String): String {
        val lower = uriString.lowercase()
        return when {
            lower.contains("sony") || lower.contains("1000xm5") -> "Sony WH-1000XM5 Wireless Noise Cancelling Headphones Model WH-1000XM5/B"
            lower.contains("iphone") || lower.contains("apple") -> "Apple iPhone 15 128 GB Model A3090 MTP03HN/A"
            lower.contains("samsung") || lower.contains("s24") -> "Samsung Galaxy S24 5G Model SM-S921BZKDINS"
            lower.contains("oneplus") || lower.contains("op12") -> "OnePlus 12 5G Model CPH2581"
            lower.contains("macbook") || lower.contains("m2") -> "Apple MacBook Air M2 Model MLY33HN/A"
            lower.contains("bose") || lower.contains("qc45") -> "Bose QuietComfort 45 Model 866724-0100"
            lower.contains("dell") || lower.contains("xps") -> "Dell XPS 13 9315 Model XPS9315"
            else -> uriString.substringAfterLast("/").substringBeforeLast(".")
        }
    }
}
