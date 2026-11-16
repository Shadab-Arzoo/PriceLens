package com.pricelens.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pricelens.data.local.entity.PriceObservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(observation: PriceObservationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservations(observations: List<PriceObservationEntity>)

    @Query("SELECT * FROM price_observations WHERE product_id = :productId ORDER BY observed_at ASC")
    fun observeHistory(productId: String): Flow<List<PriceObservationEntity>>

    @Query("SELECT * FROM price_observations WHERE product_id = :productId ORDER BY observed_at ASC")
    suspend fun getHistory(productId: String): List<PriceObservationEntity>
}
