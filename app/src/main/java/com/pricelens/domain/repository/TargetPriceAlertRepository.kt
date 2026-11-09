package com.pricelens.domain.repository

import com.pricelens.domain.model.NotificationRecord
import com.pricelens.domain.model.PriceObservation
import com.pricelens.domain.model.TargetPriceAlert
import kotlinx.coroutines.flow.Flow

interface TargetPriceAlertRepository {
    suspend fun save(alert: TargetPriceAlert)
    suspend fun getActiveAlerts(productId: String): List<TargetPriceAlert>
    suspend fun deleteAlert(alertId: String)
    fun observeAlerts(productId: String): Flow<List<TargetPriceAlert>>
    suspend fun evaluate(alert: TargetPriceAlert, observation: PriceObservation): TargetPriceAlert
    suspend fun notifications(alertId: String): List<NotificationRecord>
}
