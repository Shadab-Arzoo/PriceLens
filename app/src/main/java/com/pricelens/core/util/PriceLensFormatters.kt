package com.pricelens.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.NumberFormat
import java.util.Locale

/**
 * High-performance cached currency formatter utility for PriceLens.
 * Eliminates repeated heavyweight NumberFormat instantiation across Compose lazy lists and cards.
 */
object PriceLensFormatters {
    private val inrFormatNoDecimals = ThreadLocal.withInitial {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }

    private val inrFormatWithDecimals = ThreadLocal.withInitial {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 2
        }
    }

    /**
     * Formats an INR price into Indian currency format (e.g., ₹74,999) using thread-local cached formatters.
     */
    fun formatInr(price: Double, includeDecimals: Boolean = false): String {
        val formatter = if (includeDecimals) inrFormatWithDecimals.get() else inrFormatNoDecimals.get()
        return formatter?.format(price) ?: "₹${"%,.0f".format(price)}"
    }

    /**
     * Formats an optional price, or returns a placeholder if null.
     */
    fun formatInrOrPlaceholder(price: Double?, placeholder: String = "—"): String {
        return if (price != null) formatInr(price) else placeholder
    }
}

/**
 * Composable helper returning a remembered NumberFormat instance, avoiding reallocation during recomposition.
 */
@Composable
fun rememberInrCurrencyFormatter(maximumFractionDigits: Int = 0): NumberFormat {
    return remember(maximumFractionDigits) {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            this.maximumFractionDigits = maximumFractionDigits
        }
    }
}
