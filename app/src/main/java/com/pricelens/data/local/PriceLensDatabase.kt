package com.pricelens.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pricelens.data.local.dao.RecentSearchDao
import com.pricelens.data.local.dao.WatchlistDao
import com.pricelens.data.local.entity.RecentSearchEntity
import com.pricelens.data.local.entity.WatchlistEntity

@Database(
    entities = [
        RecentSearchEntity::class,
        WatchlistEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PriceLensDatabase : RoomDatabase() {
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun watchlistDao(): WatchlistDao
}
