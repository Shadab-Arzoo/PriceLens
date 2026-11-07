package com.pricelens.domain.repository

import com.pricelens.domain.model.NotificationRecord
import com.pricelens.domain.model.PriceObservation
import com.pricelens.domain.model.TargetPriceAlert

interface TargetPriceAlertRepository {
    suspend fun save(alert: TargetPriceAlert)
    suspend fun evaluate(alert: TargetPriceAlert, observation: PriceObservation): TargetPriceAlert
    suspend fun notifications(alertId: String): List<NotificationRecord>
}
