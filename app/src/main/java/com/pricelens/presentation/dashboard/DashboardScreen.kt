package com.pricelens.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pricelens.R
import com.pricelens.core.designsystem.BrandMint
import com.pricelens.core.designsystem.ElectricBlue
import com.pricelens.core.designsystem.MintSuccess
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.DashboardSkeleton
import com.pricelens.core.designsystem.components.EmptyStateCard
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.designsystem.components.LiquidGlassPrimaryButton
import com.pricelens.core.designsystem.components.PriceLensSearchBar
import com.pricelens.core.designsystem.components.PriceLensTopBar
import com.pricelens.domain.model.RecentSearch
import com.pricelens.domain.model.WatchlistItem
import com.pricelens.presentation.pasteurl.PasteUrlBottomSheet
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel,
    onNavigateToSearch: (String) -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToComparison: (productId: String, productName: String) -> Unit = { _, _ -> },
    onNavigateToWebPicker: (url: String, name: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToWatchlist = onNavigateToWatchlist,
        onNavigateToComparison = onNavigateToComparison,
        onNavigateToWebPicker = onNavigateToWebPicker,
        modifier = modifier
    )
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToComparison: (productId: String, productName: String) -> Unit = { _, _ -> },
    onNavigateToWebPicker: (url: String, name: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var dismissedClipboardUrl by rememberSaveable { mutableStateOf<String?>(null) }
    val clipboardText = remember(clipboardManager) {
        clipboardManager.getText()?.text?.trim().orEmpty()
    }
    val detectedClipboardUrl = remember(clipboardText) {
        val regex = Regex("""https?://[^\s<>"{}|\\^`]+""", RegexOption.IGNORE_CASE)
        val foundUrl = regex.find(clipboardText)?.value
            ?: if (clipboardText.contains(".") && !clipboardText.contains(" ")) clipboardText else null
        if (foundUrl != null) {
            val lower = foundUrl.lowercase()
            val isSupported = lower.contains("amazon.") || lower.contains("amzn.") || lower.contains("amzn.to") ||
                lower.contains("flipkart.") || lower.contains("fkrt.it") || lower.contains("myntra.") ||
                lower.contains("ajio.") || lower.contains("croma.") ||
                lower.contains("tatacliq.") || lower.contains("jiomart.")
            if (isSupported) foundUrl else null
        } else null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(brush = PriceLensTheme.glassColors.backgroundBrush)
    ) {
        // 1. Pinned Top Bar with status bar protection
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = PriceLensTheme.glassColors.surfaceGlass,
            border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder)
        ) {
            PriceLensTopBar(
                title = stringResource(R.string.dashboard_title),
                subtitle = "",
                watchlistCount = uiState.watchlistItems.size,
                onWatchlistClick = onNavigateToWatchlist,
                onLabsClick = { onEvent(DashboardUiEvent.OpenLabsSheet) },
                isLabsActive = uiState.experimentalFlags.isMasterEnabled
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // 2. Search Input Bar
            item {
                PriceLensSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { onEvent(DashboardUiEvent.SearchQueryChanged(it)) },
                    onSearch = { query ->
                        if (query.isNotBlank()) {
                            onEvent(DashboardUiEvent.SubmitSearch(query))
                            onNavigateToSearch(query)
                        }
                    }
                )
            }

            // 2b. Auto-Detected Clipboard URL Banner
            if (detectedClipboardUrl != null && detectedClipboardUrl != dismissedClipboardUrl) {
                item(key = "clipboard_detected_banner") {
                    ClipboardDetectedBanner(
                        url = detectedClipboardUrl,
                        onCompare = {
                            onEvent(DashboardUiEvent.OpenPasteUrlSheet)
                            onEvent(DashboardUiEvent.PasteUrlInputChanged(detectedClipboardUrl))
                        },
                        onDismiss = { dismissedClipboardUrl = detectedClipboardUrl }
                    )
                }
            }

            // 3. Hero Action: Compare by Store Link (Direct Link-First Experience)
            item {
                PasteLinkHeroCard(
                    onOpenPasteSheet = { onEvent(DashboardUiEvent.OpenPasteUrlSheet) }
                )
            }

            // 3b. PriceLens Labs Banner
            item {
                PriceLensLabsBanner(
                    isMasterEnabled = uiState.experimentalFlags.isMasterEnabled,
                    onClick = { onEvent(DashboardUiEvent.OpenLabsSheet) }
                )
            }

            // 3c. Live Market Flash Drops Ticker
            item {
                FlashDealsTicker(
                    onProductClick = onNavigateToComparison
                )
            }

            // 4. Loading State with Fluid Skeleton
            if (uiState.isLoading) {
                item {
                    DashboardSkeleton()
                }
            }

            // 5. Error State
            if (uiState.errorMessage != null && !uiState.isLoading) {
                item {
                    DashboardErrorState(
                        errorMessage = uiState.errorMessage,
                        onRetry = { onEvent(DashboardUiEvent.RetryLoad) }
                    )
                }
            }

            // 6. Main Content Sections
            if (!uiState.isLoading && uiState.errorMessage == null) {
                // Section: Recent Searches
                item {
                    RecentSearchesSection(
                        recentSearches = uiState.recentSearches,
                        onSelectSearch = { query ->
                            onEvent(DashboardUiEvent.RecentSearchSelected(query))
                            onNavigateToSearch(query)
                        },
                        onClearSearches = { onEvent(DashboardUiEvent.ClearRecentSearches) }
                    )
                }

                // Section: Watchlist / Tracked Price Alerts
                item {
                    WatchlistSection(
                        watchlistItems = uiState.watchlistItems,
                        onViewWatchlist = onNavigateToWatchlist,
                        onProductClick = onNavigateToComparison
                    )
                }

                // Section: Trending Comparisons
                item {
                    TrendingSection(
                        trendingProducts = uiState.trendingProducts,
                        onProductClick = onNavigateToComparison
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Paste URL Modal Bottom Sheet
        if (uiState.isPasteUrlSheetOpen) {
            PasteUrlBottomSheet(
                urlInput = uiState.pasteUrlInput,
                validationResult = uiState.urlValidation,
                confirmedIdentity = uiState.confirmedIdentity,
                extractedMetadata = uiState.extractedMetadata,
                isExtracting = uiState.isExtractingMetadata,
                onUrlChange = { onEvent(DashboardUiEvent.PasteUrlInputChanged(it)) },
                onIdentityChange = { name, brand, model ->
                    onEvent(DashboardUiEvent.ConfirmedIdentityChanged(name, brand, model))
                },
                onSubmitConfirmedUrl = { confirmed ->
                    onEvent(DashboardUiEvent.ConfirmProductAndNavigate(confirmed))
                    if (!confirmed.resolvedProductId.isNullOrBlank()) {
                        onNavigateToComparison(confirmed.resolvedProductId, confirmed.effectiveTitle)
                    } else {
                        onNavigateToSearch(confirmed.effectiveTitle)
                    }
                },
                onSubmitUrl = {
                    onEvent(DashboardUiEvent.SubmitPastedUrl)
                    if (uiState.urlValidation is com.pricelens.domain.usecase.UrlValidationResult.Supported) {
                        val supported = uiState.urlValidation as com.pricelens.domain.usecase.UrlValidationResult.Supported
                        val identity = supported.suggestedIdentity
                        if (identity != null && !identity.resolvedProductId.isNullOrBlank()) {
                            onEvent(DashboardUiEvent.ClosePasteUrlSheet)
                            onNavigateToComparison(identity.resolvedProductId, identity.effectiveTitle)
                        } else {
                            onEvent(DashboardUiEvent.ClosePasteUrlSheet)
                            onNavigateToSearch(supported.normalizedUrl)
                        }
                    }
                },
                onTrackUniversal = { meta, targetPrice ->
                    onEvent(DashboardUiEvent.TrackUniversalProduct(meta, targetPrice))
                },
                onOpenWebPicker = { url, name ->
                    onEvent(DashboardUiEvent.ClosePasteUrlSheet)
                    onNavigateToWebPicker(url, name)
                },
                onDismiss = { onEvent(DashboardUiEvent.ClosePasteUrlSheet) }
            )
        }

        // PriceLens Labs Bottom Sheet
        if (uiState.isLabsSheetOpen) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
            com.pricelens.presentation.labs.PriceLensLabsBottomSheet(
                flags = uiState.experimentalFlags,
                onToggleMaster = { onEvent(DashboardUiEvent.ToggleLabsMaster(it)) },
                onToggleFlag = { key, enabled -> onEvent(DashboardUiEvent.ToggleLabsFlag(key, enabled)) },
                onTriggerPriceSync = { onEvent(DashboardUiEvent.TriggerPriceSync) },
                onTriggerTestNotification = { onEvent(DashboardUiEvent.TriggerTestNotification) },
                onExportWatchlist = {
                    val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
                    val json = gson.toJson(uiState.watchlistItems)
                    clipboard.setText(androidx.compose.ui.text.AnnotatedString(json))
                    val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        putExtra(android.content.Intent.EXTRA_TEXT, json)
                        type = "application/json"
                    }
                    val shareIntent = android.content.Intent.createChooser(sendIntent, "Export Watchlist JSON")
                    context.startActivity(shareIntent)
                },
                onImportWatchlist = { json ->
                    onEvent(DashboardUiEvent.ImportWatchlist(json))
                },
                onDismiss = { onEvent(DashboardUiEvent.CloseLabsSheet) }
            )
        }
    }
}

@Composable
private fun PriceLensLabsBanner(
    isMasterEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glow = PriceLensTheme.glassColors.ambientGlow
    val isDark = PriceLensTheme.isDark
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bannerPressScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "bannerGlowPulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = if (isMasterEnabled) 0.35f else 0.15f,
        targetValue = if (isMasterEnabled) 0.8f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bannerBorderAlpha"
    )
    val iconTilt by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconTilt"
    )

    com.pricelens.core.designsystem.components.GlassCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        borderColor = if (isMasterEnabled) glow.copy(alpha = borderAlpha) else PriceLensTheme.glassColors.specularBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isDark) glow.copy(alpha = 0.15f) else Color(0xFFE6F4EA))
                        .border(1.dp, glow.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "PriceLens Labs",
                        tint = glow,
                        modifier = Modifier
                            .size(19.dp)
                            .graphicsLayer(rotationZ = if (isMasterEnabled) iconTilt else 0f)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PriceLens Labs",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PriceLensTheme.glassColors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = glow.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isMasterEnabled) "ACTIVE" else "BETA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = glow,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Smart Shopping & AI Tools",
                        style = MaterialTheme.typography.bodySmall,
                        color = PriceLensTheme.glassColors.textSecondary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = PriceLensTheme.glassColors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Premier Hero Card for Pasting Links and Instant Comparison
 */
@Composable
private fun PasteLinkHeroCard(
    onOpenPasteSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        onClick = onOpenPasteSheet,
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (PriceLensTheme.isDark) BrandMint.copy(alpha = 0.15f)
                            else Color(0xFFE6F4EA)
                        )
                        .border(
                            1.dp,
                            if (PriceLensTheme.isDark) BrandMint.copy(alpha = 0.35f)
                            else Color(0xFFCEEAD6),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Paste Product Link",
                        tint = PriceLensTheme.glassColors.ambientGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Paste Store Link to Compare",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                    Text(
                        text = "Amazon, Flipkart, Croma & more",
                        style = MaterialTheme.typography.bodySmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(
                        if (PriceLensTheme.isDark) PriceLensTheme.glassColors.actionFill
                        else Color(0xFF059669)
                    )
                    .border(
                        1.dp,
                        if (PriceLensTheme.isDark) PriceLensTheme.glassColors.specularBorder
                        else Color(0xFF047857),
                        RoundedCornerShape(9999.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "Paste",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun RecentSearchesSection(
    recentSearches: List<RecentSearch>,
    onSelectSearch: (String) -> Unit,
    onClearSearches: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.section_recent_searches),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PriceLensTheme.glassColors.textPrimary
            )

            if (recentSearches.isNotEmpty()) {
                IconButton(onClick = onClearSearches) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear all searches",
                        tint = PriceLensTheme.glassColors.textTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (recentSearches.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.History,
                title = stringResource(R.string.empty_recent_title),
                subtitle = stringResource(R.string.empty_recent_subtitle)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentSearches, key = { it.query }) { search ->
                    GlassCard(
                        shape = RoundedCornerShape(9999.dp),
                        elevation = 1.dp,
                        onClick = { onSelectSearch(search.query) }
                    ) {
                        Text(
                            text = search.query,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = PriceLensTheme.glassColors.textPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClipboardDetectedBanner(
    url: String,
    onCompare: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = PriceLensTheme.isDark
    val glow = PriceLensTheme.glassColors.ambientGlow
    val iconBg = if (isDark) BrandMint.copy(alpha = 0.15f) else Color(0xFFE6F4EA)
    val iconBorder = if (isDark) BrandMint.copy(alpha = 0.35f) else Color(0xFFCEEAD6)
    val bannerBorder = if (isDark) BrandMint.copy(alpha = 0.4f) else Color(0xFFCEEAD6)

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        backgroundColor = PriceLensTheme.glassColors.surfaceGlass,
        borderColor = bannerBorder,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg)
                    .border(1.dp, iconBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Clipboard link",
                    tint = glow,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = "Product link in clipboard",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PriceLensTheme.glassColors.textPrimary
                )
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = PriceLensTheme.glassColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            LiquidGlassPrimaryButton(
                text = "Compare",
                onClick = onCompare,
                fillMaxWidth = false,
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(9999.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = PriceLensTheme.glassColors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun WatchlistSection(
    watchlistItems: List<WatchlistItem>,
    onViewWatchlist: () -> Unit,
    onProductClick: (productId: String, productName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.section_watchlist),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PriceLensTheme.glassColors.textPrimary
            )
            if (watchlistItems.isNotEmpty()) {
                Text(
                    text = "${watchlistItems.size} items",
                    style = MaterialTheme.typography.labelSmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (watchlistItems.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.BookmarkBorder,
                title = stringResource(R.string.empty_watchlist_title),
                subtitle = stringResource(R.string.empty_watchlist_subtitle),
                actionLabel = "Browse Products",
                onActionClick = onViewWatchlist
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                watchlistItems.take(3).forEach { item ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onProductClick(item.productId, item.productName) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.pricelens.core.designsystem.components.ProductThumbnailImage(
                                imageUrl = item.imageUrl,
                                productName = item.productName,
                                modifier = Modifier.size(46.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = item.productName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PriceLensTheme.glassColors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                item.brand?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PriceLensTheme.glassColors.textSecondary
                                    )
                                }
                            }
                            item.currentLowestPrice?.let { price ->
                                Text(
                                    text = "₹%,.0f".format(price),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendingSection(
    trendingProducts: List<com.pricelens.domain.model.ProductSummary>,
    onProductClick: (productId: String, productName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.section_trending),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PriceLensTheme.glassColors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (trendingProducts.isEmpty()) {
            EmptyStateCard(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                title = stringResource(R.string.empty_trending_title),
                subtitle = stringResource(R.string.empty_trending_subtitle)
            )
        } else {
            val currencyFormatter = remember {
                NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
                    maximumFractionDigits = 0
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                trendingProducts.forEach { product ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        onClick = { onProductClick(product.id, product.name) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.pricelens.core.designsystem.components.ProductThumbnailImage(
                                imageUrl = product.imageUrl,
                                productName = product.name,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PriceLensTheme.glassColors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (!product.brand.isNullOrBlank()) {
                                        Text(
                                            text = product.brand,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PriceLensTheme.glassColors.textSecondary
                                        )
                                    }
                                    product.lowestPriceRetailer?.let { retailer ->
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = PriceLensTheme.glassColors.surfaceHighlight
                                        ) {
                                            Text(
                                                text = retailer,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PriceLensTheme.glassColors.textTertiary,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (product.lowestPrice != null) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = currencyFormatter.format(product.lowestPrice),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PriceLensTheme.glassColors.ambientGlow
                                    )
                                    val savings = if (product.highestPrice != null && product.highestPrice > product.lowestPrice) {
                                        product.highestPrice - product.lowestPrice
                                    } else null
                                    if (savings != null && savings > 0) {
                                        Text(
                                            text = "Save ${currencyFormatter.format(savings)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MintSuccess
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        EmptyStateCard(
            icon = Icons.Default.ErrorOutline,
            title = stringResource(R.string.state_error_title),
            subtitle = errorMessage,
            actionLabel = stringResource(R.string.state_error_retry),
            onActionClick = onRetry
        )
    }
}

@Composable
private fun FlashDealsTicker(
    onProductClick: (productId: String, productName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "flashPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flashDotAlpha"
    )

    val flashDeals = remember {
        listOf(
            Triple("prod_iphone_16_128", "Apple iPhone 16 (128GB)", Triple(73999.0, 79900.0, "Flipkart")),
            Triple("prod_samsung_s24", "Samsung Galaxy S24 (128GB)", Triple(62999.0, 79999.0, "Amazon")),
            Triple("prod_macbook_air_m3", "Apple MacBook Air M3", Triple(104990.0, 114900.0, "Amazon")),
            Triple("prod_sony_wh1000xm5", "Sony WH-1000XM5 ANC", Triple(26990.0, 34990.0, "Amazon")),
            Triple("prod_airpods_pro_2", "Apple AirPods Pro 2 USB-C", Triple(20999.0, 24900.0, "Amazon")),
            Triple("prod_oneplus_12", "OnePlus 12 (256GB)", Triple(59999.0, 64999.0, "Amazon"))
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
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
                        .background(MintSuccess.copy(alpha = pulseAlpha))
                )
                Text(
                    text = "Today's Flash Drops",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PriceLensTheme.glassColors.textPrimary
                )
            }

            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = MintSuccess.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, MintSuccess.copy(alpha = 0.35f))
            ) {
                Text(
                    text = "⚡ HOT DEALS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MintSuccess,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(flashDeals, key = { it.first }) { (id, name, pricing) ->
                val (currentPrice, originalPrice, store) = pricing
                val dropAmount = originalPrice - currentPrice
                val dropPercent = ((dropAmount / originalPrice) * 100).toInt()
                val storeColor = when (store) {
                    "Amazon" -> Color(0xFFFF9900)
                    "Flipkart" -> Color(0xFF2874F0)
                    "Croma" -> Color(0xFF00BFA5)
                    else -> Color(0xFFE53935)
                }

                GlassCard(
                    modifier = Modifier
                        .width(220.dp)
                        .animateItem()
                        .clickable {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            onProductClick(id, name)
                        },
                    shape = RoundedCornerShape(16.dp),
                    borderColor = PriceLensTheme.glassColors.specularBorder
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = storeColor.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, storeColor.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(storeColor)
                                    )
                                    Text(
                                        text = store,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = storeColor,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Text(
                                text = "-$dropPercent%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MintSuccess
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            com.pricelens.core.designsystem.components.ProductThumbnailImage(
                                imageUrl = null,
                                productName = name,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "₹${"%,.0f".format(currentPrice)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = PriceLensTheme.glassColors.ambientGlow
                            )
                            Text(
                                text = "₹${"%,.0f".format(originalPrice)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = PriceLensTheme.glassColors.textTertiary,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Save ₹${"%,.0f".format(dropAmount)} today",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MintSuccess
                        )
                    }
                }
            }
        }
    }
}
