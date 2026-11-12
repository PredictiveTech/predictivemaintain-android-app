package pe.edu.upc.predictivemaintain.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role

fun destinationsFor(roles: List<Role>): List<BottomNavDestination> {
    val highestRole = when {
        roles.contains(Role.MAINTENANCE_MANAGER) -> Role.MAINTENANCE_MANAGER
        roles.contains(Role.TECHNICIAN) -> Role.TECHNICIAN
        else -> Role.OPERATOR
    }

    val assets = BottomNavDestination(Screen.Assets, R.string.nav_assets, Icons.Filled.Settings)
    val alerts = BottomNavDestination(Screen.Alerts, R.string.nav_alerts, Icons.Filled.Warning)
    val profile = BottomNavDestination(Screen.Profile, R.string.nav_profile, Icons.Filled.Person)

    return when (highestRole) {
        Role.MAINTENANCE_MANAGER -> listOf(
            assets, alerts,
            BottomNavDestination(Screen.Orders, R.string.nav_orders, Icons.AutoMirrored.Filled.List),
            profile
        )
        Role.TECHNICIAN -> listOf(
            assets, alerts,
            BottomNavDestination(Screen.MyOrders, R.string.nav_my_orders, Icons.AutoMirrored.Filled.List),
            profile
        )
        Role.OPERATOR -> listOf(assets, alerts, profile)
    }
}
