package com.pricelens.core.navigation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import com.pricelens.presentation.deals.DealsRoute
import com.pricelens.presentation.onboarding.OnboardingScreen
import com.pricelens.presentation.search.SearchRoute
import com.pricelens.presentation.search.SearchViewModel
import com.pricelens.presentation.tools.ToolsRoute
import com.pricelens.presentation.watchlist.WatchlistRoute
import com.pricelens.presentation.watchlist.WatchlistViewModel
import com.pricelens.presentation.webpicker.WebPricePickerScreen
import com.pricelens.presentation.webpicker.WebPricePickerViewModel

@Composable
fun PriceLensNavGraph(
    navController: NavHostController,
    userPreferences: UserPreferencesRepository? = null,
    modifier: Modifier = Modifier,
    startDestination: String? = null,
    sharedUrl: String? = null,
    sharedProductId: String? = null
) {
    val context = LocalContext.current
    val effectivePreferences = userPreferences
        ?: com.pricelens.core.designsystem.LocalUserPreferences.current
        ?: remember { UserPreferencesRepository(context.applicationContext) }

    val resolvedStartDestination = startDestination ?: if (effectivePreferences.hasCompletedOnboarding) {
        PriceLensDestination.Dashboard.route
    } else {
        PriceLensDestination.Onboarding.route
    }

    NavHost(
        navController = navController,
        startDestination = resolvedStartDestination,
        modifier = modifier.fillMaxSize(),
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> (fullWidth * 0.16f).toInt() },
                animationSpec = tween(durationMillis = 320, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            ) + fadeIn(animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)) +
            scaleIn(
                initialScale = 0.95f,
                animationSpec = tween(durationMillis = 320, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -(fullWidth * 0.12f).toInt() },
                animationSpec = tween(durationMillis = 300, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            ) + fadeOut(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)) +
            scaleOut(
                targetScale = 0.96f,
                animationSpec = tween(durationMillis = 300, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -(fullWidth * 0.12f).toInt() },
                animationSpec = tween(durationMillis = 320, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            ) + fadeIn(animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)) +
            scaleIn(
                initialScale = 0.96f,
                animationSpec = tween(durationMillis = 320, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> (fullWidth * 0.16f).toInt() },
                animationSpec = tween(durationMillis = 300, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            ) + fadeOut(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)) +
            scaleOut(
                targetScale = 0.95f,
                animationSpec = tween(durationMillis = 300, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            )
        }
    ) {
        // 1. Onboarding / Intro Screen
        composable(PriceLensDestination.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    effectivePreferences.hasCompletedOnboarding = true
                    navController.navigate(PriceLensDestination.Dashboard.route) {
                        popUpTo(PriceLensDestination.Onboarding.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // 2. Main Dashboard Screen (Link-First Experience / Browse Products)
        composable(
            route = PriceLensDestination.Dashboard.route,
            enterTransition = {
                if (initialState.destination.route == PriceLensDestination.Watchlist.route) {
                    scaleIn(
                        initialScale = 0.93f,
                        animationSpec = tween(durationMillis = 340, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f))
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                    ) + slideInVertically(
                        initialOffsetY = { -it / 20 },
                        animationSpec = tween(durationMillis = 340, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f))
                    )
                } else {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth / 4 },
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))
                }
            },
            exitTransition = {
                if (targetState.destination.route == PriceLensDestination.Watchlist.route) {
                    scaleOut(
                        targetScale = 0.93f,
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                    ) + slideOutVertically(
                        targetOffsetY = { -it / 20 },
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                    )
                } else {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 4 },
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing))
                }
            },
            popEnterTransition = {
                if (initialState.destination.route == PriceLensDestination.Watchlist.route) {
                    scaleIn(
                        initialScale = 0.93f,
                        animationSpec = tween(durationMillis = 340, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f))
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                    ) + slideInVertically(
                        initialOffsetY = { -it / 20 },
                        animationSpec = tween(durationMillis = 340, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f))
                    )
                } else {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth / 4 },
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))
                }
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth / 4 },
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing))
            }
        ) {
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
                },
                onNavigateToWebPicker = { url, name ->
                    navController.navigate(
                        PriceLensDestination.WebPricePicker.createRoute(url, name)
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
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)) +
                    slideInVertically(
                        initialOffsetY = { -it / 8 },
                        animationSpec = tween(durationMillis = 260, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f))
                    ) +
                    scaleIn(
                        initialScale = 0.96f,
                        animationSpec = tween(durationMillis = 260, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f))
                    )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)) +
                    scaleOut(targetScale = 0.96f, animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 200)) + scaleIn(initialScale = 0.96f)
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)) +
                    slideOutVertically(
                        targetOffsetY = { -it / 8 },
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                    )
            }
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
            ),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() },
                    animationSpec = tween(durationMillis = 320, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f))
                ) +
                fadeIn(animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)) +
                scaleIn(
                    initialScale = 0.94f,
                    animationSpec = tween(durationMillis = 320, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f))
                )
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
                    targetOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() },
                    animationSpec = tween(durationMillis = 280, easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f))
                ) +
                fadeOut(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)) +
                scaleOut(
                    targetScale = 0.94f,
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                )
            }
        ) {
            val viewModel: ProductComparisonViewModel = hiltViewModel()
            ProductComparisonRoute(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5. Watchlist Screen (Bookmarks)
        composable(
            route = PriceLensDestination.Watchlist.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { fullHeight -> (fullHeight * 0.95f).toInt() },
                    animationSpec = tween(
                        durationMillis = 360,
                        easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
                    )
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                ) + scaleIn(
                    initialScale = 0.93f,
                    animationSpec = tween(
                        durationMillis = 360,
                        easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
                    )
                )
            },
            exitTransition = {
                if (targetState.destination.route == PriceLensDestination.Dashboard.route) {
                    slideOutVertically(
                        targetOffsetY = { fullHeight -> (fullHeight * 0.95f).toInt() },
                        animationSpec = tween(
                            durationMillis = 320,
                            easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
                        )
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                    ) + scaleOut(
                        targetScale = 0.93f,
                        animationSpec = tween(
                            durationMillis = 320,
                            easing = FastOutSlowInEasing
                        )
                    )
                } else {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 4 },
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing))
                }
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> (fullHeight * 0.95f).toInt() },
                    animationSpec = tween(
                        durationMillis = 320,
                        easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
                    )
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                ) + scaleOut(
                    targetScale = 0.93f,
                    animationSpec = tween(
                        durationMillis = 320,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) {
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
                    val popped = navController.popBackStack(PriceLensDestination.Dashboard.route, false)
                    if (!popped) {
                        navController.navigate(PriceLensDestination.Dashboard.route) {
                            popUpTo(PriceLensDestination.Dashboard.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 6. Universal Web Price Picker (Interactive In-App WebView Inspector)
        composable(
            route = PriceLensDestination.WebPricePicker.route,
            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("name") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            ),
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 340, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f))
                ) + fadeIn(animationSpec = tween(durationMillis = 200))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 280, easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f))
                ) + fadeOut(animationSpec = tween(durationMillis = 180))
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 280, easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f))
                ) + fadeOut(animationSpec = tween(durationMillis = 180))
            }
        ) {
            val viewModel: WebPricePickerViewModel = hiltViewModel()
            WebPricePickerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 7. Deals Radar Screen (Live Price Drops & Flash Discounts)
        composable(
            route = PriceLensDestination.Deals.route,
            enterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)) +
                slideInVertically(
                    initialOffsetY = { fullHeight -> (fullHeight * 0.12f).toInt() },
                    animationSpec = tween(durationMillis = 300, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f))
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)) +
                slideOutVertically(
                    targetOffsetY = { fullHeight -> -(fullHeight * 0.08f).toInt() },
                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                )
            }
        ) {
            DealsRoute(
                onNavigateToComparison = { productId, productName ->
                    navController.navigate(
                        PriceLensDestination.ProductComparison.createRoute(productId, productName)
                    )
                }
            )
        }

        // 8. Power Tools & Labs Screen
        composable(
            route = PriceLensDestination.Tools.route,
            enterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)) +
                slideInVertically(
                    initialOffsetY = { fullHeight -> (fullHeight * 0.12f).toInt() },
                    animationSpec = tween(durationMillis = 300, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f))
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)) +
                slideOutVertically(
                    targetOffsetY = { fullHeight -> -(fullHeight * 0.08f).toInt() },
                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                )
            }
        ) {
            ToolsRoute(
                onNavigateToWebPicker = { url, name ->
                    navController.navigate(
                        PriceLensDestination.WebPricePicker.createRoute(url, name)
                    )
                },
                onNavigateToSearch = { query ->
                    navController.navigate(PriceLensDestination.Search.createRoute(query))
                },
                userPreferences = effectivePreferences
            )
        }
    }
}
