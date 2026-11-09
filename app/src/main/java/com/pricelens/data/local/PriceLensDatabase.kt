package com.pricelens.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pricelens.data.local.dao.RecentSearchDao
import com.pricelens.data.local.dao.PriceHistoryDao
import com.pricelens.data.local.dao.TargetPriceAlertDao
import com.pricelens.data.local.dao.WatchlistDao
import com.pricelens.data.local.entity.RecentSearchEntity
import com.pricelens.data.local.entity.NotificationRecordEntity
import com.pricelens.data.local.entity.PriceObservationEntity
import com.pricelens.data.local.entity.TargetPriceAlertEntity
import com.pricelens.data.local.entity.WatchlistEntity

@Database(
    entities = [
        RecentSearchEntity::class,
        WatchlistEntity::class,
        PriceObservationEntity::class,
        TargetPriceAlertEntity::class,
        NotificationRecordEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PriceLensDatabase : RoomDatabase() {
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun targetPriceAlertDao(): TargetPriceAlertDao
}
