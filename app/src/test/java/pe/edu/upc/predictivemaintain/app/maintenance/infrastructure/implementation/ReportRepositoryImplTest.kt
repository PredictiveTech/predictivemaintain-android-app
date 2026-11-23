package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.implementation

import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.ReportPeriod
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class ReportRepositoryImplTest {

    @Test
    fun `history file name uses the dates of the period`() {
        val period = ReportPeriod(Instant.parse("2026-10-24T00:00:00Z"), Instant.parse("2026-11-23T15:30:00Z"))

        assertEquals(
            "maintenance-history_2026-10-24_2026-11-23.csv",
            ReportRepositoryImpl.historyFileName(period, ZoneOffset.UTC)
        )
    }
}
