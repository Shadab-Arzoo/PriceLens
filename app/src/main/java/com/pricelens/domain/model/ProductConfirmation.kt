package com.pricelens.domain.model

/**
 * Discovery input source for product identification.
 */
enum class DiscoverySource {
    PASTED_URL,
    CAMERA_OCR,
    IMAGE_GALLERY,
    BARCODE
}

/**
 * Model representing an identified product candidate that requires explicit
 * user confirmation (and allows user editing/correction) before comparing retailer offers.
 */
data class ConfirmedProductIdentity(
    val source: DiscoverySource,
    val rawInput: String,
    val suggestedName: String,
    val suggestedBrand: String? = null,
    val suggestedModel: String? = null,
    val resolvedProductId: String? = null,
    val confidenceScore: Double = 1.0,
    val isUserConfirmed: Boolean = false,
    val editedName: String = suggestedName,
    val editedBrand: String? = suggestedBrand,
    val editedModel: String? = suggestedModel
) {
    /**
     * The final confirmed title to use for comparison query or lookup.
     */
    val effectiveTitle: String
        get() = editedName.ifBlank { suggestedName }

    /**
     * Returns a copy with user corrections applied.
     */
    fun withCorrections(name: String, brand: String?, model: String?): ConfirmedProductIdentity {
        return copy(
            editedName = name.trim(),
            editedBrand = brand?.trim()?.ifBlank { null },
            editedModel = model?.trim()?.ifBlank { null }
        )
    }

    /**
     * Marks the identity as explicitly confirmed by the user.
     */
    fun confirm(): ConfirmedProductIdentity {
        return copy(isUserConfirmed = true)
    }
}
