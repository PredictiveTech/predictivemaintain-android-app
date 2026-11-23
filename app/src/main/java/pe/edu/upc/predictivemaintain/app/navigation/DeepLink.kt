package pe.edu.upc.predictivemaintain.app.navigation

import android.content.Intent

/**
 * Destinations a push notification can open (US-19).
 *
 * The push carries its kind in the data entry [EXTRA_TYPE]. The value for the expiry warning is the
 * name of the `SUBSCRIPTION_EXPIRING` notification type of the backend API; confirm the push payload
 * uses the same key and value (the OpenAPI spec documents the notification history, not the FCM data).
 */
enum class DeepLink {
    SUBSCRIPTION;

    /** The route to open, or null when this user must not see it (the screen is manager only). */
    fun targetRoute(isManager: Boolean): String? = when (this) {
        SUBSCRIPTION -> if (isManager) Screen.Subscription.route else null
    }

    companion object {
        const val EXTRA_TYPE = "type"
        const val TYPE_SUBSCRIPTION_EXPIRING = "SUBSCRIPTION_EXPIRING"

        fun fromType(type: String?): DeepLink? = when (type?.trim()?.uppercase()) {
            TYPE_SUBSCRIPTION_EXPIRING -> SUBSCRIPTION
            else -> null
        }

        /** Works for the intent built by our own service and for the one Firebase builds (data as extras). */
        fun fromIntent(intent: Intent?): DeepLink? = fromType(intent?.getStringExtra(EXTRA_TYPE))
    }
}
