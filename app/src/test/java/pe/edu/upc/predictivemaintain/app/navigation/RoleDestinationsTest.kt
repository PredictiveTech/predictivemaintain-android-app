package pe.edu.upc.predictivemaintain.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role

class RoleDestinationsTest {

    @Test
    fun `manager role returns manager destinations`() {
        val destinations = destinationsFor(listOf(Role.MAINTENANCE_MANAGER))
        assertEquals(5, destinations.size)
        assertEquals(Screen.Home, destinations[0].screen)
        assertEquals(Screen.Assets, destinations[1].screen)
        assertEquals(Screen.Alerts, destinations[2].screen)
        assertEquals(Screen.Orders, destinations[3].screen)
        assertEquals(Screen.More, destinations[4].screen)
    }

    @Test
    fun `technician role returns technician destinations`() {
        val destinations = destinationsFor(listOf(Role.TECHNICIAN))
        assertEquals(5, destinations.size)
        assertEquals(Screen.Home, destinations[0].screen)
        assertEquals(Screen.Assets, destinations[1].screen)
        assertEquals(Screen.Alerts, destinations[2].screen)
        assertEquals(Screen.MyOrders, destinations[3].screen)
        assertEquals(Screen.More, destinations[4].screen)
    }

    @Test
    fun `operator role returns operator destinations`() {
        val destinations = destinationsFor(listOf(Role.OPERATOR))
        assertEquals(4, destinations.size)
        assertEquals(Screen.Home, destinations[0].screen)
        assertEquals(Screen.Assets, destinations[1].screen)
        assertEquals(Screen.Alerts, destinations[2].screen)
        assertEquals(Screen.More, destinations[3].screen)
    }

    @Test
    fun `user with multiple roles gets the highest role`() {
        val destinations = destinationsFor(listOf(Role.OPERATOR, Role.MAINTENANCE_MANAGER, Role.TECHNICIAN))
        assertEquals(5, destinations.size)
        assertEquals(Screen.Orders, destinations[3].screen) // Manager has Orders instead of My orders
    }
}
