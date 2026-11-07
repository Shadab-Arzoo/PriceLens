package com.pricelens.domain.repository

import com.pricelens.domain.model.RecentSearch
import com.pricelens.domain.model.WatchlistItem
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getRecentSearches(): Flow<List<RecentSearch>>
    fun getWatchlistItems(): Flow<List<WatchlistItem>>
    suspend fun saveRecentSearch(query: String)
    suspend fun clearRecentSearches()
}
