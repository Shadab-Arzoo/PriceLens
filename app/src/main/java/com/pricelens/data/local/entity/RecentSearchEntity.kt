package com.pricelens.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pricelens.domain.model.RecentSearch

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey
    @ColumnInfo(name = "query")
    val query: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
) {
    fun toDomain(): RecentSearch = RecentSearch(
        query = query,
        timestamp = timestamp
    )
}
