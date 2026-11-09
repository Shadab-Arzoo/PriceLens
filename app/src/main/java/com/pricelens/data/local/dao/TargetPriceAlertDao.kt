package com.pricelens.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pricelens.data.local.entity.NotificationRecordEntity
import com.pricelens.data.local.entity.TargetPriceAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TargetPriceAlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAlert(alert: TargetPriceAlertEntity)

    @Query("SELECT * FROM target_price_alerts WHERE product_id = :productId ORDER BY id")
    fun observeAlerts(productId: String): Flow<List<TargetPriceAlertEntity>>

    @Query("SELECT * FROM target_price_alerts WHERE product_id = :productId AND is_active = 1")
    suspend fun getActiveAlerts(productId: String): List<TargetPriceAlertEntity>

    @Query("DELETE FROM target_price_alerts WHERE id = :alertId")
    suspend fun deleteAlert(alertId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotification(record: NotificationRecordEntity)

    @Query("SELECT * FROM notification_records WHERE alert_id = :alertId ORDER BY created_at DESC")
    suspend fun getNotifications(alertId: String): List<NotificationRecordEntity>
}
