package com.pricelens.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pricelens.domain.model.WatchlistItem

@Entity(tableName = "watchlist_items")
data class WatchlistEntity(
    @PrimaryKey
    @ColumnInfo(name = "product_id")
    val productId: String,
    @ColumnInfo(name = "product_name")
    val productName: String,
    @ColumnInfo(name = "brand")
    val brand: String?,
    @ColumnInfo(name = "target_price")
    val targetPrice: Double?,
    @ColumnInfo(name = "current_lowest_price")
    val currentLowestPrice: Double?,
    @ColumnInfo(name = "currency")
    val currency: String,
    @ColumnInfo(name = "added_at")
    val addedAt: Long
) {
    fun toDomain(): WatchlistItem = WatchlistItem(
        productId = productId,
        productName = productName,
        brand = brand,
        targetPrice = targetPrice,
        currentLowestPrice = currentLowestPrice,
        currency = currency,
        addedAtTimestamp = addedAt
    )
}
