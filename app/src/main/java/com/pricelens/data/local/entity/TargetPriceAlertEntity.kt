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
    @ColumnInfo(name = "reached_at") val reachedAt: Long?,
    @ColumnInfo(name = "retailer_name") val retailerName: String? = null,
    @ColumnInfo(name = "website_url") val websiteUrl: String? = null
) {
    fun toDomain() = TargetPriceAlert(
        id = id,
        productId = productId,
        targetPrice = targetPrice,
        currency = currency,
        isActive = isActive,
        lastEvaluatedObservationId = lastObservationId,
        reachedAt = reachedAt,
        retailerName = retailerName,
        websiteUrl = websiteUrl
    )

    companion object {
        fun fromDomain(alert: TargetPriceAlert) = TargetPriceAlertEntity(
            id = alert.id,
            productId = alert.productId,
            targetPrice = alert.targetPrice,
            currency = alert.currency,
            isActive = alert.isActive,
            lastObservationId = alert.lastEvaluatedObservationId,
            reachedAt = alert.reachedAt,
            retailerName = alert.retailerName,
            websiteUrl = alert.websiteUrl
        )
    }
}
