package com.pricelens.data.repository

import com.pricelens.data.local.dao.RecentSearchDao
import com.pricelens.data.local.dao.WatchlistDao
import com.pricelens.data.local.entity.RecentSearchEntity
import com.pricelens.domain.model.RecentSearch
import com.pricelens.domain.model.WatchlistItem
import com.pricelens.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val recentSearchDao: RecentSearchDao,
    private val watchlistDao: WatchlistDao
) : DashboardRepository {

    override fun getRecentSearches(): Flow<List<RecentSearch>> {
        return recentSearchDao.getRecentSearches().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getWatchlistItems(): Flow<List<WatchlistItem>> {
        return watchlistDao.getWatchlistItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveRecentSearch(query: String) {
        recentSearchDao.insertSearch(
            RecentSearchEntity(
                query = query,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun clearRecentSearches() {
        recentSearchDao.clearAll()
    }
}
