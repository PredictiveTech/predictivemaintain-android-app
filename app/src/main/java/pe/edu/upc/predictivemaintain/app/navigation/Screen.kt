package pe.edu.upc.predictivemaintain.app.navigation

import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
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
    data object OrderDetail : Screen("orders/{orderId}") {
        fun createRoute(orderId: String) = "orders/$orderId"
    }
    data object CreateAsset : Screen("asset_create")
    data object Profile : Screen("profile")
}

data class BottomNavDestination(
    val screen: Screen,
    val titleResId: Int,
    val icon: ImageVector
)
