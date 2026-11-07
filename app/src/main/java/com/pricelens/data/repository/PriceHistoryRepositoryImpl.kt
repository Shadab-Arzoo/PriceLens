package com.pricelens.data.repository

import com.pricelens.data.local.dao.PriceHistoryDao
import com.pricelens.data.local.entity.PriceObservationEntity
import com.pricelens.domain.model.PriceObservation
import com.pricelens.domain.repository.PriceHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PriceHistoryRepositoryImpl @Inject constructor(
    private val dao: PriceHistoryDao
) : PriceHistoryRepository {
    override suspend fun record(observation: PriceObservation) {
        dao.insertObservation(PriceObservationEntity.fromDomain(observation))
    }

    override fun observe(productId: String): Flow<List<PriceObservation>> =
        dao.observeHistory(productId).map { observations -> observations.map { it.toDomain() } }
}
