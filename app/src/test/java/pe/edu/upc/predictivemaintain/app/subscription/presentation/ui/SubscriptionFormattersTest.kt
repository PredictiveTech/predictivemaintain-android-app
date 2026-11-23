package pe.edu.upc.predictivemaintain.app.subscription.presentation.ui

import pe.edu.upc.predictivemaintain.app.core.ui.StatusTone
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionStatus
import pe.edu.upc.predictivemaintain.app.subscription.fakes.basicPlan
import pe.edu.upc.predictivemaintain.app.subscription.fakes.subscription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class SubscriptionFormattersTest {

    @Test
    fun `money uses the currency of the plan`() {
        assertEquals("$149.00", formatMoney(149.0, "USD", Locale.US))
    }

    @Test
    fun `money falls back to the raw code when the currency is unknown or missing`() {
        assertEquals("49.50 ZZZZ", formatMoney(49.5, "ZZZZ", Locale.US))
        assertEquals("49.50", formatMoney(49.5, "", Locale.US))
    }

    @Test
    fun `usage percent is rounded to a whole number`() {
        assertEquals("92%", formatUsagePercent(91.6, Locale.US))
        assertEquals("0%", formatUsagePercent(0.0, Locale.US))
    }

    @Test
    fun `progress is clamped between empty and full`() {
        assertEquals(0f, usageFraction(-5.0), 0f)
        assertEquals(0.5f, usageFraction(50.0), 0f)
        assertEquals(1f, usageFraction(130.0), 0f)
    }

    @Test
    fun `usage level follows the server flag and an exhausted plan is full`() {
        assertEquals(UsageLevel.NORMAL, usageLevel(subscription(used = 12)))
        assertEquals(UsageLevel.NEAR_LIMIT, usageLevel(subscription(plan = basicPlan, used = 9, nearLimit = true)))
        assertEquals(UsageLevel.FULL, usageLevel(subscription(plan = basicPlan, used = 10, nearLimit = true)))
        assertEquals(UsageLevel.FULL, usageLevel(subscription(plan = basicPlan, used = 12)))
    }

    @Test
    fun `tones`() {
        assertNull(usageTone(UsageLevel.NORMAL))
        assertEquals(StatusTone.WARNING, usageTone(UsageLevel.NEAR_LIMIT))
        assertEquals(StatusTone.DANGER, usageTone(UsageLevel.FULL))
        assertEquals(StatusTone.SUCCESS, statusTone(SubscriptionStatus.ACTIVE))
        assertEquals(StatusTone.DANGER, statusTone(SubscriptionStatus.CANCELLED))
    }
}
