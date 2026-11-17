package com.pricelens.presentation.deals

enum class DealCategory(val title: String) {
    ALL("All Deals"),
    SMARTPHONES("Smartphones"),
    LAPTOPS("Laptops & Mac"),
    AUDIO("Audio & ANC"),
    WEARABLES("Wearables")
}

enum class DealSortOrder(val label: String) {
    BIGGEST_DISCOUNT("Biggest Discount %"),
    MAX_SAVINGS("Highest Cash Savings"),
    PRICE_LOW_TO_HIGH("Price: Low to High")
}

enum class BudgetFilter(val label: String) {
    ALL("All Budgets"),
    UNDER_25K("Under ₹25,000"),
    UNDER_50K("Under ₹50,000"),
    FLAGSHIPS("Flagships (₹50k+)")
}

data class DealItem(
    val productId: String,
    val title: String,
    val brand: String,
    val category: DealCategory,
    val currentPrice: Double,
    val originalPrice: Double,
    val savingsAmount: Double,
    val discountPercent: Int,
    val lowestRetailer: String,
    val retailerCount: Int,
    val storeUrl: String,
    val imageUrl: String?,
    val dealBadge: String,
    val highlightPromo: String
)

data class DealsUiState(
    val isLoading: Boolean = true,
    val deals: List<DealItem> = emptyList(),
    val spotlightDeal: DealItem? = null,
    val searchQuery: String = "",
    val minDiscountPercent: Int = 0,
    val selectedCategory: DealCategory = DealCategory.ALL,
    val selectedStore: String = "All Stores",
    val selectedSort: DealSortOrder = DealSortOrder.BIGGEST_DISCOUNT,
    val selectedBudget: BudgetFilter = BudgetFilter.ALL,
    val notificationMessage: String? = null
)
