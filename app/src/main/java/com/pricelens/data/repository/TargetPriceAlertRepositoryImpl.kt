package com.pricelens.data.repository

import com.pricelens.data.local.dao.TargetPriceAlertDao
import com.pricelens.data.local.entity.NotificationRecordEntity
import com.pricelens.data.local.entity.TargetPriceAlertEntity
import com.pricelens.domain.model.NotificationRecord
import com.pricelens.domain.model.PriceObservation
import com.pricelens.domain.model.TargetPriceAlert
import com.pricelens.domain.repository.TargetPriceAlertRepository
import com.pricelens.domain.service.AlertEvaluator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface PriceAlertNotificationSender {
    suspend fun send(
        record: NotificationRecord,
        alert: TargetPriceAlert,
        observation: PriceObservation
    )
}

class TargetPriceAlertRepositoryImpl @Inject constructor(
    private val dao: TargetPriceAlertDao,
    private val notificationSender: PriceAlertNotificationSender,
    private val evaluator: AlertEvaluator
) : TargetPriceAlertRepository {
    override suspend fun save(alert: TargetPriceAlert) {
        dao.upsertAlert(TargetPriceAlertEntity.fromDomain(alert))
    }

    override suspend fun getActiveAlerts(productId: String): List<TargetPriceAlert> =
        dao.getActiveAlerts(productId).map { it.toDomain() }

    override suspend fun deleteAlert(alertId: String) {
        dao.deleteAlert(alertId)
    }

    override fun observeAlerts(productId: String): Flow<List<TargetPriceAlert>> =
        dao.observeAlerts(productId).map { list -> list.map { it.toDomain() } }

    override suspend fun evaluate(
        alert: TargetPriceAlert,
        observation: PriceObservation
    ): TargetPriceAlert {
        val existing = notifications(alert.id)
        val evaluation = evaluator.evaluate(alert, observation, existing)
        dao.upsertAlert(TargetPriceAlertEntity.fromDomain(evaluation.alert))
        evaluation.notifications
            .drop(existing.size)
            .forEach { record ->
                dao.insertNotification(NotificationRecordEntity.fromDomain(record))
                runCatching {
                    notificationSender.send(record, evaluation.alert, observation)
                }
            }
        return evaluation.alert
    }

    override suspend fun notifications(alertId: String): List<NotificationRecord> =
        dao.getNotifications(alertId).map { it.toDomain() }
}
