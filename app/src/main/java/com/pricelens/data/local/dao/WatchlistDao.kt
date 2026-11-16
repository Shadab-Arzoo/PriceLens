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

    @Query("SELECT * FROM watchlist_items WHERE product_id = :productId LIMIT 1")
    suspend fun getWatchlistItemById(productId: String): WatchlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: WatchlistEntity)

    @Delete
    suspend fun deleteItem(item: WatchlistEntity)

    @Query("DELETE FROM watchlist_items WHERE product_id = :productId")
    suspend fun deleteById(productId: String)

    @Query("UPDATE watchlist_items SET current_lowest_price = :lowestPrice WHERE product_id = :productId")
    suspend fun updateLowestPrice(productId: String, lowestPrice: Double)
}
