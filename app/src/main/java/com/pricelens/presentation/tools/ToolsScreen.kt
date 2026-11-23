package com.pricelens.presentation.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TouchApp
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
import androidx.compose.ui.graphics.Brush
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
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.ThemeAccent
import com.pricelens.core.designsystem.AppFont
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.designsystem.components.LiquidGlassView
import com.pricelens.core.designsystem.components.liquidGlass
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import com.pricelens.core.preferences.UserPreferencesRepository
import com.pricelens.core.util.rememberInrCurrencyFormatter
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min

@Composable
fun ToolsRoute(
    onNavigateToWebPicker: (url: String, name: String) -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onNavigateToOnboarding: () -> Unit = {},
    userPreferences: UserPreferencesRepository? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val effectivePreferences = userPreferences
        ?: com.pricelens.core.designsystem.LocalUserPreferences.current
        ?: remember { UserPreferencesRepository(context.applicationContext) }

    ToolsScreen(
        userPreferences = effectivePreferences,
        onNavigateToWebPicker = onNavigateToWebPicker,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToOnboarding = onNavigateToOnboarding,
        modifier = modifier
    )
}

@androidx.compose.runtime.Immutable
data class UrlDiagnosticInfo(
    val storeName: String,
    val storeColor: Color,
    val identifierType: String,
    val identifierValue: String?,
    val isSupported: Boolean,
    val diagnosticMessage: String
)

private val AsinRegex = Regex("""(?:/dp/|/gp/product/|/d/)([A-Z0-9]{10})""", RegexOption.IGNORE_CASE)
private val PidRegex = Regex("""pid=([A-Za-z0-9]{16})""", RegexOption.IGNORE_CASE)
private val CromaSkuRegex = Regex("""/p/(\d+)""")
private val RdSkuRegex = Regex("""/p/(\d+)""")

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
            val asin = AsinRegex.find(cleanUrl)?.groupValues?.get(1)
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
            val pid = PidRegex.find(cleanUrl)?.groupValues?.get(1)
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
            val cromaSku = CromaSkuRegex.find(cleanUrl)?.groupValues?.get(1)
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
            val rdSku = RdSkuRegex.find(cleanUrl)?.groupValues?.get(1)
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
    onNavigateToOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currencyFormatter = rememberInrCurrencyFormatter()
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

    // Theme state
    val currentThemeMode by userPreferences.themeModeFlow.collectAsState()
    val currentThemeAccent by userPreferences.themeAccentFlow.collectAsState()
    val currentGlowIntensity by userPreferences.glowIntensityFlow.collectAsState()
    val currentContrastStyle by userPreferences.contrastStyleFlow.collectAsState()
    val currentGlassRefraction by userPreferences.glassRefractionFlow.collectAsState()
    val currentGlassDispersion by userPreferences.glassDispersionFlow.collectAsState()
    val isIosSwitchEnabled by userPreferences.iosSwitchFlow.collectAsState()
    val currentAppFont by userPreferences.appFontFlow.collectAsState()

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
                            .background(PriceLensTheme.glassColors.ambientGlow)
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
                    text = "Theme Studio, Live Web Inspector, Store Diagnostics, EMI Planner & PriceLens Labs",
                    style = MaterialTheme.typography.labelSmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )
            }
        }

        // Interactive Intro & Feature Walkthrough Replay
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNavigateToOnboarding()
                }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = PriceLensTheme.glassColors.ambientGlow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Interactive App Tour",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textPrimary
                        )
                        Text(
                            text = "Replay the interactive price comparisons, tap-to-inspect sandbox & alerts demo",
                            style = MaterialTheme.typography.bodySmall,
                            color = PriceLensTheme.glassColors.textSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = PriceLensTheme.glassColors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 2. Tool 1: Theme & Appearance Studio
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                borderColor = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.45f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = PriceLensTheme.glassColors.ambientGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Theme & Appearance Studio",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                                Text(
                                    text = "Personalize colors, mode & liquid glass glow",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PriceLensTheme.glassColors.textSecondary
                                )
                            }
                        }

                        // Header actions (Shuffle & Current Accent Badge)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = PriceLensTheme.glassColors.surfaceHighlight,
                                border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(99.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        val otherAccents = ThemeAccent.entries.filter { it != currentThemeAccent }
                                        userPreferences.setThemeAccent(otherAccents.random())
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = "Shuffle Accent",
                                        tint = PriceLensTheme.glassColors.ambientGlow,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "SHUFFLE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = PriceLensTheme.glassColors.ambientGlow
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(PriceLensTheme.glassColors.ambientGlow)
                                    )
                                    Text(
                                        text = currentThemeAccent.displayName.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PriceLensTheme.glassColors.ambientGlow
                                    )
                                }
                            }
                        }
                    }

                    // Liquid Glass Engine Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(PriceLensTheme.glassColors.ambientGlow)
                            )
                            Text(
                                text = "Liquid Glass Engine",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.ambientGlow
                            )
                            Text(
                                text = "• Physically-based Refraction & Dispersion",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = PriceLensTheme.glassColors.textSecondary
                            )
                        }
                    }

                    // Interactive Liquid Glass Lens Preview Playground
                    val playgroundAccent = PriceLensTheme.glassColors.ambientGlow
                    val playgroundSecondary = PriceLensTheme.glassColors.accentSecondary
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .drawBehind {
                                // Dynamic colorful backdrop pattern behind glass
                                drawRect(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            playgroundAccent.copy(alpha = 0.35f),
                                            playgroundSecondary.copy(alpha = 0.30f),
                                            Color(0xFF38BDF8).copy(alpha = 0.25f),
                                            Color(0xFFA855F7).copy(alpha = 0.25f)
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, size.height)
                                    )
                                )
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(playgroundAccent.copy(alpha = 0.45f), Color.Transparent)
                                    ),
                                    center = Offset(size.width * 0.25f, size.height * 0.5f),
                                    radius = size.height * 0.8f
                                )
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(playgroundSecondary.copy(alpha = 0.40f), Color.Transparent)
                                    ),
                                    center = Offset(size.width * 0.75f, size.height * 0.5f),
                                    radius = size.height * 0.7f
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .height(85.dp)
                                .liquidGlass(
                                    shape = RoundedCornerShape(16.dp),
                                    cornerRadius = 16.dp,
                                    showTopGlint = true,
                                    interactiveTouch = true
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(playgroundAccent)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Interactive Optical Lens",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PriceLensTheme.glassColors.textPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Touch and drag to experience reactive ray refraction",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                    color = PriceLensTheme.glassColors.textSecondary
                                )
                            }
                        }
                    }

                    // Optical Refraction Curve
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "OPTICAL REFRACTION STRENGTH",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textTertiary,
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UserPreferencesRepository.GlassRefraction.entries.forEach { refraction ->
                                val isSelected = currentGlassRefraction == refraction
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) {
                                        PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.18f)
                                    } else {
                                        PriceLensTheme.glassColors.surfaceHighlight
                                    },
                                    border = BorderStroke(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            userPreferences.setGlassRefraction(refraction)
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = refraction.displayName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${(refraction.multiplier * 100).toInt()}% lens",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                            color = if (isSelected) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.8f) else PriceLensTheme.glassColors.textTertiary,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Chromatic Dispersion (Prism Fringes)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "CHROMATIC DISPERSION (PRISM)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textTertiary,
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UserPreferencesRepository.GlassDispersion.entries.forEach { dispersion ->
                                val isSelected = currentGlassDispersion == dispersion
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) {
                                        PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.18f)
                                    } else {
                                        PriceLensTheme.glassColors.surfaceHighlight
                                    },
                                    border = BorderStroke(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            userPreferences.setGlassDispersion(dispersion)
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = dispersion.displayName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = dispersion.subtitle,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp),
                                            color = if (isSelected) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.8f) else PriceLensTheme.glassColors.textTertiary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ============================================================
                    // App Typography & Font Selection
                    // ============================================================
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "APP TYPOGRAPHY & FONT",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.textTertiary,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = currentAppFont.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = PriceLensTheme.glassColors.ambientGlow
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppFont.entries.forEach { fontOption ->
                                val isSelected = currentAppFont == fontOption
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) {
                                        PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.18f)
                                    } else {
                                        PriceLensTheme.glassColors.surfaceHighlight
                                    },
                                    border = BorderStroke(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder
                                    ),
                                    modifier = Modifier
                                        .width(112.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            userPreferences.setAppFont(fontOption)
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                        horizontalAlignment = Alignment.Start,
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = fontOption.samplePreview,
                                            fontFamily = fontOption.getFontFamily(),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textPrimary
                                        )
                                        Text(
                                            text = fontOption.displayName,
                                            fontFamily = fontOption.getFontFamily(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = fontOption.subtitle,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                            color = if (isSelected) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.85f) else PriceLensTheme.glassColors.textTertiary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ============================================================
                    // NEW: iOS Switch (Dedicated Liquid Glass Setting Button/Toggle)
                    // Reference: QmDeve/AndroidLiquidGlassView & Kyant0/AndroidLiquidGlass
                    // ============================================================
                    LiquidGlassView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                userPreferences.setIosSwitchEnabled(!isIosSwitchEnabled)
                            },
                        cornerRadius = 18.dp,
                        refractionHeight = 24.dp,
                        refractionOffset = 12.dp,
                        dispersion = 0.25f,
                        blurRadius = 16.dp,
                        tintColor = if (isIosSwitchEnabled) {
                            Color(0xFF007AFF).copy(alpha = if (PriceLensTheme.isDark) 0.22f else 0.12f)
                        } else {
                            PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.6f)
                        },
                        borderWidth = if (isIosSwitchEnabled) 1.5.dp else 1.dp,
                        borderColor = if (isIosSwitchEnabled) Color(0xFF0A84FF) else PriceLensTheme.glassColors.specularBorder,
                        isInteractive = true
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isIosSwitchEnabled) {
                                                    Brush.linearGradient(listOf(Color(0xFF0A84FF), Color(0xFF5E5CE6)))
                                                } else {
                                                    Brush.linearGradient(listOf(Color(0xFF3A3A3C), Color(0xFF2C2C2E)))
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = if (isIosSwitchEnabled) Color.White else PriceLensTheme.glassColors.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "iOS Switch",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isIosSwitchEnabled) Color(0xFF0A84FF) else PriceLensTheme.glassColors.textPrimary
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isIosSwitchEnabled) Color(0xFF007AFF) else PriceLensTheme.glassColors.textTertiary.copy(alpha = 0.25f)
                                            ) {
                                                Text(
                                                    text = if (isIosSwitchEnabled) "ON" else "OFF",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isIosSwitchEnabled) Color.White else PriceLensTheme.glassColors.textSecondary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = if (isIosSwitchEnabled) {
                                                "iOS-inspired Liquid Glass active across the entire application"
                                            } else {
                                                "Enable iOS-inspired Liquid Glass design (translucency, refraction & blur)"
                                            },
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                            color = PriceLensTheme.glassColors.textSecondary
                                        )
                                    }
                                }

                                Switch(
                                    checked = isIosSwitchEnabled,
                                    onCheckedChange = { checked ->
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        userPreferences.setIosSwitchEnabled(checked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF007AFF),
                                        uncheckedThumbColor = PriceLensTheme.glassColors.textSecondary,
                                        uncheckedTrackColor = PriceLensTheme.glassColors.surfaceHighlight
                                    )
                                )
                            }

                            // Technical & visual reference tags
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isIosSwitchEnabled) Color(0xFF007AFF).copy(alpha = 0.15f) else PriceLensTheme.glassColors.textTertiary.copy(alpha = 0.10f)
                                ) {
                                    Text(
                                        text = "AndroidLiquidGlassView",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        color = if (isIosSwitchEnabled) Color(0xFF0A84FF) else PriceLensTheme.glassColors.textTertiary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isIosSwitchEnabled) Color(0xFF5E5CE6).copy(alpha = 0.15f) else PriceLensTheme.glassColors.textTertiary.copy(alpha = 0.10f)
                                ) {
                                    Text(
                                        text = "Backdrop Optical Shaders",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        color = if (isIosSwitchEnabled) Color(0xFF5E5CE6) else PriceLensTheme.glassColors.textTertiary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Apple Experience Feature Banner / 1-Tap Switcher
                    val isAppleActive = currentContrastStyle == UserPreferencesRepository.ContrastStyle.APPLE_CUPERTINO &&
                        currentThemeAccent == ThemeAccent.CUPERTINO_BLUE

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAppleActive) {
                            Color(0xFF007AFF).copy(alpha = if (PriceLensTheme.isDark) 0.18f else 0.10f)
                        } else {
                            PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.5f)
                        },
                        border = BorderStroke(
                            if (isAppleActive) 1.5.dp else 1.dp,
                            if (isAppleActive) Color(0xFF0A84FF) else PriceLensTheme.glassColors.specularBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (isAppleActive) {
                                    userPreferences.setThemeAccent(ThemeAccent.EMERALD_MINT)
                                    userPreferences.setContrastStyle(UserPreferencesRepository.ContrastStyle.PITCH_BLACK)
                                    userPreferences.setGlowIntensity(UserPreferencesRepository.GlowIntensity.VIVID)
                                    userPreferences.setGlassRefraction(UserPreferencesRepository.GlassRefraction.STANDARD)
                                } else {
                                    userPreferences.setThemeAccent(ThemeAccent.CUPERTINO_BLUE)
                                    userPreferences.setContrastStyle(UserPreferencesRepository.ContrastStyle.APPLE_CUPERTINO)
                                    userPreferences.setGlowIntensity(UserPreferencesRepository.GlowIntensity.SUBTLE)
                                    userPreferences.setGlassRefraction(UserPreferencesRepository.GlassRefraction.SUBTLE)
                                    userPreferences.setGlassDispersion(UserPreferencesRepository.GlassDispersion.DELICATE)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF0A84FF), Color(0xFF5E5CE6))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Apple Cupertino Theme",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAppleActive) Color(0xFF0A84FF) else PriceLensTheme.glassColors.textPrimary
                                        )
                                        if (isAppleActive) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF007AFF),
                                                modifier = Modifier.padding(start = 2.dp)
                                            ) {
                                                Text(
                                                    text = "ACTIVE",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = if (isAppleActive) "Authentic iOS 18 & iPadOS Dock active" else "Tap to switch to 1:1 Apple HIG design",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = PriceLensTheme.glassColors.textSecondary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isAppleActive) Color(0xFF007AFF).copy(alpha = 0.2f) else PriceLensTheme.glassColors.surfaceHighlight,
                                border = BorderStroke(1.dp, if (isAppleActive) Color(0xFF0A84FF) else PriceLensTheme.glassColors.specularBorder)
                            ) {
                                Text(
                                    text = if (isAppleActive) "Reset" else "Apply",
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAppleActive) Color(0xFF0A84FF) else PriceLensTheme.glassColors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // A. Mode Selector (Dark / Light / System)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "DISPLAY MODE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textTertiary,
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val modes = listOf(
                                Triple(UserPreferencesRepository.ThemeMode.DARK, "Dark", Icons.Default.DarkMode),
                                Triple(UserPreferencesRepository.ThemeMode.LIGHT, "Light", Icons.Default.LightMode),
                                Triple(UserPreferencesRepository.ThemeMode.SYSTEM, "System", Icons.Default.SettingsBrightness)
                            )

                            modes.forEach { (mode, label, icon) ->
                                val isSelected = currentThemeMode == mode
                                val isCupertino = PriceLensTheme.isCupertino
                                val isDark = PriceLensTheme.isDark

                                val itemBg = if (isSelected) {
                                    if (isCupertino) {
                                        if (isDark) Color(0xFF636366) else Color.White
                                    } else {
                                        PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.18f)
                                    }
                                } else {
                                    if (isCupertino) {
                                        if (isDark) Color(0xFF1C1C1E) else Color(0xFFE5E5EA)
                                    } else {
                                        PriceLensTheme.glassColors.surfaceHighlight
                                    }
                                }

                                val itemBorder = if (isCupertino) {
                                    if (isSelected && !isDark) BorderStroke(0.5.dp, Color(0x1A000000)) else BorderStroke(0.dp, Color.Transparent)
                                } else {
                                    BorderStroke(1.dp, if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder)
                                }

                                val itemTextColor = if (isSelected) {
                                    if (isCupertino) {
                                        if (isDark) Color.White else Color.Black
                                    } else {
                                        PriceLensTheme.glassColors.ambientGlow
                                    }
                                } else {
                                    PriceLensTheme.glassColors.textSecondary
                                }

                                Surface(
                                    shape = RoundedCornerShape(if (isCupertino) 9.dp else 12.dp),
                                    color = itemBg,
                                    border = itemBorder,
                                    shadowElevation = if (isCupertino && isSelected && !isDark) 2.dp else 0.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(if (isCupertino) 9.dp else 12.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            userPreferences.setThemeMode(mode)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = itemTextColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = itemTextColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // B. Canvas Contrast Style (AMOLED Pitch vs Deep Obsidian)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "CANVAS CONTRAST STYLE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textTertiary,
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val contrastStyles = listOf(
                                Triple(
                                    UserPreferencesRepository.ContrastStyle.PITCH_BLACK,
                                    "AMOLED",
                                    "Pure pitch"
                                ),
                                Triple(
                                    UserPreferencesRepository.ContrastStyle.OBSIDIAN_TINT,
                                    "Obsidian",
                                    "Aura glow"
                                ),
                                Triple(
                                    UserPreferencesRepository.ContrastStyle.APPLE_CUPERTINO,
                                    "Apple",
                                    "iOS / visionOS"
                                )
                            )

                            contrastStyles.forEach { (style, title, subtitle) ->
                                val isSelected = currentContrastStyle == style
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) {
                                        PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.16f)
                                    } else {
                                        PriceLensTheme.glassColors.surfaceHighlight
                                    },
                                    border = BorderStroke(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            userPreferences.setContrastStyle(style)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (style) {
                                                        UserPreferencesRepository.ContrastStyle.PITCH_BLACK -> Color.Black
                                                        UserPreferencesRepository.ContrastStyle.OBSIDIAN_TINT -> Color(0xFF0D131F)
                                                        UserPreferencesRepository.ContrastStyle.APPLE_CUPERTINO -> Color(0xFF007AFF)
                                                    }
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) PriceLensTheme.glassColors.ambientGlow else Color.White.copy(alpha = 0.25f),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(PriceLensTheme.glassColors.ambientGlow)
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textPrimary
                                            )
                                            Text(
                                                text = subtitle,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                                color = PriceLensTheme.glassColors.textSecondary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // C. Glow Radiance Intensity
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "GLOW RADIANCE INTENSITY",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textTertiary,
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UserPreferencesRepository.GlowIntensity.entries.forEach { intensity ->
                                val isSelected = currentGlowIntensity == intensity
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) {
                                        PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.18f)
                                    } else {
                                        PriceLensTheme.glassColors.surfaceHighlight
                                    },
                                    border = BorderStroke(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            userPreferences.setGlowIntensity(intensity)
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = intensity.displayName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${(intensity.multiplier * 100).toInt()}% glow",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                            color = if (isSelected) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.8f) else PriceLensTheme.glassColors.textTertiary,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // D. Accent Palette Swatches
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACCENT COLOR THEMES",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.textTertiary,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "${ThemeAccent.entries.size} Curated Palettes",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = PriceLensTheme.glassColors.textSecondary
                            )
                        }

                        // 2 rows of 5 theme cards
                        val rows = ThemeAccent.entries.chunked(5)
                        rows.forEach { rowAccents ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                rowAccents.forEach { accent ->
                                    val isSelected = currentThemeAccent == accent
                                    val accentColor = if (PriceLensTheme.isDark) accent.accentColor else accent.lightAccentColor

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) {
                                            accentColor.copy(alpha = if (PriceLensTheme.isDark) 0.18f else 0.12f)
                                        } else {
                                            PriceLensTheme.glassColors.surfaceHighlight
                                        },
                                        border = BorderStroke(
                                            if (isSelected) 1.5.dp else 1.dp,
                                            if (isSelected) accentColor else PriceLensTheme.glassColors.specularBorder
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                userPreferences.setThemeAccent(accent)
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            Brush.linearGradient(accent.previewGradient)
                                                        )
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = accent.displayName,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) accentColor else PriceLensTheme.glassColors.textPrimary,
                                                maxLines = 1
                                            )

                                            Text(
                                                text = accent.subtitle,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                                                color = PriceLensTheme.glassColors.textTertiary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Theme Character Description Banner
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(PriceLensTheme.accentGradient),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${currentThemeAccent.displayName} Character",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = PriceLensTheme.glassColors.ambientGlow
                                    )
                                    Text(
                                        text = currentThemeAccent.characterDescription,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = PriceLensTheme.glassColors.textSecondary,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    // C. Live Theme Sandbox Preview
                    var testPulseActive by remember { mutableStateOf(false) }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, if (testPulseActive) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = PriceLensTheme.glassColors.ambientGlow,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "LIVE THEME PREVIEW",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = PriceLensTheme.glassColors.textSecondary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "35% OFF DROP",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PriceLensTheme.glassColors.ambientGlow
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "Sony WH-1000XM5 Wireless Headphones",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PriceLensTheme.glassColors.textPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Lowest price across Amazon & Flipkart",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = PriceLensTheme.glassColors.textSecondary
                                    )
                                }

                                Text(
                                    text = "₹24,990",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PriceLensTheme.glassColors.ambientGlow
                                )
                            }

                            // Dynamic Live Accent Bar
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Drop Confidence Score",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = PriceLensTheme.glassColors.textTertiary
                                    )
                                    Text(
                                        text = "94% Verified Deal",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = PriceLensTheme.glassColors.ambientGlow
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(CircleShape)
                                        .background(PriceLensTheme.glassColors.surfaceHighlight)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.94f)
                                            .height(4.dp)
                                            .clip(CircleShape)
                                            .background(PriceLensTheme.accentGradient)
                                    )
                                }
                            }

                            // Interactive Glow Pulse Test Button
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (testPulseActive) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        testPulseActive = !testPulseActive
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 7.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TouchApp,
                                        contentDescription = null,
                                        tint = if (testPulseActive) Color.Black else PriceLensTheme.glassColors.ambientGlow,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (testPulseActive) "✓ Interactive Glow Active • Tap to Reset" else "Tap to Test Interactive Theme Pulse",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (testPulseActive) Color.Black else PriceLensTheme.glassColors.ambientGlow
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Tool 2: Universal In-App Web Price Picker
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                borderColor = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.4f)
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
                                    .background(PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = PriceLensTheme.glassColors.ambientGlow,
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
                            color = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "LIVE BROWSER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.ambientGlow,
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
                                        tint = PriceLensTheme.glassColors.ambientGlow,
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
                                focusedBorderColor = PriceLensTheme.glassColors.ambientGlow,
                                unfocusedBorderColor = PriceLensTheme.glassColors.specularBorder,
                                focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                                unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PriceLensTheme.glassColors.ambientGlow,
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
                                    color = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "READY FOR COMPARISON",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = PriceLensTheme.glassColors.ambientGlow,
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
                                color = if (isSelected) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.2f) else PriceLensTheme.glassColors.surfaceHighlight,
                                border = BorderStroke(1.dp, if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder),
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
                                        color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    // EMI Output Summary Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PriceLensTheme.glassColors.surfaceHighlight,
                        border = BorderStroke(1.dp, PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.45f)),
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
                                        color = PriceLensTheme.glassColors.ambientGlow
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${currencyFormatter.format(monthlyInstallment)}/mo",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PriceLensTheme.glassColors.ambientGlow
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
                                .background(PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = PriceLensTheme.glassColors.ambientGlow,
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
                                color = if (isSelected) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.2f) else PriceLensTheme.glassColors.surfaceHighlight,
                                border = BorderStroke(1.dp, if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder),
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
                                        color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textPrimary
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
                                color = if (isSelected) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.2f) else PriceLensTheme.glassColors.surfaceHighlight,
                                border = BorderStroke(1.dp, if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder),
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
                                        color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Calculation Result Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PriceLensTheme.glassColors.surfaceHighlight,
                        border = BorderStroke(1.dp, PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.4f)),
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
                                    color = PriceLensTheme.glassColors.ambientGlow
                                )
                            }
                            Text(
                                text = currencyFormatter.format(calculatedTarget),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = PriceLensTheme.glassColors.ambientGlow
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
                                color = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = status,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = PriceLensTheme.glassColors.ambientGlow,
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
                            color = PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (cachePurged) "PURGED & CLEAN" else "OPTIMAL",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.ambientGlow,
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
                        color = if (cachePurged) PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.2f) else PriceLensTheme.glassColors.surfaceHighlight,
                        border = BorderStroke(1.dp, if (cachePurged) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder),
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
                                tint = if (cachePurged) PriceLensTheme.glassColors.ambientGlow else ElectricBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (cachePurged) "Cache Cleaned • Feeds Re-Synchronizing" else "Purge Stale Cache & Re-Sync Stores",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (cachePurged) PriceLensTheme.glassColors.ambientGlow else ElectricBlue
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
                            tint = PriceLensTheme.glassColors.ambientGlow,
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

                    // Toggle 0: iOS Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "iOS Switch",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.textPrimary
                            )
                            Text(
                                text = "Transforms entire app into iOS Liquid Glass with frosted blur & optical depth",
                                style = MaterialTheme.typography.labelSmall,
                                color = PriceLensTheme.glassColors.textSecondary
                            )
                        }
                        Switch(
                            checked = isIosSwitchEnabled,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                userPreferences.setIosSwitchEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF007AFF)
                            )
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
                                checkedTrackColor = PriceLensTheme.glassColors.ambientGlow
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
                                checkedTrackColor = PriceLensTheme.glassColors.ambientGlow
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
                                text = "Theme Mode & Palette",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.textPrimary
                            )
                            Text(
                                text = "${currentThemeAccent.displayName} • ${if (PriceLensTheme.isDark) "Dark Liquid Glass" else "Light Frosted"}",
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
                                    tint = PriceLensTheme.glassColors.ambientGlow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (PriceLensTheme.isDark) "DARK" else "LIGHT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PriceLensTheme.glassColors.ambientGlow
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
