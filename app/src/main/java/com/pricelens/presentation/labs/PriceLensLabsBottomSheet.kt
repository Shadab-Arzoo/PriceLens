package com.pricelens.presentation.labs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pricelens.core.designsystem.BrandMint
import com.pricelens.core.designsystem.ElectricBlue
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.preferences.UserPreferencesRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceLensLabsBottomSheet(
    flags: UserPreferencesRepository.ExperimentalFlags,
    onToggleMaster: (Boolean) -> Unit,
    onToggleFlag: (String, Boolean) -> Unit,
    onTriggerPriceSync: () -> Unit,
    onTriggerTestNotification: () -> Unit,
    onExportWatchlist: () -> Unit,
    onImportWatchlist: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = PriceLensTheme.isDark
    val glow = PriceLensTheme.glassColors.ambientGlow
    val iconBg = if (isDark) BrandMint.copy(alpha = 0.15f) else Color(0xFFE6F4EA)
    val iconBorder = if (isDark) BrandMint.copy(alpha = 0.35f) else Color(0xFFCEEAD6)

    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var actionStatusMessage by remember { mutableStateOf<String?>(null) }
    var isSyncing by remember { mutableStateOf(false) }
    var isAlerting by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val syncRotation by animateFloatAsState(
        targetValue = if (isSyncing) 360f else 0f,
        animationSpec = if (isSyncing) tween(700, easing = LinearEasing) else spring(),
        finishedListener = { isSyncing = false },
        label = "syncRotation"
    )

    val alertWiggle by animateFloatAsState(
        targetValue = if (isAlerting) 15f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        finishedListener = { isAlerting = false },
        label = "alertWiggle"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PriceLensTheme.glassColors.surfaceGlass,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = PriceLensTheme.glassColors.specularBorder) }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(iconBg)
                            .border(1.dp, iconBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "PriceLens Labs",
                            tint = glow,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PriceLens Labs",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = glow.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "EXPERIMENTAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = glow,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Next-generation shopping intelligence & tools",
                            style = MaterialTheme.typography.bodySmall,
                            color = PriceLensTheme.glassColors.textSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = PriceLensTheme.glassColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Master Labs Switch Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (flags.isMasterEnabled) glow.copy(alpha = 0.5f) else PriceLensTheme.glassColors.specularBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Enable Experimental Features",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textPrimary
                        )
                        Text(
                            text = "Turn on experimental algorithms, fake discount detection, and bank offer calculators.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PriceLensTheme.glassColors.textSecondary
                        )
                    }

                    Switch(
                        checked = flags.isMasterEnabled,
                        onCheckedChange = onToggleMaster,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = glow,
                            uncheckedThumbColor = PriceLensTheme.glassColors.textSecondary,
                            uncheckedTrackColor = PriceLensTheme.glassColors.surfaceHighlight
                        )
                    )
                }
            }

            // Status message
            AnimatedVisibility(visible = actionStatusMessage != null) {
                actionStatusMessage?.let { msg ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = glow.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, glow.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = glow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = PriceLensTheme.glassColors.textPrimary
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = flags.isMasterEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "TOOLS & DIAGNOSTICS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = glow
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                isSyncing = true
                                onTriggerPriceSync()
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                actionStatusMessage = "⚡ Scanning live prices for all tracked items in background!"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = PriceLensTheme.glassColors.surfaceHighlight
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(16.dp)
                                    .graphicsLayer(rotationZ = syncRotation)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Sync Prices", style = MaterialTheme.typography.labelMedium)
                        }

                        FilledTonalButton(
                            onClick = {
                                isAlerting = true
                                onTriggerTestNotification()
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                actionStatusMessage = "🔔 Test price-drop notification dispatched!"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = PriceLensTheme.glassColors.surfaceHighlight
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(16.dp)
                                    .graphicsLayer(rotationZ = alertWiggle)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Test Alert", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Watchlist Backup Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onExportWatchlist()
                                actionStatusMessage = "📤 Watchlist JSON copied to clipboard & shared!"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder)
                        ) {
                            Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Export JSON", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { showImportDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Import JSON", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    if (showImportDialog) {
                        Spacer(modifier = Modifier.height(10.dp))
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Import Watchlist JSON",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = importJsonText,
                                    onValueChange = { importJsonText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Paste exported JSON here...") },
                                    maxLines = 4,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                                        unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(onClick = { showImportDialog = false }) {
                                        Text("Cancel")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FilledTonalButton(onClick = {
                                        if (importJsonText.isNotBlank()) {
                                            onImportWatchlist(importJsonText)
                                            actionStatusMessage = "📥 Watchlist items imported successfully!"
                                            showImportDialog = false
                                            importJsonText = ""
                                        }
                                    }) {
                                        Text("Import")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "ACTIVE LABS EXPERIMENTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = glow
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. Fake Discount Detector
                    FeatureToggleCard(
                        icon = Icons.Default.Verified,
                        title = "Deal Authenticity & Fake Discount Detector",
                        description = "Calculates true discounts vs 30-day historical medians to unmask artificially inflated sale MRPs.",
                        enabled = flags.enableDealAuthenticity,
                        onToggle = { onToggleFlag(UserPreferencesRepository.KEY_EXP_DEAL_AUTHENTICITY, it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Buy Now Verdict
                    FeatureToggleCard(
                        icon = Icons.Default.PriceCheck,
                        title = "\"Should I Buy Now?\" Decision Verdict",
                        description = "Instant Green/Yellow/Red verdict based on recent price movements and all-time low comparisons.",
                        enabled = flags.enableBuyNowVerdict,
                        onToggle = { onToggleFlag(UserPreferencesRepository.KEY_EXP_BUY_NOW_VERDICT, it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Bank Card & Offers Calculator
                    FeatureToggleCard(
                        icon = Icons.Default.CreditCard,
                        title = "Bank Card & Cashback Simulator",
                        description = "Simulate 10% instant discounts (HDFC/ICICI/SBI) & 5% cashback to calculate true net effective prices.",
                        enabled = flags.enableBankCardCalculator,
                        onToggle = { onToggleFlag(UserPreferencesRepository.KEY_EXP_BANK_CARD, it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4. Alert Presets
                    FeatureToggleCard(
                        icon = Icons.Default.NotificationsActive,
                        title = "Instant Target Price Presets",
                        description = "One-tap -5%, -10%, All-Time Low, and Any Drop shortcut chips when setting target alerts.",
                        enabled = flags.enableAlertPresets,
                        onToggle = { onToggleFlag(UserPreferencesRepository.KEY_EXP_ALERT_PRESETS, it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 5. Dynamic Universal Search
                    FeatureToggleCard(
                        icon = Icons.Default.Search,
                        title = "Dynamic Universal Search Engine",
                        description = "Dynamically searches and compares any product on Earth across retailers, beyond static catalog items.",
                        enabled = flags.enableDynamicSearch,
                        onToggle = { onToggleFlag(UserPreferencesRepository.KEY_EXP_DYNAMIC_SEARCH, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureToggleCard(
    icon: ImageVector,
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val glow = PriceLensTheme.glassColors.ambientGlow
    val haptic = LocalHapticFeedback.current

    val borderColor by animateColorAsState(
        targetValue = if (enabled) glow.copy(alpha = 0.5f) else PriceLensTheme.glassColors.specularBorder,
        animationSpec = spring(),
        label = "featureBorder"
    )

    val iconBg by animateColorAsState(
        targetValue = if (enabled) glow.copy(alpha = 0.15f) else PriceLensTheme.glassColors.surfaceHighlight,
        animationSpec = spring(),
        label = "featureIconBg"
    )

    val iconTint by animateColorAsState(
        targetValue = if (enabled) glow else PriceLensTheme.glassColors.textSecondary,
        animationSpec = spring(),
        label = "featureIconTint"
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggle(!enabled)
            },
        borderColor = borderColor,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconBg)
                    .border(
                        1.dp,
                        if (enabled) glow.copy(alpha = 0.35f) else Color.Transparent,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )
            }

            Switch(
                checked = enabled,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggle(it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = glow,
                    uncheckedThumbColor = PriceLensTheme.glassColors.textSecondary,
                    uncheckedTrackColor = PriceLensTheme.glassColors.surfaceHighlight
                )
            )
        }
    }
}
