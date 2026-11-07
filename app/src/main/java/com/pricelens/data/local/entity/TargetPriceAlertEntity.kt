package com.pricelens.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pricelens.domain.model.TargetPriceAlert

@Entity(tableName = "target_price_alerts")
data class TargetPriceAlertEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "product_id") val productId: String,
    @ColumnInfo(name = "target_price") val targetPrice: Double,
    val currency: String,
    @ColumnInfo(name = "is_active") val isActive: Boolean,
    @ColumnInfo(name = "last_observation_id") val lastObservationId: String?,
    @ColumnInfo(name = "reached_at") val reachedAt: Long?
) {
    fun toDomain() = TargetPriceAlert(
        id, productId, targetPrice, currency, isActive, lastObservationId, reachedAt
    )

    companion object {
        fun fromDomain(alert: TargetPriceAlert) = TargetPriceAlertEntity(
            alert.id, alert.productId, alert.targetPrice, alert.currency,
            alert.isActive, alert.lastEvaluatedObservationId, alert.reachedAt
        )
    }
}
