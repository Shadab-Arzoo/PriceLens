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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
                userPreferences = userPreferences,
                onToggleTheme = {
                    userPreferences.toggleTheme(isDark)
                }
            ) {
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
                    PriceLensNavGraph(
                        navController = navController,
                        userPreferences = userPreferences,
                        sharedUrl = sharedUrl,
                        sharedProductId = sharedProductId
                    )

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
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
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
        modifier = modifier
    )
}
