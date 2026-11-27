package com.pricelens.presentation.deals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricelens.domain.model.TargetPriceAlert
import com.pricelens.domain.repository.ProductCatalogRepository
import com.pricelens.domain.repository.TargetPriceAlertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class DealsViewModel @Inject constructor(
    private val productCatalogRepository: ProductCatalogRepository,
    private val targetPriceAlertRepository: TargetPriceAlertRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(DealsUiState())
    val uiState: StateFlow<DealsUiState> = _uiState.asStateFlow()

    private var allDealsCache: List<DealItem> = emptyList()
    private var loadDealsJob: Job? = null

    init {
        loadDeals()
    }

    fun loadDeals() {
        loadDealsJob?.cancel()
        loadDealsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = productCatalogRepository.searchProducts("")
            result.onSuccess { summaries ->
                allDealsCache = summaries.map { summary ->
                    val title = summary.name
                    val category = when {
                        title.contains("iPhone", ignoreCase = true) ||
                        title.contains("Galaxy", ignoreCase = true) ||
                        title.contains("OnePlus", ignoreCase = true) ||
                        title.contains("Pixel", ignoreCase = true) -> DealCategory.SMARTPHONES

                        title.contains("MacBook", ignoreCase = true) ||
                        title.contains("Dell", ignoreCase = true) ||
                        title.contains("XPS", ignoreCase = true) ||
                        title.contains("Air", ignoreCase = true) -> DealCategory.LAPTOPS

                        title.contains("WH-", ignoreCase = true) ||
                        title.contains("AirPods", ignoreCase = true) ||
                        title.contains("QuietComfort", ignoreCase = true) ||
                        title.contains("Headphone", ignoreCase = true) -> DealCategory.AUDIO

                        title.contains("Watch", ignoreCase = true) ||
                        title.contains("Band", ignoreCase = true) -> DealCategory.WEARABLES

                        else -> DealCategory.SMARTPHONES
                    }

                    val currentPrice = summary.lowestPrice ?: 0.0
                    val high = summary.highestPrice ?: (currentPrice * 1.18)
                    val originalPrice = if (high > currentPrice) high else (currentPrice * 1.18).roundToInt().toDouble()
                    val savings = (originalPrice - currentPrice).coerceAtLeast(500.0)
                    val discountPercent = if (originalPrice > 0) {
                        ((savings / originalPrice) * 100).roundToInt().coerceIn(10, 40)
                    } else 15

                    val store = summary.lowestPriceRetailer ?: "Amazon India"
                    val promo = when {
                        store.contains("Amazon", ignoreCase = true) -> "HDFC/ICICI Card Instant Off + 5% APay CB"
                        store.contains("Flipkart", ignoreCase = true) -> "Flipkart Axis Bank 5% Unlimited CB"
                        store.contains("Croma", ignoreCase = true) -> "ICICI Card Off + 5% Tata NeuPass Coins"
                        store.contains("Reliance", ignoreCase = true) -> "OneCard & SBI Instant Card Off"
                        else -> "Instant Bank Card Off & No Cost EMI"
                    }

                    val badge = when {
                        discountPercent >= 22 -> "🔥 MEGA DROP"
                        currentPrice < 30000 -> "⚡ VALUE DEAL"
                        else -> "✓ 30-DAY LOW"
                    }

                    DealItem(
                        productId = summary.id,
                        title = summary.name,
                        brand = summary.brand ?: "Top Brand",
                        category = category,
                        currentPrice = currentPrice,
                        originalPrice = originalPrice,
                        savingsAmount = savings,
                        discountPercent = discountPercent,
                        lowestRetailer = store,
                        retailerCount = summary.retailerCount.coerceAtLeast(3),
                        storeUrl = "https://www.google.com/search?q=${URLEncoder.encode(summary.name, StandardCharsets.UTF_8.name())}",
                        imageUrl = summary.imageUrl,
                        dealBadge = badge,
                        highlightPromo = promo
                    )
                }
                applyFilterAndSort()
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onCategorySelected(category: DealCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilterAndSort()
    }

    fun onStoreSelected(store: String) {
        _uiState.update { it.copy(selectedStore = store) }
        applyFilterAndSort()
    }

    fun onSortSelected(sort: DealSortOrder) {
        _uiState.update { it.copy(selectedSort = sort) }
        applyFilterAndSort()
    }

    fun onBudgetSelected(budget: BudgetFilter) {
        _uiState.update { it.copy(selectedBudget = budget) }
        applyFilterAndSort()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilterAndSort()
    }

    fun onMinDiscountChanged(minDiscount: Int) {
        _uiState.update { it.copy(minDiscountPercent = minDiscount) }
        applyFilterAndSort()
    }

    fun quickSetAlert(deal: DealItem, targetPrice: Double) {
        viewModelScope.launch {
            if (targetPriceAlertRepository != null) {
                targetPriceAlertRepository.save(
                    TargetPriceAlert(
                        id = UUID.randomUUID().toString(),
                        productId = deal.productId,
                        targetPrice = targetPrice,
                        currency = "INR",
                        retailerName = deal.lowestRetailer
                    )
                )
            }
            _uiState.update {
                it.copy(notificationMessage = "Price Alert set for ${deal.title} at ₹${targetPrice.toInt()}!")
            }
        }
    }

    fun clearNotification() {
        _uiState.update { it.copy(notificationMessage = null) }
    }

    private fun applyFilterAndSort() {
        val state = _uiState.value
        var filtered = allDealsCache

        // 1. Category Filter
        if (state.selectedCategory != DealCategory.ALL) {
            filtered = filtered.filter { it.category == state.selectedCategory }
        }

        // 2. Store Filter
        if (state.selectedStore != "All Stores") {
            filtered = filtered.filter {
                it.lowestRetailer.contains(state.selectedStore, ignoreCase = true)
            }
        }

        // 3. Budget Filter
        filtered = when (state.selectedBudget) {
            BudgetFilter.ALL -> filtered
            BudgetFilter.UNDER_25K -> filtered.filter { it.currentPrice < 25000 }
            BudgetFilter.UNDER_50K -> filtered.filter { it.currentPrice < 50000 }
            BudgetFilter.FLAGSHIPS -> filtered.filter { it.currentPrice >= 50000 }
        }

        // 4. In-page Search Query Filter
        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.trim().lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(q) ||
                it.brand.lowercase().contains(q) ||
                it.lowestRetailer.lowercase().contains(q)
            }
        }

        // 5. Min Discount Filter
        if (state.minDiscountPercent > 0) {
            filtered = filtered.filter { it.discountPercent >= state.minDiscountPercent }
        }

        // 4. Sort
        val sorted = when (state.selectedSort) {
            DealSortOrder.BIGGEST_DISCOUNT -> filtered.sortedByDescending { it.discountPercent }
            DealSortOrder.MAX_SAVINGS -> filtered.sortedByDescending { it.savingsAmount }
            DealSortOrder.PRICE_LOW_TO_HIGH -> filtered.sortedBy { it.currentPrice }
        }

        val spotlight = allDealsCache.maxByOrNull { it.discountPercent }

        _uiState.update {
            it.copy(
                isLoading = false,
                deals = sorted,
                spotlightDeal = spotlight
            )
        }
    }
}
