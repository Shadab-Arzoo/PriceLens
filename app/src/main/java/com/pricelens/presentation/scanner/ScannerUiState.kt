package com.pricelens.presentation.scanner

import com.pricelens.domain.model.ConfirmedProductIdentity

/**
 * UI state for the Camera / Image OCR scanner screen.
 */
data class ScannerUiState(
    val hasCameraPermission: Boolean = false,
    val isPermissionDenied: Boolean = false,
    val isProcessingOcr: Boolean = false,
    val selectedImageUri: String? = null,
    val rawOcrText: String? = null,
    val ocrSuggestion: ConfirmedProductIdentity? = null,
    val isConfirmed: Boolean = false,
    val errorMessage: String? = null
)
