package com.pricelens.domain.repository

import com.pricelens.domain.model.PriceObservation
import kotlinx.coroutines.flow.Flow

interface PriceHistoryRepository {
    suspend fun record(observation: PriceObservation)
    fun observe(productId: String): Flow<List<PriceObservation>>
}
