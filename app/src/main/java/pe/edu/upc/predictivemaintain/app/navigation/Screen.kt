package pe.edu.upc.predictivemaintain.app.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Assets : Screen("assets")
    data object AssetDetail : Screen("assets/{assetId}") {
        fun createRoute(assetId: String) = "assets/$assetId"
    }
    data object Alerts : Screen("alerts") {
        fun createRoute(assetId: String? = null) = if (!assetId.isNullOrBlank() && assetId != "{assetId}") "alerts?assetId=$assetId" else "alerts"
    }
    data object AlertDetail : Screen("alerts/{alertId}") {
        fun createRoute(alertId: String) = "alerts/$alertId"
    }
    data object Orders : Screen("orders")
    data object MyOrders : Screen("my_orders")
    data object More : Screen("more")
}

data class BottomNavDestination(
    val screen: Screen,
    val titleResId: Int
)
