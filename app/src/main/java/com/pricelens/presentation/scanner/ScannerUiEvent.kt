package com.pricelens.presentation.scanner

import com.pricelens.domain.model.ConfirmedProductIdentity

/**
 * UI events for the Camera / Image OCR scanner screen.
 */
sealed interface ScannerUiEvent {
    data object CameraPermissionGranted : ScannerUiEvent
    data object CameraPermissionDenied : ScannerUiEvent
    data class ImageSelected(val uriString: String) : ScannerUiEvent
    data class ProcessOcrText(val text: String) : ScannerUiEvent
    data class OcrSuggestionEdited(val name: String, val brand: String?, val model: String?) : ScannerUiEvent
    data class ConfirmProduct(val identity: ConfirmedProductIdentity) : ScannerUiEvent
    data object ResetScan : ScannerUiEvent
}
