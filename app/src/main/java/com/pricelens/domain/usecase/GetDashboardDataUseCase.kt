package com.pricelens.domain.usecase

import com.pricelens.domain.model.ProductSummary
import com.pricelens.domain.model.RecentSearch
import com.pricelens.domain.model.WatchlistItem
import com.pricelens.domain.repository.DashboardRepository
import com.pricelens.domain.repository.ProductCatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class DashboardData(
    val recentSearches: List<RecentSearch>,
    val watchlistItems: List<WatchlistItem>,
    val trendingProducts: List<ProductSummary>
)

class GetDashboardDataUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val productCatalogRepository: ProductCatalogRepository
) {
    operator fun invoke(): Flow<DashboardData> {
        return combine(
            dashboardRepository.getRecentSearches(),
            dashboardRepository.getWatchlistItems()
        ) { searches, watchlist ->
            // Query real trending products; if none recorded, returns empty list (strictly no fabricated data)
            val trending = productCatalogRepository.getTrendingProducts().getOrDefault(emptyList())
            DashboardData(
                recentSearches = searches,
                watchlistItems = watchlist,
                trendingProducts = trending
            )
        }
    }
}
