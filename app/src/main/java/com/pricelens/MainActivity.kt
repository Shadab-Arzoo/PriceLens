package com.pricelens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.pricelens.core.designsystem.LocalRootBackdrop
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.DockTab
import com.pricelens.core.designsystem.components.PriceLensBottomDock
import com.pricelens.core.navigation.PriceLensDestination
import com.pricelens.core.navigation.PriceLensNavGraph
import com.pricelens.core.preferences.UserPreferencesRepository
import com.pricelens.domain.repository.DashboardRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private val SharedTextUrlRegex = Regex("""https?://[^\s]+""")
    }

    @Inject
    lateinit var userPreferences: UserPreferencesRepository

    @Inject
    lateinit var dashboardRepository: DashboardRepository

    private var sharedUrl by mutableStateOf<String?>(null)
    private var sharedProductId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            val themeMode by userPreferences.themeModeFlow.collectAsStateWithLifecycle()
            val themeAccent by userPreferences.themeAccentFlow.collectAsStateWithLifecycle()
            val glowIntensity by userPreferences.glowIntensityFlow.collectAsStateWithLifecycle()
            val contrastStyle by userPreferences.contrastStyleFlow.collectAsStateWithLifecycle()
            val glassRefraction by userPreferences.glassRefractionFlow.collectAsStateWithLifecycle()
            val glassDispersion by userPreferences.glassDispersionFlow.collectAsStateWithLifecycle()
            val iosSwitchEnabled by userPreferences.iosSwitchFlow.collectAsStateWithLifecycle()
            val appFont by userPreferences.appFontFlow.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                UserPreferencesRepository.ThemeMode.DARK -> true
                UserPreferencesRepository.ThemeMode.LIGHT -> false
                UserPreferencesRepository.ThemeMode.SYSTEM -> systemDark
            }

            PriceLensTheme(
                darkTheme = isDark,
                accent = themeAccent,
                glowIntensity = glowIntensity,
                contrastStyle = contrastStyle,
                glassRefraction = glassRefraction,
                glassDispersion = glassDispersion,
                iosSwitchEnabled = iosSwitchEnabled,
                appFont = appFont,
                userPreferences = userPreferences,
                onToggleTheme = {
                    userPreferences.toggleTheme(isDark)
                }
            ) {
                val rootBackdrop = rememberLayerBackdrop()

                CompositionLocalProvider(LocalRootBackdrop provides rootBackdrop) {
                    val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val isDockVisible = remember(currentRoute) {
                    currentRoute != null &&
                        !currentRoute.startsWith(PriceLensDestination.Onboarding.route) &&
                        !currentRoute.startsWith(PriceLensDestination.WebPricePicker.route.substringBefore("?"))
                }

                val selectedTab = remember(currentRoute) {
                    when {
                        currentRoute == null -> DockTab.EXPLORE
                        currentRoute.startsWith(PriceLensDestination.Dashboard.route) -> DockTab.EXPLORE
                        currentRoute.startsWith("search") -> DockTab.SEARCH
                        currentRoute.startsWith(PriceLensDestination.Deals.route) -> DockTab.DEALS
                        currentRoute.startsWith(PriceLensDestination.Watchlist.route) -> DockTab.WATCHLIST
                        currentRoute.startsWith(PriceLensDestination.Tools.route) -> DockTab.TOOLS
                        else -> null
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = PriceLensTheme.glassColors.backgroundBrush)
                ) {
                    val accent = PriceLensTheme.glassColors.ambientGlow
                    val secondary = PriceLensTheme.glassColors.accentSecondary
                    val isDark = PriceLensTheme.isDark

                    val isCupertino = PriceLensTheme.isCupertino

                    // Ambient liquid glass backdrop orbs (active only for non-Apple themes; Apple uses pure neutral canvas)
                    if (!isCupertino) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    // Top-right radiant orb
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                accent.copy(alpha = if (isDark) 0.16f else 0.08f),
                                                Color.Transparent
                                            ),
                                            center = Offset(size.width * 0.85f, size.height * 0.10f),
                                            radius = size.width * 0.70f
                                        )
                                    )

                                    // Middle-left secondary orb
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                secondary.copy(alpha = if (isDark) 0.12f else 0.06f),
                                                Color.Transparent
                                            ),
                                            center = Offset(size.width * 0.10f, size.height * 0.42f),
                                            radius = size.width * 0.65f
                                        )
                                    )

                                    // Bottom floating orb behind dock
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                accent.copy(alpha = if (isDark) 0.18f else 0.09f),
                                                Color.Transparent
                                            ),
                                            center = Offset(size.width * 0.50f, size.height * 0.92f),
                                            radius = size.width * 0.60f
                                        )
                                    )
                                }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (iosSwitchEnabled) Modifier.layerBackdrop(rootBackdrop) else Modifier)
                    ) {
                        PriceLensNavGraph(
                            navController = navController,
                            userPreferences = userPreferences,
                            sharedUrl = sharedUrl,
                            sharedProductId = sharedProductId
                        )
                    }

                    val onTabSelected = remember(navController) {
                        { tab: DockTab ->
                            val targetRoute = when (tab) {
                                DockTab.EXPLORE -> PriceLensDestination.Dashboard.route
                                DockTab.SEARCH -> PriceLensDestination.Search.createRoute("")
                                DockTab.DEALS -> PriceLensDestination.Deals.route
                                DockTab.WATCHLIST -> PriceLensDestination.Watchlist.route
                                DockTab.TOOLS -> PriceLensDestination.Tools.route
                            }
                            navController.navigate(targetRoute) {
                                popUpTo(PriceLensDestination.Dashboard.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }

                    val onTabReselected = remember(navController) {
                        { tab: DockTab ->
                            val targetRoute = when (tab) {
                                DockTab.EXPLORE -> PriceLensDestination.Dashboard.route
                                DockTab.SEARCH -> PriceLensDestination.Search.createRoute("")
                                DockTab.DEALS -> PriceLensDestination.Deals.route
                                DockTab.WATCHLIST -> PriceLensDestination.Watchlist.route
                                DockTab.TOOLS -> PriceLensDestination.Tools.route
                            }
                            navController.navigate(targetRoute) {
                                popUpTo(targetRoute) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }

                    val onTabLongClick = remember(navController) {
                        { tab: DockTab ->
                            when (tab) {
                                DockTab.EXPLORE -> {
                                    navController.navigate(PriceLensDestination.Dashboard.route) {
                                        popUpTo(PriceLensDestination.Dashboard.route) { inclusive = true }
                                    }
                                }
                                DockTab.SEARCH -> {
                                    navController.navigate(PriceLensDestination.Search.createRoute("")) {
                                        launchSingleTop = true
                                    }
                                }
                                DockTab.DEALS -> {
                                    navController.navigate(PriceLensDestination.Deals.route) {
                                        launchSingleTop = true
                                    }
                                }
                                DockTab.WATCHLIST -> {
                                    navController.navigate(PriceLensDestination.Watchlist.route) {
                                        launchSingleTop = true
                                    }
                                }
                                DockTab.TOOLS -> {
                                    navController.navigate(PriceLensDestination.Tools.route) {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }
                    }

                    MainActivityBottomDock(
                        dashboardRepository = dashboardRepository,
                        selectedTab = selectedTab,
                        onTabSelected = onTabSelected,
                        onTabReselected = onTabReselected,
                        onTabLongClick = onTabLongClick,
                        isVisible = isDockVisible,
                        backdrop = if (iosSwitchEnabled) rootBackdrop else null,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action == Intent.ACTION_SEND && intent.type?.startsWith("text/") == true) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
            val extracted = SharedTextUrlRegex.find(text)?.value ?: text.trim()
            if (extracted.isNotBlank()) {
                sharedUrl = extracted
            }
        } else if (intent.hasExtra("productId")) {
            sharedProductId = intent.getStringExtra("productId")
        }
    }
}

@Composable
private fun MainActivityBottomDock(
    dashboardRepository: DashboardRepository,
    selectedTab: DockTab?,
    onTabSelected: (DockTab) -> Unit,
    onTabReselected: (DockTab) -> Unit,
    onTabLongClick: (DockTab) -> Unit,
    isVisible: Boolean,
    backdrop: Backdrop? = null,
    modifier: Modifier = Modifier
) {
    val watchlistCountFlow = remember(dashboardRepository) {
        dashboardRepository.getWatchlistItems()
            .map { it.size }
            .distinctUntilChanged()
    }
    val watchlistCount by watchlistCountFlow.collectAsStateWithLifecycle(initialValue = 0)

    PriceLensBottomDock(
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        onTabReselected = onTabReselected,
        onTabLongClick = onTabLongClick,
        watchlistCount = watchlistCount,
        isVisible = isVisible,
        backdrop = backdrop,
        modifier = modifier
    )
}
