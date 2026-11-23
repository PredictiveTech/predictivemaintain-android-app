package pe.edu.upc.predictivemaintain.app.subscription.domain.entity

import java.time.Instant

enum class SubscriptionStatus {
    ACTIVE, SUSPENDED, CANCELLED, UNKNOWN;

    companion object {
        fun fromString(value: String?): SubscriptionStatus = when (value?.uppercase()) {
            "ACTIVE" -> ACTIVE
            "SUSPENDED" -> SUSPENDED
            "CANCELLED" -> CANCELLED
            else -> UNKNOWN
        }
    }
}

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val assetLimit: Int,
    val amount: Double,
    val currency: String
)

/** Assets used against the plan limit. [nearLimit] is decided by the server (from 90%). */
data class SubscriptionCapacity(
    val limit: Int,
    val used: Long,
    val available: Int,
    val usagePercent: Double,
    val nearLimit: Boolean
)

/** [expiresSoon] is decided by the server (7 days or fewer). */
data class SubscriptionExpiry(
    val daysRemaining: Long,
    val expiresSoon: Boolean,
    val expired: Boolean
)

data class Subscription(
    val id: String,
    val status: SubscriptionStatus,
    val startsAt: Instant?,
    /** End of the current period: the date the subscription has to be renewed. */
    val endsAt: Instant,
    val monitoringAllowed: Boolean,
    val plan: SubscriptionPlan,
    val capacity: SubscriptionCapacity,
    val expiry: SubscriptionExpiry
) {
    /** Assets that would have to be deactivated before moving to [target]; 0 when it already fits. */
    fun assetsToDeactivateFor(target: SubscriptionPlan): Int =
        (capacity.used - target.assetLimit).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
}
