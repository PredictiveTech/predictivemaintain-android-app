package pe.edu.upc.predictivemaintain.app.navigation

import android.content.Intent
import android.net.Uri

/**
 * Screens a push notification can open. A link is carried in an Intent as a `predictivemaintain://...` URI that
 * matches the `navDeepLink` of the destination, or, when Firebase itself shows the notification, as the
 * raw FCM data entries (extras), which [fromData] understands too.
 *
 * Payload keys read here (the backend payload is not changed; these are the keys the code looks for, so
 * confirm them against a real push):
 *  - `type` = `SUBSCRIPTION_EXPIRING`                     -> subscription
 *  - `workOrderId`                                         -> work order
 *  - `alertId`, or `type` = `ALERT_RAISED` with `subjectId` -> alert
 */
sealed interface DeepLink {

    data object Subscription : DeepLink
    data class Alert(val id: String) : DeepLink
    data class WorkOrder(val id: String) : DeepLink

    /** The URI that the matching `navDeepLink` of AppNavigation declares. */
    val uri: Uri
        get() = Uri.Builder().scheme(SCHEME).authority(
            when (this) {
                Subscription -> HOST_SUBSCRIPTION
                is Alert -> HOST_ALERTS
                is WorkOrder -> HOST_ORDERS
            }
        ).apply {
            when (this@DeepLink) {
                Subscription -> Unit
                is Alert -> appendPath(id)
                is WorkOrder -> appendPath(id)
            }
        }.build()

    /** The subscription is for the Maintenance Manager only; alerts and orders are open to every role. */
    fun isAllowedFor(isManager: Boolean): Boolean = this !is Subscription || isManager

    companion object {
        const val SCHEME = "predictivemaintain"
        const val HOST_ALERTS = "alerts"
        const val HOST_ORDERS = "orders"
        const val HOST_SUBSCRIPTION = "subscription"

        const val KEY_TYPE = "type"
        const val KEY_ALERT_ID = "alertId"
        const val KEY_WORK_ORDER_ID = "workOrderId"
        const val KEY_SUBJECT_ID = "subjectId"
        const val TYPE_SUBSCRIPTION_EXPIRING = "SUBSCRIPTION_EXPIRING"
        const val TYPE_ALERT_RAISED = "ALERT_RAISED"

        fun fromData(data: Map<String, String?>): DeepLink? {
            fun value(key: String) = data[key]?.trim()?.takeIf { it.isNotEmpty() }
            val type = value(KEY_TYPE)?.uppercase()
            return when {
                type == TYPE_SUBSCRIPTION_EXPIRING -> Subscription
                value(KEY_WORK_ORDER_ID) != null -> WorkOrder(value(KEY_WORK_ORDER_ID)!!)
                value(KEY_ALERT_ID) != null -> Alert(value(KEY_ALERT_ID)!!)
                type == TYPE_ALERT_RAISED && value(KEY_SUBJECT_ID) != null -> Alert(value(KEY_SUBJECT_ID)!!)
                else -> null
            }
        }

        fun fromUri(uri: Uri?): DeepLink? {
            if (uri == null || uri.scheme != SCHEME) return null
            val id = uri.pathSegments.firstOrNull()?.takeIf { it.isNotBlank() }
            return when (uri.host) {
                HOST_SUBSCRIPTION -> Subscription
                HOST_ALERTS -> id?.let(::Alert)
                HOST_ORDERS -> id?.let(::WorkOrder)
                else -> null
            }
        }

        /** Our own PendingIntents carry the URI; a notification shown by Firebase carries the data as extras. */
        fun fromIntent(intent: Intent?): DeepLink? {
            if (intent == null) return null
            fromUri(intent.data)?.let { return it }
            val extras = intent.extras ?: return null
            return fromData(extras.keySet().associateWith { key -> extras.getString(key) })
        }
    }
}
