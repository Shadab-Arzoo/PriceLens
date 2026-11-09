package com.pricelens.domain.service

import com.pricelens.domain.model.NotificationRecord
import com.pricelens.domain.model.PriceObservation
import com.pricelens.domain.model.TargetPriceAlert
import java.util.UUID

data class AlertEvaluation(
    val alert: TargetPriceAlert,
    val reached: Boolean,
    val notifications: List<NotificationRecord>
)

/**
 * Evaluates one observation at a time. Persisting lastEvaluatedObservationId and
 * the notification record makes retries safe at the repository boundary.
 * Supports tracking specific outside website/retailer prices as well as product-wide lowest prices.
 */
class AlertEvaluator(
    private val now: () -> Long = { System.currentTimeMillis() }
) {
    fun createAlert(
        productId: String,
        targetPrice: Double,
        currency: String,
        retailerName: String? = null,
        websiteUrl: String? = null
    ): TargetPriceAlert =
        TargetPriceAlert(
            id = UUID.randomUUID().toString(),
            productId = productId,
            targetPrice = targetPrice,
            currency = currency,
            retailerName = retailerName,
            websiteUrl = websiteUrl
        )

    fun evaluate(
        alert: TargetPriceAlert,
        observation: PriceObservation,
        existingNotifications: List<NotificationRecord> = emptyList()
    ): AlertEvaluation {
        require(alert.productId == observation.productId)
        require(alert.currency == observation.currency)

        if (!alert.isActive || alert.lastEvaluatedObservationId == observation.id) {
            return AlertEvaluation(alert, reached = false, notifications = existingNotifications)
        }

        val matchesRetailer = alert.retailerName.isNullOrBlank() ||
            observation.retailerName.equals(alert.retailerName, ignoreCase = true)

        val reached = matchesRetailer && observation.price <= alert.targetPrice && alert.reachedAt == null
        val notification = if (reached) {
            NotificationRecord(
                id = "${alert.id}:${observation.id}",
                alertId = alert.id,
                observationId = observation.id,
                createdAt = now(),
                delivered = false
            )
        } else {
            null
        }
        val updatedAlert = alert.copy(
            lastEvaluatedObservationId = observation.id,
            reachedAt = if (reached) observation.observedAt else alert.reachedAt,
            isActive = alert.isActive && !reached
        )
        return AlertEvaluation(
            alert = updatedAlert,
            reached = reached,
            notifications = if (notification == null) {
                existingNotifications
            } else {
                existingNotifications + notification
            }
        )
    }
}
