package com.pricelens.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class PriceStatistics(
    val current: Double? = null,
    val low: Double? = null,
    val high: Double? = null,
    val average: Double? = null,
    val isUnavailable: Boolean = true
)
