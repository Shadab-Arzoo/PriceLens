package com.pricelens.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class RecentSearch(
    val query: String,
    val timestamp: Long
)
