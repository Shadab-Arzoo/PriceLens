package com.pricelens.domain.model

data class PriceStatistics(
    val current: Double? = null,
    val low: Double? = null,
    val high: Double? = null,
    val average: Double? = null,
    val isUnavailable: Boolean = true
)
