package com.pricelens.core.navigation

sealed class PriceLensDestination(val route: String) {
    data object Dashboard : PriceLensDestination("dashboard")
    data object Scanner : PriceLensDestination("scanner")
    data object Watchlist : PriceLensDestination("watchlist")
    data object Search : PriceLensDestination("search?q={q}") {
        fun createRoute(query: String = ""): String = "search?q=$query"
    }
}
