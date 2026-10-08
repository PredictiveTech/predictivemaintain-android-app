package pe.edu.upc.predictivemaintain.app.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Assets : Screen("assets")
    data object Alerts : Screen("alerts")
    data object Orders : Screen("orders")
    data object MyOrders : Screen("my_orders")
    data object More : Screen("more")
}

data class BottomNavDestination(
    val screen: Screen,
    val titleResId: Int
)
