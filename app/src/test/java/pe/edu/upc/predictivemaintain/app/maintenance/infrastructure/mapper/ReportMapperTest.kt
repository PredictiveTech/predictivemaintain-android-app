package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.mapper

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetEventType
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AssetEventDto
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AvailabilityReportDto
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.ComparisonRowDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ReportMapperTest {

    private fun availability(
        events: List<AssetEventDto> = emptyList(),
        percent: Double? = 97.5,
        downtime: Long? = 3600
    ) = AvailabilityReportDto(
        assetId = "a-1", assetCode = "PUMP-01",
        from = "2026-10-24T00:00:00Z", to = "2026-11-23T15:00:00Z",
        availabilityPercent = percent, downtimeSeconds = downtime, periodSeconds = 2_000_000,
        hasEvents = events.isNotEmpty(), events = events
    )

    private fun failureField(result: Outcome<*>): String =
        ((result as Outcome.Failure).error as AppError.InvalidResponse).field

    @Test
    fun `availability maps every field`() {
        val result = ReportMapper.toDomain(availability())

        val report = (result as Outcome.Success).data
        assertEquals("a-1", report.assetId)
        assertEquals("PUMP-01", report.assetCode)
        assertEquals(97.5, report.availabilityPercent, 0.0)
        assertEquals(3600L, report.downtimeSeconds)
        assertEquals(2_000_000L, report.periodSeconds)
        assertEquals(Instant.parse("2026-10-24T00:00:00Z"), report.from)
    }

    @Test
    fun `without events the list is empty`() {
        val report = (ReportMapper.toDomain(availability(percent = 100.0, downtime = 0)) as Outcome.Success).data

        assertTrue(report.events.isEmpty())
        assertTrue(report.downtimeIntervals.isEmpty())
        assertEquals(100.0, report.availabilityPercent, 0.0)
    }

    @Test
    fun `events are split by type and sorted newest first`() {
        val events = listOf(
            AssetEventDto("DOWNTIME", "d-1", "2026-11-01T10:00:00Z", "2026-11-01T12:00:00Z"),
            AssetEventDto("ALERT", "al-1", "2026-11-05T08:00:00Z", null, severity = "CRITICAL", status = "OPEN"),
            AssetEventDto("downtime", "d-2", "2026-11-10T09:00:00Z", null),
            AssetEventDto("SOMETHING_NEW", "x-1", "2026-11-02T09:00:00Z", null)
        )

        val report = (ReportMapper.toDomain(availability(events)) as Outcome.Success).data

        assertEquals(listOf("d-2", "al-1", "x-1", "d-1"), report.events.map { it.referenceId })
        assertEquals(listOf("d-2", "d-1"), report.downtimeIntervals.map { it.referenceId })
        assertEquals(listOf("al-1"), report.alerts.map { it.referenceId })
        assertEquals(AssetEventType.UNKNOWN, report.events.first { it.referenceId == "x-1" }.type)
        assertNull(report.downtimeIntervals.first().endedAt) // a stop in progress has no end
        assertEquals("CRITICAL", report.alerts.single().severity)
    }

    @Test
    fun `availability without percent is invalid`() {
        assertEquals("availabilityPercent", failureField(ReportMapper.toDomain(availability(percent = null))))
    }

    @Test
    fun `availability outside 0 to 100 is invalid`() {
        assertEquals("availabilityPercent", failureField(ReportMapper.toDomain(availability(percent = 100.5))))
        assertEquals("availabilityPercent", failureField(ReportMapper.toDomain(availability(percent = -1.0))))
    }

    @Test
    fun `negative downtime is invalid`() {
        assertEquals("downtimeSeconds", failureField(ReportMapper.toDomain(availability(downtime = -5))))
    }

    @Test
    fun `event with a bad date is invalid`() {
        val events = listOf(AssetEventDto("ALERT", "al-1", "yesterday", null))

        assertEquals("events.occurredAt", failureField(ReportMapper.toDomain(availability(events))))
    }

    @Test
    fun `comparison maps rows in the given order`() {
        val dtos = listOf(
            ComparisonRowDto("a-1", "PUMP-01", "Main pump", 4, 1, 2, 7200, 96.0),
            ComparisonRowDto("a-2", "FAN-02", null, 0, 0, 0, 0, 100.0)
        )

        val rows = (ReportMapper.toDomain(dtos) as Outcome.Success).data

        assertEquals(listOf("PUMP-01", "FAN-02"), rows.map { it.assetCode })
        assertEquals(4, rows[0].alerts)
        assertEquals(1, rows[0].criticalAlerts)
        assertEquals(2, rows[0].stops)
        assertEquals(7200L, rows[0].downtimeSeconds)
        assertEquals("", rows[1].assetName)
        assertEquals(100.0, rows[1].availabilityPercent, 0.0)
    }

    @Test
    fun `empty comparison is an empty list`() {
        assertEquals(emptyList<Any>(), (ReportMapper.toDomain(emptyList<ComparisonRowDto>()) as Outcome.Success).data)
    }

    @Test
    fun `comparison row without code is invalid`() {
        val result = ReportMapper.toDomain(listOf(ComparisonRowDto("a-1", " ", "x", 0, 0, 0, 0, 100.0)))

        assertEquals("assetCode", failureField(result))
    }

    @Test
    fun `comparison row with missing counter is invalid`() {
        val result = ReportMapper.toDomain(listOf(ComparisonRowDto("a-1", "P", "x", null, 0, 0, 0, 100.0)))

        assertEquals("alerts", failureField(result))
    }
}
