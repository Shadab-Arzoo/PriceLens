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
import com.pricelens.presentation.dashboard.DashboardRoute
import com.pricelens.presentation.dashboard.DashboardViewModel
import com.pricelens.presentation.scanner.ScannerScreen

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
                }
            )
        }

        composable(PriceLensDestination.Scanner.route) {
            ScannerScreen(
                onNavigateBack = { navController.popBackStack() }
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
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("q").orEmpty()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PriceLensTheme.glassColors.backgroundBrush)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (query.isNotBlank()) "Search results for: \"$query\"" else "Product Search",
                    style = MaterialTheme.typography.titleMedium,
                    color = PriceLensTheme.glassColors.textPrimary
                )
            }
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
