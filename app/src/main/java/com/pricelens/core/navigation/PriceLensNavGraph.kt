package com.pricelens.core.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.preferences.UserPreferencesRepository
import com.pricelens.presentation.comparison.ProductComparisonRoute
import com.pricelens.presentation.comparison.ProductComparisonViewModel
import com.pricelens.presentation.dashboard.DashboardRoute
import com.pricelens.presentation.dashboard.DashboardViewModel
import com.pricelens.presentation.onboarding.OnboardingScreen
import com.pricelens.presentation.search.SearchRoute
import com.pricelens.presentation.search.SearchViewModel
import com.pricelens.presentation.watchlist.WatchlistRoute
import com.pricelens.presentation.watchlist.WatchlistViewModel

@Composable
fun PriceLensNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String? = null,
    sharedUrl: String? = null,
    sharedProductId: String? = null
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferencesRepository(context.applicationContext) }

    val resolvedStartDestination = startDestination ?: if (userPreferences.hasCompletedOnboarding) {
        PriceLensDestination.Dashboard.route
    } else {
        PriceLensDestination.Onboarding.route
    }

    NavHost(
        navController = navController,
        startDestination = resolvedStartDestination,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth / 4 },
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth / 4 },
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing))
        }
    ) {
        // 1. Onboarding / Intro Screen
        composable(PriceLensDestination.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    userPreferences.hasCompletedOnboarding = true
                    navController.navigate(PriceLensDestination.Dashboard.route) {
                        popUpTo(PriceLensDestination.Onboarding.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // 2. Main Dashboard Screen (Link-First Experience)
        composable(PriceLensDestination.Dashboard.route) {
            val viewModel: DashboardViewModel = hiltViewModel()

            androidx.compose.runtime.LaunchedEffect(sharedUrl) {
                if (!sharedUrl.isNullOrBlank()) {
                    viewModel.onEvent(com.pricelens.presentation.dashboard.DashboardUiEvent.OpenPasteUrlSheet)
                    viewModel.onEvent(com.pricelens.presentation.dashboard.DashboardUiEvent.PasteUrlInputChanged(sharedUrl))
                }
            }

            androidx.compose.runtime.LaunchedEffect(sharedProductId) {
                if (!sharedProductId.isNullOrBlank()) {
                    navController.navigate(
                        PriceLensDestination.ProductComparison.createRoute(sharedProductId, "")
                    )
                }
            }

            DashboardRoute(
                viewModel = viewModel,
                onNavigateToSearch = { query ->
                    navController.navigate(PriceLensDestination.Search.createRoute(query))
                },
                onNavigateToWatchlist = {
                    navController.navigate(PriceLensDestination.Watchlist.route)
                },
                onNavigateToComparison = { productId, productName ->
                    navController.navigate(
                        PriceLensDestination.ProductComparison.createRoute(productId, productName)
                    )
                }
            )
        }

        // 3. Search Screen
        composable(
            route = PriceLensDestination.Search.route,
            arguments = listOf(
                navArgument("q") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            val viewModel: SearchViewModel = hiltViewModel()
            SearchRoute(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onProductClick = { productId, productName ->
                    navController.navigate(
                        PriceLensDestination.ProductComparison.createRoute(productId, productName)
                    )
                }
            )
        }

        // 4. Product Comparison Screen
        composable(
            route = PriceLensDestination.ProductComparison.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                },
                navArgument("productName") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            val viewModel: ProductComparisonViewModel = hiltViewModel()
            ProductComparisonRoute(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5. Watchlist Screen
        composable(PriceLensDestination.Watchlist.route) {
            val viewModel: WatchlistViewModel = hiltViewModel()
            WatchlistRoute(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToComparison = { productId, productName ->
                    navController.navigate(
                        PriceLensDestination.ProductComparison.createRoute(productId, productName)
                    )
                },
                onNavigateToBrowse = {
                    navController.popBackStack(PriceLensDestination.Dashboard.route, false)
                }
            )
        }
    }
}
