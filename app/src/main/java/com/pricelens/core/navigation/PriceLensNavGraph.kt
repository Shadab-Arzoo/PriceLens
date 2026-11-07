package com.pricelens.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.presentation.comparison.ProductComparisonRoute
import com.pricelens.presentation.comparison.ProductComparisonViewModel
import com.pricelens.presentation.dashboard.DashboardRoute
import com.pricelens.presentation.dashboard.DashboardViewModel
import com.pricelens.presentation.scanner.ScannerScreen
import com.pricelens.presentation.search.SearchRoute
import com.pricelens.presentation.search.SearchViewModel

@Composable
fun PriceLensNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = PriceLensDestination.Dashboard.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(PriceLensDestination.Dashboard.route) {
            val viewModel: DashboardViewModel = hiltViewModel()
            DashboardRoute(
                viewModel = viewModel,
                onNavigateToScan = {
                    navController.navigate(PriceLensDestination.Scanner.route)
                },
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

        composable(PriceLensDestination.Scanner.route) {
            val viewModel: com.pricelens.presentation.scanner.ScannerViewModel = hiltViewModel()
            com.pricelens.presentation.scanner.ScannerRoute(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToComparison = { productId, productName ->
                    navController.navigate(
                        PriceLensDestination.ProductComparison.createRoute(productId, productName)
                    )
                },
                onNavigateToSearch = { query ->
                    navController.navigate(PriceLensDestination.Search.createRoute(query))
                }
            )
        }

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

        composable(PriceLensDestination.Watchlist.route) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PriceLensTheme.glassColors.backgroundBrush)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tracked Price Watchlist",
                    style = MaterialTheme.typography.titleMedium,
                    color = PriceLensTheme.glassColors.textPrimary
                )
            }
        }
    }
}
