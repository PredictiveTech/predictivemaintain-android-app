package pe.edu.upc.predictivemaintain.app.subscription.domain.entity

import pe.edu.upc.predictivemaintain.app.subscription.fakes.basicPlan
import pe.edu.upc.predictivemaintain.app.subscription.fakes.proPlan
import pe.edu.upc.predictivemaintain.app.subscription.fakes.subscription
import pe.edu.upc.predictivemaintain.app.subscription.fakes.tinyPlan
import org.junit.Assert.assertEquals
import org.junit.Test

class SubscriptionTest {

    @Test
    fun `nothing to deactivate when the assets fit the destination plan`() {
        assertEquals(0, subscription(used = 10).assetsToDeactivateFor(basicPlan)) // exactly the limit
        assertEquals(0, subscription(used = 4).assetsToDeactivateFor(basicPlan))
    }

    @Test
    fun `counts the assets over the destination limit`() {
        assertEquals(2, subscription(used = 12).assetsToDeactivateFor(basicPlan))
        assertEquals(9, subscription(used = 12).assetsToDeactivateFor(tinyPlan))
    }

    @Test
    fun `an upgrade never needs deactivations`() {
        assertEquals(0, subscription(plan = basicPlan, used = 10).assetsToDeactivateFor(proPlan))
    }

    @Test
    fun `status parses the three server values and falls back to unknown`() {
        assertEquals(SubscriptionStatus.ACTIVE, SubscriptionStatus.fromString("active"))
        assertEquals(SubscriptionStatus.SUSPENDED, SubscriptionStatus.fromString("SUSPENDED"))
        assertEquals(SubscriptionStatus.CANCELLED, SubscriptionStatus.fromString("CANCELLED"))
        assertEquals(SubscriptionStatus.UNKNOWN, SubscriptionStatus.fromString("PAUSED"))
        assertEquals(SubscriptionStatus.UNKNOWN, SubscriptionStatus.fromString(null))
    }
}
