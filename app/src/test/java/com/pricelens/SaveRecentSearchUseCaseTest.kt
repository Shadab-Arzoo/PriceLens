package com.pricelens

import com.pricelens.domain.model.RecentSearch
import com.pricelens.domain.model.WatchlistItem
import com.pricelens.domain.repository.DashboardRepository
import com.pricelens.domain.usecase.SaveRecentSearchUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveRecentSearchUseCaseTest {

    private class FakeDashboardRepository : DashboardRepository {
        val saved = mutableListOf<String>()

        override fun getRecentSearches(): Flow<List<RecentSearch>> = flowOf(emptyList())
        override fun getWatchlistItems(): Flow<List<WatchlistItem>> = flowOf(emptyList())

        override suspend fun saveRecentSearch(query: String) {
            saved.add(query)
        }

        override suspend fun clearRecentSearches() {
            saved.clear()
        }
    }

    @Test
    fun `invoking with valid query trims and saves`() = runTest {
        val repo = FakeDashboardRepository()
        val useCase = SaveRecentSearchUseCase(repo)

        useCase("  Sony WH-1000XM5  ")

        assertEquals(1, repo.saved.size)
        assertEquals("Sony WH-1000XM5", repo.saved[0])
    }

    @Test
    fun `invoking with blank query does not save`() = runTest {
        val repo = FakeDashboardRepository()
        val useCase = SaveRecentSearchUseCase(repo)

        useCase("    ")

        assertEquals(0, repo.saved.size)
    }
}
