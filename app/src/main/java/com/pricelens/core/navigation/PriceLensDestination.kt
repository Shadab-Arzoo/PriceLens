package com.pricelens.core.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class PriceLensDestination(val route: String) {
    data object Onboarding : PriceLensDestination("onboarding")
    data object Dashboard : PriceLensDestination("dashboard")
    data object Scanner : PriceLensDestination("scanner")
    data object Watchlist : PriceLensDestination("watchlist")
    data object Search : PriceLensDestination("search?q={q}") {
        fun createRoute(query: String = ""): String {
            val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
            return "search?q=$encoded"
        }
    }
    data object ProductComparison : PriceLensDestination("product/{productId}?productName={productName}") {
        fun createRoute(productId: String, productName: String = ""): String {
            val encodedName = URLEncoder.encode(productName, StandardCharsets.UTF_8.name())
            return "product/$productId?productName=$encodedName"
        }
    }
}
