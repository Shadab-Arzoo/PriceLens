package com.pricelens.domain.usecase

import com.pricelens.domain.repository.DashboardRepository
import javax.inject.Inject

class SaveRecentSearchUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository
) {
    suspend operator fun invoke(query: String) {
        val sanitized = query.trim()
        if (sanitized.isNotBlank()) {
            dashboardRepository.saveRecentSearch(sanitized)
        }
    }
}
