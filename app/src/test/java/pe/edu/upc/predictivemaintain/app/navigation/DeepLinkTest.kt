package pe.edu.upc.predictivemaintain.app.navigation

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DeepLinkTest {

    @Test
    fun `the subscription expiry type opens the subscription`() {
        assertEquals(DeepLink.SUBSCRIPTION, DeepLink.fromType("SUBSCRIPTION_EXPIRING"))
        assertEquals(DeepLink.SUBSCRIPTION, DeepLink.fromType(" subscription_expiring "))
    }

    @Test
    fun `other or missing types open nothing`() {
        assertNull(DeepLink.fromType("ALERT_RAISED"))
        assertNull(DeepLink.fromType(""))
        assertNull(DeepLink.fromType(null))
    }

    @Test
    fun `the intent extra of a tapped notification is read`() {
        val intent = Intent().putExtra(DeepLink.EXTRA_TYPE, "SUBSCRIPTION_EXPIRING")

        assertEquals(DeepLink.SUBSCRIPTION, DeepLink.fromIntent(intent))
        assertNull(DeepLink.fromIntent(Intent()))
        assertNull(DeepLink.fromIntent(null))
    }

    @Test
    fun `only a manager can be taken to the subscription`() {
        assertEquals(Screen.Subscription.route, DeepLink.SUBSCRIPTION.targetRoute(isManager = true))
        assertNull(DeepLink.SUBSCRIPTION.targetRoute(isManager = false))
    }
}
