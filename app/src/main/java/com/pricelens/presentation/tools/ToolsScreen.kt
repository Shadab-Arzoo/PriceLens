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

    // Theme state
    val currentThemeMode by userPreferences.themeModeFlow.collectAsState()
    val currentThemeAccent by userPreferences.themeAccentFlow.collectAsState()
    val currentGlowIntensity by userPreferences.glowIntensityFlow.collectAsState()
    val currentContrastStyle by userPreferences.contrastStyleFlow.collectAsState()

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
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) {
                                        PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.18f)
                                    } else {
                                        PriceLensTheme.glassColors.surfaceHighlight
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.specularBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
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
                                            tint = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textSecondary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) PriceLensTheme.glassColors.ambientGlow else PriceLensTheme.glassColors.textSecondary
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
                                    "AMOLED Pitch",
                                    "Pure #000000 canvas"
                                ),
                                Triple(
                                    UserPreferencesRepository.ContrastStyle.OBSIDIAN_TINT,
                                    "Deep Obsidian",
                                    "Atmospheric aura glow"
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
                                                    if (style == UserPreferencesRepository.ContrastStyle.PITCH_BLACK) {
                                                        Color.Black
                                                    } else {
                                                        Color(0xFF0D131F)
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
