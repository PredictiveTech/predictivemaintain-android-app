package pe.edu.upc.predictivemaintain.app.navigation

import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role

fun destinationsFor(roles: List<Role>): List<BottomNavDestination> {
    val highestRole = when {
        roles.contains(Role.MAINTENANCE_MANAGER) -> Role.MAINTENANCE_MANAGER
        roles.contains(Role.TECHNICIAN) -> Role.TECHNICIAN
        else -> Role.OPERATOR
    }

    return when (highestRole) {
        Role.MAINTENANCE_MANAGER -> listOf(
            BottomNavDestination(Screen.Assets, R.string.nav_assets),
            BottomNavDestination(Screen.Alerts, R.string.nav_alerts),
            BottomNavDestination(Screen.Orders, R.string.nav_orders)
        )
        Role.TECHNICIAN -> listOf(
            BottomNavDestination(Screen.Assets, R.string.nav_assets),
            BottomNavDestination(Screen.Alerts, R.string.nav_alerts),
            BottomNavDestination(Screen.MyOrders, R.string.nav_my_orders)
        )
        Role.OPERATOR -> listOf(
            BottomNavDestination(Screen.Assets, R.string.nav_assets),
            BottomNavDestination(Screen.Alerts, R.string.nav_alerts)
        )
    }
}
