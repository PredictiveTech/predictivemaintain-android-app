package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class ReportRangeError { START_AFTER_END, STARTS_IN_FUTURE, TOO_LONG }

/** The exact instants sent to the reports endpoints. */
data class ReportPeriod(val from: Instant, val to: Instant)

/**
 * Days chosen by the user (both inclusive). The backend takes instants, caps the end at "now" and
 * refuses ranges longer than 366 days, so [toPeriod] applies the same rules and [validate] reports
 * a problem before any request is made.
 */
data class DateRange(val start: LocalDate, val end: LocalDate) {

    fun toPeriod(zone: ZoneId, now: Instant): ReportPeriod {
        val from = start.atStartOfDay(zone).toInstant()
        val endOfLastDay = end.plusDays(1).atStartOfDay(zone).toInstant()
        return ReportPeriod(from = from, to = minOf(endOfLastDay, now))
    }

    fun validate(zone: ZoneId, now: Instant): ReportRangeError? {
        if (start.isAfter(end)) return ReportRangeError.START_AFTER_END
        val period = toPeriod(zone, now)
        if (!period.from.isBefore(now)) return ReportRangeError.STARTS_IN_FUTURE
        if (Duration.between(period.from, period.to) > Duration.ofDays(MAX_DAYS)) return ReportRangeError.TOO_LONG
        return null
    }

    companion object {
        const val MAX_DAYS = 366L
        const val DEFAULT_DAYS = 30L

        /** The last 30 days, today included. */
        fun lastDays(today: LocalDate, days: Long = DEFAULT_DAYS) = DateRange(today.minusDays(days), today)
    }
}
