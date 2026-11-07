package com.pricelens.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pricelens.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist_items ORDER BY added_at DESC")
    fun getWatchlistItems(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: WatchlistEntity)

    @Delete
    suspend fun deleteItem(item: WatchlistEntity)

    @Query("DELETE FROM watchlist_items WHERE product_id = :productId")
    suspend fun deleteById(productId: String)
}
