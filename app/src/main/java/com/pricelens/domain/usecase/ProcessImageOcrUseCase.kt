package com.pricelens.domain.usecase

import com.pricelens.domain.model.ConfirmedProductIdentity
import com.pricelens.domain.model.DiscoverySource
import com.pricelens.domain.service.ProductOcrScanner
import javax.inject.Inject

/**
 * Use case to process image / camera OCR text and produce editable product identification suggestions
 * that require explicit user confirmation before retailer offers are shown.
 */
class ProcessImageOcrUseCase @Inject constructor(
    private val ocrScanner: ProductOcrScanner
) {
    operator fun invoke(
        rawText: String,
        source: DiscoverySource = DiscoverySource.CAMERA_OCR
    ): Result<ConfirmedProductIdentity> {
        val trimmed = rawText.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("OCR text input cannot be empty"))
        }

        val identity = ocrScanner.scanText(trimmed, source)
            ?: return Result.failure(IllegalStateException("No recognizable product identity found in image/text"))

        return Result.success(identity)
    }
}
