package pe.edu.upc.predictivemaintain.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.presentation.ui.HomeScreen
import pe.edu.upc.predictivemaintain.app.iam.presentation.ui.LoginScreen
import pe.edu.upc.predictivemaintain.app.iam.presentation.ui.SplashScreen
import pe.edu.upc.predictivemaintain.app.iam.presentation.viewmodel.HomeViewModel
import pe.edu.upc.predictivemaintain.app.iam.presentation.viewmodel.LoginViewModel
import pe.edu.upc.predictivemaintain.app.iam.presentation.viewmodel.SplashViewModel
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui.AlertDetailScreen
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui.AlertsScreen
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui.AssetDetailScreen
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui.AssetsScreen
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui.WorkOrderDetailScreen
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui.WorkOrdersScreen
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel.AlertsViewModel
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel.AssetsViewModel
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel.WorkOrderDetailViewModel
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel.WorkOrdersViewModel

@Composable
fun AppNavigation(
    session: AuthSession?
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val destinations = session?.let { destinationsFor(it.roles) } ?: emptyList()
    val showBottomBar = session != null && destinations.any { it.screen.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    destinations.forEach { destination ->
                        val selected = currentRoute == destination.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(destination.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { },
                            label = { Text(text = stringResource(destination.titleResId)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                val splashViewModel: SplashViewModel = hiltViewModel()
                SplashScreen(
                    viewModel = splashViewModel,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Login.route) {
                val loginViewModel: LoginViewModel = hiltViewModel()
                LoginScreen(
                    viewModel = loginViewModel,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onLoggedOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Assets.route) {
                val assetsViewModel: AssetsViewModel = hiltViewModel()
                AssetsScreen(
                    viewModel = assetsViewModel,
                    onAssetClick = { assetId ->
                        navController.navigate(Screen.AssetDetail.createRoute(assetId))
                    }
                )
            }
            composable(
                route = "assets/{assetId}",
                arguments = listOf(navArgument("assetId") { type = NavType.StringType })
            ) { backStackEntry ->
                val assetId = backStackEntry.arguments?.getString("assetId") ?: ""
                AssetDetailScreen(
                    assetId = assetId,
                    onBackClick = { navController.popBackStack() },
                    onViewAlertsClick = { id ->
                        navController.navigate(Screen.Alerts.createRoute(id))
                    }
                )
            }
            composable(
                route = "alerts?assetId={assetId}",
                arguments = listOf(navArgument("assetId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val assetId = backStackEntry.arguments?.getString("assetId")
                val alertsViewModel: AlertsViewModel = hiltViewModel()
                AlertsScreen(
                    viewModel = alertsViewModel,
                    assetId = assetId,
                    onAlertClick = { alertId ->
                        navController.navigate(Screen.AlertDetail.createRoute(alertId))
                    }
                )
            }
            composable(
                route = "alerts/{alertId}",
                arguments = listOf(navArgument("alertId") { type = NavType.StringType })
            ) { backStackEntry ->
                val alertId = backStackEntry.arguments?.getString("alertId") ?: ""
                AlertDetailScreen(
                    alertId = alertId,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToWorkOrder = { orderId ->
                        navController.navigate(Screen.OrderDetail.createRoute(orderId))
                    }
                )
            }
            composable(Screen.Orders.route) {
                val workOrdersViewModel: WorkOrdersViewModel = hiltViewModel()
                WorkOrdersScreen(
                    viewModel = workOrdersViewModel,
                    onOrderClick = { orderId ->
                        navController.navigate(Screen.OrderDetail.createRoute(orderId))
                    }
                )
            }
            composable(Screen.MyOrders.route) {
                val workOrdersViewModel: WorkOrdersViewModel = hiltViewModel()
                WorkOrdersScreen(
                    viewModel = workOrdersViewModel,
                    onOrderClick = { orderId ->
                        navController.navigate(Screen.OrderDetail.createRoute(orderId))
                    }
                )
            }
            composable(
                route = "orders/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                val workOrderDetailViewModel: WorkOrderDetailViewModel = hiltViewModel()
                WorkOrderDetailScreen(
                    orderId = orderId,
                    viewModel = workOrderDetailViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.More.route) { PlaceholderScreen("More") }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}
