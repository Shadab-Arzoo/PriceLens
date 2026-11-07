package com.pricelens.data.remote.adapter

/**
 * Server-side configuration metadata for authorized pricing feeds.
 *
 * Enforces that provider credentials (such as SerpApi keys, Amazon PA-API secrets, or
 * Cuelinks affiliate tokens) remain strictly hosted in server-side infrastructure and are
 * never accessible to or embedded inside the Android client bundle.
 */
data class AuthorizedPricingConfig(
    val providerName: String = "Authorized Multi-Retailer Proxy",
    val serverBaseUrl: String = "https://api.pricelens.internal/v1",
    val timeoutMillis: Long = 10000L,
    val isLiveProviderApproved: Boolean = false,
    val approvedBy: String? = null
)
