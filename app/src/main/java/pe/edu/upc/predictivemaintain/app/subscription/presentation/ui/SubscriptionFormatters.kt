package pe.edu.upc.predictivemaintain.app.subscription.presentation.ui

import pe.edu.upc.predictivemaintain.app.core.ui.StatusTone
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.Subscription
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionStatus
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

internal fun formatMoney(amount: Double, currency: String, locale: Locale = Locale.getDefault()): String =
    try {
        NumberFormat.getCurrencyInstance(locale).apply { this.currency = Currency.getInstance(currency) }.format(amount)
    } catch (_: IllegalArgumentException) {
        // Unknown or missing currency code: show the number with whatever code the server sent.
        String.format(locale, "%.2f %s", amount, currency).trim()
    }

internal fun formatUsagePercent(percent: Double, locale: Locale = Locale.getDefault()): String =
    String.format(locale, "%.0f%%", percent)

/** Progress bar fill: 0 to 1 whatever the server sends (over 100% is possible after a downgrade). */
internal fun usageFraction(percent: Double): Float = (percent / 100.0).coerceIn(0.0, 1.0).toFloat()

internal enum class UsageLevel { NORMAL, NEAR_LIMIT, FULL }

/** The server decides when the plan is near its limit; a plan with no room left is shown as full. */
internal fun usageLevel(subscription: Subscription): UsageLevel = when {
    subscription.capacity.available <= 0 -> UsageLevel.FULL
    subscription.capacity.nearLimit -> UsageLevel.NEAR_LIMIT
    else -> UsageLevel.NORMAL
}

internal fun usageTone(level: UsageLevel): StatusTone? = when (level) {
    UsageLevel.NORMAL -> null
    UsageLevel.NEAR_LIMIT -> StatusTone.WARNING
    UsageLevel.FULL -> StatusTone.DANGER
}

internal fun statusTone(status: SubscriptionStatus): StatusTone = when (status) {
    SubscriptionStatus.ACTIVE -> StatusTone.SUCCESS
    SubscriptionStatus.SUSPENDED -> StatusTone.WARNING
    SubscriptionStatus.CANCELLED -> StatusTone.DANGER
    SubscriptionStatus.UNKNOWN -> StatusTone.NEUTRAL
}
