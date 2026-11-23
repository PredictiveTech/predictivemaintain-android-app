package pe.edu.upc.predictivemaintain.app.navigation

import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DeepLinkTest {

    @Test
    fun `the subscription expiry type opens the subscription`() {
        assertEquals(DeepLink.Subscription, DeepLink.fromData(mapOf("type" to "SUBSCRIPTION_EXPIRING")))
        assertEquals(DeepLink.Subscription, DeepLink.fromData(mapOf("type" to " subscription_expiring ")))
    }

    @Test
    fun `an alert id opens the alert`() {
        assertEquals(DeepLink.Alert("a-1"), DeepLink.fromData(mapOf("alertId" to "a-1")))
        assertEquals(DeepLink.Alert("a-2"), DeepLink.fromData(mapOf("type" to "ALERT_RAISED", "subjectId" to "a-2")))
    }

    @Test
    fun `a work order id opens the order and wins over an alert id`() {
        assertEquals(DeepLink.WorkOrder("w-1"), DeepLink.fromData(mapOf("workOrderId" to "w-1")))
        assertEquals(DeepLink.WorkOrder("w-1"), DeepLink.fromData(mapOf("workOrderId" to "w-1", "alertId" to "a-1")))
    }

    @Test
    fun `unrelated or incomplete payloads open nothing`() {
        assertNull(DeepLink.fromData(emptyMap()))
        assertNull(DeepLink.fromData(mapOf("title" to "Hi", "body" to "There")))
        assertNull(DeepLink.fromData(mapOf("alertId" to "  ")))
        assertNull(DeepLink.fromData(mapOf("type" to "ALERT_RAISED"))) // no subject to open
        assertNull(DeepLink.fromData(mapOf("type" to "SOMETHING_ELSE", "subjectId" to "x")))
        assertNull(DeepLink.fromData(mapOf("alertId" to null)))
    }

    @Test
    fun `links become the uri that AppNavigation declares and parse back`() {
        assertEquals("predictivemaintain://alerts/a-1", DeepLink.Alert("a-1").uri.toString())
        assertEquals("predictivemaintain://orders/w-1", DeepLink.WorkOrder("w-1").uri.toString())
        assertEquals("predictivemaintain://subscription", DeepLink.Subscription.uri.toString())

        listOf(DeepLink.Alert("a-1"), DeepLink.WorkOrder("w-1"), DeepLink.Subscription).forEach {
            assertEquals(it, DeepLink.fromUri(it.uri))
        }
    }

    @Test
    fun `an id with special characters survives the uri`() {
        val link = DeepLink.Alert("a b/c")

        assertEquals(link, DeepLink.fromUri(Uri.parse(link.uri.toString())))
    }

    @Test
    fun `foreign uris are ignored`() {
        assertNull(DeepLink.fromUri(null))
        assertNull(DeepLink.fromUri(Uri.parse("https://alerts/a-1")))
        assertNull(DeepLink.fromUri(Uri.parse("predictivemaintain://alerts")))
        assertNull(DeepLink.fromUri(Uri.parse("predictivemaintain://unknown/1")))
    }

    @Test
    fun `our own intent carries the uri`() {
        val intent = Intent().setData(DeepLink.WorkOrder("w-9").uri)

        assertEquals(DeepLink.WorkOrder("w-9"), DeepLink.fromIntent(intent))
    }

    @Test
    fun `a notification shown by Firebase carries the data as extras`() {
        val intent = Intent().putExtra("alertId", "a-7").putExtra("google.message_id", "m1")

        assertEquals(DeepLink.Alert("a-7"), DeepLink.fromIntent(intent))
    }

    @Test
    fun `a plain launch has no link`() {
        assertNull(DeepLink.fromIntent(Intent()))
        assertNull(DeepLink.fromIntent(null))
    }

    @Test
    fun `only a manager can be taken to the subscription`() {
        assertTrue(DeepLink.Subscription.isAllowedFor(isManager = true))
        assertFalse(DeepLink.Subscription.isAllowedFor(isManager = false))
        assertTrue(DeepLink.Alert("a").isAllowedFor(isManager = false))
        assertTrue(DeepLink.WorkOrder("w").isAllowedFor(isManager = false))
    }
}
