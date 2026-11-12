package pe.edu.upc.predictivemaintain.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role

class RoleDestinationsTest {

    @Test
    fun `manager role returns manager destinations`() {
        val destinations = destinationsFor(listOf(Role.MAINTENANCE_MANAGER))
        assertEquals(
            listOf(Screen.Assets, Screen.Alerts, Screen.Orders, Screen.Profile),
            destinations.map { it.screen }
        )
    }

    @Test
    fun `technician role returns technician destinations`() {
        val destinations = destinationsFor(listOf(Role.TECHNICIAN))
        assertEquals(
            listOf(Screen.Assets, Screen.Alerts, Screen.MyOrders, Screen.Profile),
            destinations.map { it.screen }
        )
    }

    @Test
    fun `operator role returns operator destinations`() {
        val destinations = destinationsFor(listOf(Role.OPERATOR))
        assertEquals(
            listOf(Screen.Assets, Screen.Alerts, Screen.Profile),
            destinations.map { it.screen }
        )
    }

    @Test
    fun `user with multiple roles gets the highest role`() {
        val destinations = destinationsFor(listOf(Role.OPERATOR, Role.MAINTENANCE_MANAGER, Role.TECHNICIAN))
        assertEquals(Screen.Orders, destinations[2].screen) // Manager has Orders instead of My orders
    }

    @Test
    fun `every role has the profile tab last`() {
        Role.entries.forEach { role ->
            assertEquals(Screen.Profile, destinationsFor(listOf(role)).last().screen)
        }
    }
}
