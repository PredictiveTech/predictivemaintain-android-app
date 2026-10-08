package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertStatus
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AlertDto

class AlertMapperTest {

    @Test
    fun `toDomain with valid dto succeeds and maps unknown severity status to UNKNOWN`() {
        val dto = AlertDto(
            id = "alert-1",
            assetId = "asset-1",
            assetCode = "AST-001",
            assetName = "Pump 1",
            severity = "UNKNOWN_SEV",
            status = "UNKNOWN_STAT",
            raisedAt = "2026-03-01T10:00:00Z",
            version = 1L,
            diagnostic = null
        )

        val result = AlertMapper.toDomain(dto)

        assertTrue(result is Outcome.Success)
        val alert = (result as Outcome.Success).data
        assertEquals("alert-1", alert.id.value)
        assertEquals(AlertSeverity.UNKNOWN, alert.severity)
        assertEquals(AlertStatus.UNKNOWN, alert.status)
        assertNull(alert.diagnostic)
    }

    @Test
    fun `toDomain with invalid date fails with InvalidResponse`() {
        val dto = AlertDto(
            id = "alert-1",
            assetId = "asset-1",
            assetCode = "AST-001",
            assetName = "Pump 1",
            raisedAt = "invalid-date",
            version = 1L
        )

        val result = AlertMapper.toDomain(dto)

        assertTrue(result is Outcome.Failure)
        val error = (result as Outcome.Failure).error
        assertTrue(error is AppError.InvalidResponse)
        assertEquals("raisedAt", (error as AppError.InvalidResponse).field)
    }
}
