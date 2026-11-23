package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class DateRangeTest {

    private val utc = ZoneOffset.UTC
    private val now = Instant.parse("2026-11-23T15:30:00Z")
    private val today = LocalDate.of(2026, 11, 23)

    @Test
    fun `default range is the last 30 days ending today`() {
        val range = DateRange.lastDays(today)

        assertEquals(LocalDate.of(2026, 10, 24), range.start)
        assertEquals(today, range.end)
        assertNull(range.validate(utc, now))
    }

    @Test
    fun `period starts at the start of the first day`() {
        val period = DateRange(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10)).toPeriod(utc, now)

        assertEquals(Instant.parse("2026-11-01T00:00:00Z"), period.from)
        assertEquals(Instant.parse("2026-11-11T00:00:00Z"), period.to) // end of the last day, in the past
    }

    @Test
    fun `period end never goes past now`() {
        val period = DateRange(LocalDate.of(2026, 11, 1), today).toPeriod(utc, now)

        assertEquals(now, period.to)
    }

    @Test
    fun `period uses the given time zone`() {
        val lima = ZoneId.of("America/Lima") // UTC-5, no DST
        val period = DateRange(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1)).toPeriod(lima, now)

        assertEquals(Instant.parse("2026-11-01T05:00:00Z"), period.from)
        assertEquals(Instant.parse("2026-11-02T05:00:00Z"), period.to)
    }

    @Test
    fun `start after end is rejected`() {
        val range = DateRange(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 1))

        assertEquals(ReportRangeError.START_AFTER_END, range.validate(utc, now))
    }

    @Test
    fun `a range that starts after now is rejected`() {
        val range = DateRange(today.plusDays(1), today.plusDays(2))

        assertEquals(ReportRangeError.STARTS_IN_FUTURE, range.validate(utc, now))
    }

    @Test
    fun `366 days is accepted and 367 is rejected`() {
        val ok = DateRange(today.minusDays(365), today.minusDays(1)) // 365 whole days
        val longest = DateRange(LocalDate.of(2025, 11, 22), LocalDate.of(2026, 11, 22)) // 366 whole days
        val tooLong = DateRange(LocalDate.of(2025, 11, 21), LocalDate.of(2026, 11, 22)) // 367 whole days

        assertNull(ok.validate(utc, now))
        assertNull(longest.validate(utc, now))
        assertEquals(ReportRangeError.TOO_LONG, tooLong.validate(utc, now))
    }
}
