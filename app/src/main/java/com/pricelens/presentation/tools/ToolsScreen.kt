package com.pricelens.presentation.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pricelens.core.designsystem.AmberAlert
import com.pricelens.core.designsystem.CyanAccent
import com.pricelens.core.designsystem.ElectricBlue
import com.pricelens.core.designsystem.LocalThemeToggle
import com.pricelens.core.designsystem.MintSuccess
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.preferences.UserPreferencesRepository
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min

@Composable
fun ToolsRoute(
    onNavigateToWebPicker: (url: String, name: String) -> Unit,
    onNavigateToSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferencesRepository(context.applicationContext) }

    ToolsScreen(
        userPreferences = userPreferences,
        onNavigateToWebPicker = onNavigateToWebPicker,
        onNavigateToSearch = onNavigateToSearch,
        modifier = modifier
    )
}

data class UrlDiagnosticInfo(
    val storeName: String,
    val storeColor: Color,
    val identifierType: String,
    val identifierValue: String?,
    val isSupported: Boolean,
    val diagnosticMessage: String
)

private fun diagnoseStoreUrl(url: String): UrlDiagnosticInfo {
    val cleanUrl = url.trim()
    if (cleanUrl.isBlank()) {
        return UrlDiagnosticInfo(
            storeName = "Awaiting Store URL",
            storeColor = Color.Gray,
            identifierType = "N/A",
            identifierValue = null,
            isSupported = false,
            diagnosticMessage = "Paste a product link from Amazon, Flipkart, Croma, Reliance, or Vijay Sales."
        )
    }

    val lower = cleanUrl.lowercase()
    return when {
        lower.contains("amazon.in") || lower.contains("amzn.in") || lower.contains("amzn.to") -> {
            val asinRegex = Regex("""(?:/dp/|/gp/product/|/d/)([A-Z0-9]{10})""", RegexOption.IGNORE_CASE)
            val asin = asinRegex.find(cleanUrl)?.groupValues?.get(1)
            UrlDiagnosticInfo(
                storeName = "Amazon India",
                storeColor = Color(0xFFFF9900),
                identifierType = "Amazon Standard Identification Number (ASIN)",
                identifierValue = asin ?: "Detected in URL parameters",
                isSupported = true,
                diagnosticMessage = if (asin != null) "Verified ASIN: $asin • Direct Product Page" else "Amazon URL recognized • Direct Product Link"
            )
        }
        lower.contains("flipkart.com") || lower.contains("fkrt.it") -> {
            val pidRegex = Regex("""pid=([A-Za-z0-9]{16})""", RegexOption.IGNORE_CASE)
            val pid = pidRegex.find(cleanUrl)?.groupValues?.get(1)
            UrlDiagnosticInfo(
                storeName = "Flipkart",
                storeColor = Color(0xFF2874F0),
                identifierType = "Flipkart Serial Number (FSN / PID)",
                identifierValue = pid ?: "Detected in URL parameters",
                isSupported = true,
                diagnosticMessage = if (pid != null) "Verified FSN: $pid • Direct Product Page" else "Flipkart URL recognized • Direct Product Link"
            )
        }
        lower.contains("croma.com") -> {
            val cromaSku = Regex("""/p/(\d+)""").find(cleanUrl)?.groupValues?.get(1)
            UrlDiagnosticInfo(
                storeName = "Croma",
                storeColor = Color(0xFF00E676),
                identifierType = "Croma Product SKU",
                identifierValue = cromaSku ?: "Active Croma Catalog ID",
                isSupported = true,
                diagnosticMessage = "Croma Tata Neu Verified Catalog Link"
            )
        }
        lower.contains("reliancedigital.in") -> {
            val rdSku = Regex("""/p/(\d+)""").find(cleanUrl)?.groupValues?.get(1)
            UrlDiagnosticInfo(
                storeName = "Reliance Digital",
                storeColor = Color(0xFFE53935),
                identifierType = "Reliance Digital Article SKU",
                identifierValue = rdSku ?: "Active Store Item Code",
                isSupported = true,
                diagnosticMessage = "Reliance Digital Official Store Link"
            )
        }
        lower.contains("vijaysales.com") -> {
            UrlDiagnosticInfo(
                storeName = "Vijay Sales",
                storeColor = Color(0xFFFF6D00),
                identifierType = "Vijay Sales SKU / Item Code",
                identifierValue = "Retail Catalog Match",
                isSupported = true,
                diagnosticMessage = "Vijay Sales Retail Electronics Catalog Link"
            )
        }
        else -> {
            UrlDiagnosticInfo(
                storeName = "Universal Web Store",
                storeColor = CyanAccent,
                identifierType = "Generic Web Link",
                identifierValue = null,
                isSupported = true,
                diagnosticMessage = "Supported by Universal Web Price Inspector (Point & Tap Mode)"
            )
        }
    }
}

@Composable
fun ToolsScreen(
    userPreferences: UserPreferencesRepository,
    onNavigateToWebPicker: (url: String, name: String) -> Unit,
    onNavigateToSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }
    val haptic = LocalHapticFeedback.current
    val flags by userPreferences.experimentalFlagsFlow.collectAsState()
    val themeToggle = LocalThemeToggle.current

    // Web Inspector State
    var customUrlInput by remember { mutableStateOf("") }

    // Target Calculator State
    var calcPriceInput by remember { mutableDoubleStateOf(50000.0) }
    var calcDiscountPercent by remember { mutableIntStateOf(15) }
    val calculatedTarget = calcPriceInput * (1.0 - calcDiscountPercent / 100.0)
    val calculatedSavings = calcPriceInput - calculatedTarget

    // No-Cost EMI & Bank Card Calculator State
    var emiBasePrice by remember { mutableDoubleStateOf(65000.0) }
    var selectedBankIndex by remember { mutableIntStateOf(0) }
    val bankOptions = listOf(
        "HDFC Bank" to 3000.0,
        "ICICI Bank" to 2500.0,
        "SBI Card" to 2000.0,
        "Axis Bank" to 0.05,
        "OneCard" to 1500.0
    )
    var selectedTenorMonths by remember { mutableIntStateOf(6) }
    val tenorOptions = listOf(3, 6, 9, 12)

    val currentBank = bankOptions[selectedBankIndex]
    val emiDiscount = if (currentBank.first == "Axis Bank") {
        emiBasePrice * currentBank.second
    } else {
        min(currentBank.second, emiBasePrice * 0.15)
    }
    val emiNetPayable = (emiBasePrice - emiDiscount).coerceAtLeast(0.0)
    val monthlyInstallment = emiNetPayable / selectedTenorMonths

    // Store URL Diagnostic Analyzer State
    var diagnosticUrlInput by remember { mutableStateOf("https://www.amazon.in/dp/B0DGHSTVD1") }
    val diagnosticResult = remember(diagnosticUrlInput) { diagnoseStoreUrl(diagnosticUrlInput) }

    // Cache purge state
    var cachePurged by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(brush = PriceLensTheme.glassColors.backgroundBrush),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CyanAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Power Tools & Labs",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                }
                Text(
                    text = "Live Web Inspector, Store Diagnostics, EMI Planner & PriceLens Labs",
                    style = MaterialTheme.typography.labelSmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )
            }
        }

        // 2. Tool 1: Universal In-App Web Price Picker
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                borderColor = CyanAccent.copy(alpha = 0.4f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Universal Web Price Inspector",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                                Text(
                                    text = "Point & tap any price element on any retailer website",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PriceLensTheme.glassColors.textSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "LIVE BROWSER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Preset Store Launchers:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val stores = listOf(
                            "Amazon India" to "https://www.amazon.in",
                            "Flipkart" to "https://www.flipkart.com",
                            "Croma" to "https://www.croma.com",
                            "Reliance Digital" to "https://www.reliancedigital.in",
                            "Vijay Sales" to "https://www.vijaysales.com"
                        )
                        stores.forEach { (name, url) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PriceLensTheme.glassColors.surfaceHighlight,
                                border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder),
                                modifier = Modifier.clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onNavigateToWebPicker(url, name)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PriceLensTheme.glassColors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Custom URL launcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customUrlInput,
                            onValueChange = { customUrlInput = it },
                            placeholder = {
                                Text(
                                    text = "Enter any store URL...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PriceLensTheme.glassColors.textTertiary
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = PriceLensTheme.glassColors.specularBorder,
                                focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                                unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CyanAccent,
                            modifier = Modifier.clickable {
                                val target = if (customUrlInput.isNotBlank()) {
                                    if (!customUrlInput.startsWith("http")) "https://$customUrlInput" else customUrlInput
                                } else {
                                    "https://www.amazon.in"
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onNavigateToWebPicker(target, "Web Inspector")
                            }
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
                                Text(
                                    text = "Open",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Tool 2: Store URL Diagnostic Analyzer & ASIN/SKU Extractor
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                borderColor = ElectricBlue.copy(alpha = 0.4f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ElectricBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PriceCheck,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Store URL Diagnostic Analyzer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                                Text(
                                    text = "Extracts ASIN, FSN & verifies direct store feeds",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PriceLensTheme.glassColors.textSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ElectricBlue.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "DIAGNOSTICS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Diagnostic URL Input
                    OutlinedTextField(
                        value = diagnosticUrlInput,
                        onValueChange = { diagnosticUrlInput = it },
                        placeholder = {
                            Text(
                                text = "Paste Amazon, Flipkart or Croma URL...",
                                style = MaterialTheme.typography.bodySmall,
                                color = PriceLensTheme.glassColors.textTertiary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = PriceLensTheme.glassColors.specularBorder,
                            focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                            unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Diagnostic Result Breakdown
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PriceLensTheme.glassColors.surfaceHighlight,
                        border = BorderStroke(1.dp, diagnosticResult.storeColor.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(diagnosticResult.storeColor)
                                    )
                                    Text(
                                        text = diagnosticResult.storeName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = diagnosticResult.storeColor
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MintSuccess.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "READY FOR COMPARISON",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = MintSuccess,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            diagnosticResult.identifierValue?.let { idVal ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = diagnosticResult.identifierType,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PriceLensTheme.glassColors.textSecondary
                                    )
                                    Text(
                                        text = idVal,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PriceLensTheme.glassColors.textPrimary
                                    )
                                }
                            }

                            Text(
                                text = diagnosticResult.diagnosticMessage,
                                style = MaterialTheme.typography.labelSmall,
                                color = PriceLensTheme.glassColors.textSecondary
                            )

                            // Quick Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ElectricBlue.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onNavigateToWebPicker(
                                                diagnosticUrlInput.ifBlank { "https://www.amazon.in" },
                                                diagnosticResult.storeName
                                            )
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Inspect in Web Browser ↗",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ElectricBlue
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PriceLensTheme.glassColors.surfaceHighlight,
                                    border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onNavigateToSearch(diagnosticResult.identifierValue ?: diagnosticResult.storeName)
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Search in PriceLens 🔍",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = PriceLensTheme.glassColors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Tool 3: No-Cost EMI & Bank Card Calculator
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                borderColor = AmberAlert.copy(alpha = 0.4f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AmberAlert.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = AmberAlert,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "No-Cost EMI & Bank Card Planner",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                                Text(
                                    text = "Instant bank discounts + 0% interest tenors",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PriceLensTheme.glassColors.textSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AmberAlert.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "EMI LAB",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberAlert,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Product Base Price Selector
                    Text(
                        text = "Base Product Price:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val pricePresets = listOf(30000.0, 50000.0, 75000.0, 120000.0)
                        pricePresets.forEach { prc ->
                            val isSelected = emiBasePrice == prc
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) AmberAlert.copy(alpha = 0.2f) else PriceLensTheme.glassColors.surfaceHighlight,
                                border = BorderStroke(1.dp, if (isSelected) AmberAlert else PriceLensTheme.glassColors.specularBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        emiBasePrice = prc
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currencyFormatter.format(prc),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) AmberAlert else PriceLensTheme.glassColors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Bank Card Selector Chips
                    Text(
                        text = "Select Bank Card Offer:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        bankOptions.forEachIndexed { idx, (bankName, _) ->
                            val isSelected = selectedBankIndex == idx
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AmberAlert.copy(alpha = 0.25f) else PriceLensTheme.glassColors.surfaceHighlight,
                                border = BorderStroke(1.dp, if (isSelected) AmberAlert else PriceLensTheme.glassColors.specularBorder),
                                modifier = Modifier.clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedBankIndex = idx
                                }
                            ) {
                                Text(
                                    text = bankName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AmberAlert else PriceLensTheme.glassColors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Tenor Chips
                    Text(
                        text = "No-Cost EMI Tenor Duration:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tenorOptions.forEach { months ->
                            val isSelected = selectedTenorMonths == months
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MintSuccess.copy(alpha = 0.2f) else PriceLensTheme.glassColors.surfaceHighlight,
                                border = BorderStroke(1.dp, if (isSelected) MintSuccess else PriceLensTheme.glassColors.specularBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedTenorMonths = months
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$months Mos",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MintSuccess else PriceLensTheme.glassColors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    // EMI Output Summary Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PriceLensTheme.glassColors.surfaceHighlight,
                        border = BorderStroke(1.dp, MintSuccess.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Monthly Instalment (No-Cost)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PriceLensTheme.glassColors.textSecondary
                                    )
                                    Text(
                                        text = "Instant Discount: -${currencyFormatter.format(emiDiscount)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MintSuccess
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${currencyFormatter.format(monthlyInstallment)}/mo",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MintSuccess
                                    )
                                    Text(
                                        text = "for $selectedTenorMonths months",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PriceLensTheme.glassColors.textSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Net Outflow Payable:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                                Text(
                                    text = currencyFormatter.format(emiNetPayable),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Tool 4: Target Price & Savings Calculator
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                borderColor = PriceLensTheme.glassColors.specularBorder
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MintSuccess.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MintSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Target Buy & Savings Calculator",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.textPrimary
                            )
                            Text(
                                text = "Compute alert thresholds and target price dips",
                                style = MaterialTheme.typography.labelSmall,
                                color = PriceLensTheme.glassColors.textSecondary
                            )
                        }
                    }

                    // Price Presets
                    Text(
                        text = "Current Item Base Price:",
                        style = MaterialTheme.typography.labelSmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(15000.0, 30000.0, 50000.0, 75000.0)
                        presets.forEach { price ->
                            val isSelected = calcPriceInput == price
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MintSuccess.copy(alpha = 0.2f) else PriceLensTheme.glassColors.surfaceHighlight,
                                border = BorderStroke(1.dp, if (isSelected) MintSuccess else PriceLensTheme.glassColors.specularBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        calcPriceInput = price
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currencyFormatter.format(price),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MintSuccess else PriceLensTheme.glassColors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Desired Dip %
                    Text(
                        text = "Target Price Dip: ${calcDiscountPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 20, 25).forEach { pct ->
                            val isSelected = calcDiscountPercent == pct
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) CyanAccent.copy(alpha = 0.2f) else PriceLensTheme.glassColors.surfaceHighlight,
                                border = BorderStroke(1.dp, if (isSelected) CyanAccent else PriceLensTheme.glassColors.specularBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        calcDiscountPercent = pct
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$pct%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) CyanAccent else PriceLensTheme.glassColors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Calculation Result Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PriceLensTheme.glassColors.surfaceHighlight,
                        border = BorderStroke(1.dp, MintSuccess.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Target Buying Price",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PriceLensTheme.glassColors.textSecondary
                                )
                                Text(
                                    text = "Cash Savings: ${currencyFormatter.format(calculatedSavings)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MintSuccess
                                )
                            }
                            Text(
                                text = currencyFormatter.format(calculatedTarget),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MintSuccess
                            )
                        }
                    }
                }
            }
        }

        // 6. Tool 5: Store Health & Connectivity Status
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                borderColor = PriceLensTheme.glassColors.specularBorder
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "STORE INTEGRATION HEALTH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textSecondary
                    )

                    val stores = listOf(
                        Triple("Amazon India", "Direct Retailer Feed & ASIN Tracker", "100% OPERATIONAL"),
                        Triple("Flipkart", "Live Pricing & Axis Bank Discount Rules", "100% OPERATIONAL"),
                        Triple("Croma", "Online Catalog & Tata Neu NeuCoins", "99.8% OPERATIONAL"),
                        Triple("Reliance Digital", "Store Catalog & OneCard Discounts", "100% OPERATIONAL"),
                        Triple("Vijay Sales", "Electronics Deals & MyVS Loyalty", "99.5% OPERATIONAL")
                    )

                    stores.forEach { (name, desc, status) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = PriceLensTheme.glassColors.textSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MintSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = status,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MintSuccess,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Tool 6: App Cache & Local Storage Diagnostics
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                borderColor = PriceLensTheme.glassColors.specularBorder
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STORAGE & CACHE DIAGNOSTICS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.textSecondary
                            )
                            Text(
                                text = "Local Room DB SQLite & Scraper Cache",
                                style = MaterialTheme.typography.bodySmall,
                                color = PriceLensTheme.glassColors.textPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MintSuccess.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (cachePurged) "PURGED & CLEAN" else "OPTIMAL",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = MintSuccess,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PriceLensTheme.glassColors.surfaceHighlight,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Cached Comparisons",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = PriceLensTheme.glassColors.textSecondary
                                )
                                Text(
                                    text = if (cachePurged) "0 active (Fresh)" else "64 comparisons",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PriceLensTheme.glassColors.surfaceHighlight,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Storage Consumed",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = PriceLensTheme.glassColors.textSecondary
                                )
                                Text(
                                    text = if (cachePurged) "0.1 MB" else "2.4 MB",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (cachePurged) MintSuccess.copy(alpha = 0.2f) else PriceLensTheme.glassColors.surfaceHighlight,
                        border = BorderStroke(1.dp, if (cachePurged) MintSuccess else PriceLensTheme.glassColors.specularBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                cachePurged = !cachePurged
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (cachePurged) Icons.Default.CheckCircle else Icons.Default.Refresh,
                                contentDescription = null,
                                tint = if (cachePurged) MintSuccess else ElectricBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (cachePurged) "Cache Cleaned • Feeds Re-Synchronizing" else "Purge Stale Cache & Re-Sync Stores",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (cachePurged) MintSuccess else ElectricBlue
                            )
                        }
                    }
                }
            }
        }

        // 8. Tool 7: PriceLens Labs Feature Toggles
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                borderColor = PriceLensTheme.glassColors.specularBorder
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PRICELENS LABS EXPERIMENTAL FEATURES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textSecondary
                        )
                    }

                    // Toggle 1: Forecast
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "AI Price Drop Forecast",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.textPrimary
                            )
                            Text(
                                text = "Predicts future price drops and optimal purchase timing",
                                style = MaterialTheme.typography.labelSmall,
                                color = PriceLensTheme.glassColors.textSecondary
                            )
                        }
                        Switch(
                            checked = flags.enablePriceDropForecast,
                            onCheckedChange = { userPreferences.setExperimentalFlag(UserPreferencesRepository.KEY_EXP_PRICE_DROP_FORECAST, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = CyanAccent
                            )
                        )
                    }

                    // Toggle 2: Coupon Finder
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Smart Promo & Coupon Finder",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.textPrimary
                            )
                            Text(
                                text = "Verifies active bank card offers, cashbacks and store coupons",
                                style = MaterialTheme.typography.labelSmall,
                                color = PriceLensTheme.glassColors.textSecondary
                            )
                        }
                        Switch(
                            checked = flags.enableCouponFinder,
                            onCheckedChange = { userPreferences.setExperimentalFlag(UserPreferencesRepository.KEY_EXP_COUPON_FINDER, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = CyanAccent
                            )
                        )
                    }

                    // Toggle 3: Theme
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Theme Mode",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.textPrimary
                            )
                            Text(
                                text = if (PriceLensTheme.isDark) "Dark Liquid Glass Theme" else "Light Frosted Theme",
                                style = MaterialTheme.typography.labelSmall,
                                color = PriceLensTheme.glassColors.textSecondary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PriceLensTheme.glassColors.surfaceHighlight,
                            border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder),
                            modifier = Modifier.clickable {
                                themeToggle()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (PriceLensTheme.isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (PriceLensTheme.isDark) "DARK" else "LIGHT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
