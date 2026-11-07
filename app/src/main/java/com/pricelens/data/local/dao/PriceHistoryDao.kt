package com.pricelens.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pricelens.data.local.entity.PriceObservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertObservation(observation: PriceObservationEntity)

    @Query("SELECT * FROM price_observations WHERE product_id = :productId ORDER BY observed_at ASC")
    fun observeHistory(productId: String): Flow<List<PriceObservationEntity>>
}
