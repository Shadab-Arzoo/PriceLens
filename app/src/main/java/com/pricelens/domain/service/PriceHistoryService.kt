package com.pricelens.domain.service

import com.pricelens.domain.model.PriceObservation
import com.pricelens.domain.model.PriceStatistics

class PriceHistoryService {
    fun statistics(observations: List<PriceObservation>): PriceStatistics {
        if (observations.size < MINIMUM_HISTORY_POINTS) return PriceStatistics()

        val ordered = observations.sortedBy { it.observedAt }
        val prices = ordered.map { it.price }
        return PriceStatistics(
            current = ordered.last().price,
            low = prices.minOrNull(),
            high = prices.maxOrNull(),
            average = prices.average(),
            isUnavailable = false
        )
    }

    private companion object {
        const val MINIMUM_HISTORY_POINTS = 2
    }
}
