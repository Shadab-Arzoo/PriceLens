package com.pricelens.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pricelens.domain.model.NotificationRecord

@Entity(
    tableName = "notification_records",
    indices = [androidx.room.Index(value = ["alert_id", "observation_id"], unique = true)]
)
data class NotificationRecordEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "alert_id") val alertId: String,
    @ColumnInfo(name = "observation_id") val observationId: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    val delivered: Boolean
) {
    fun toDomain() = NotificationRecord(id, alertId, observationId, createdAt, delivered)

    companion object {
        fun fromDomain(record: NotificationRecord) = NotificationRecordEntity(
            record.id, record.alertId, record.observationId, record.createdAt, record.delivered
        )
    }
}
