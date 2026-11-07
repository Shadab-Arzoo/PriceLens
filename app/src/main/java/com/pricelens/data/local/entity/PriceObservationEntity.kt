package com.pricelens.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pricelens.domain.model.PriceObservation

@Entity(
    tableName = "price_observations",
    indices = [
        androidx.room.Index(value = ["product_id", "retailer_id", "observed_at"])
    ]
)
data class PriceObservationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "product_id") val productId: String,
    @ColumnInfo(name = "retailer_id") val retailerId: String,
    @ColumnInfo(name = "retailer_name") val retailerName: String,
    val price: Double,
    val currency: String,
    @ColumnInfo(name = "observed_at") val observedAt: Long
) {
    fun toDomain() = PriceObservation(
        id, productId, retailerId, retailerName, price, currency, observedAt
    )

    companion object {
        fun fromDomain(observation: PriceObservation) = PriceObservationEntity(
            id = observation.id,
            productId = observation.productId,
            retailerId = observation.retailerId,
            retailerName = observation.retailerName,
            price = observation.price,
            currency = observation.currency,
            observedAt = observation.observedAt
        )
    }
}
