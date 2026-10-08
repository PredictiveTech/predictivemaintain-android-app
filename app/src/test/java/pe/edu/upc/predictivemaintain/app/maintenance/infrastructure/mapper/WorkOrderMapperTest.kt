package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.WorkOrderDto

class WorkOrderMapperTest {

    @Test
    fun `toDomain with valid dto maps all fields successfully`() {
        val dto = WorkOrderDto(
            id = "wo-1",
            alertId = "alert-1",
            assetId = "asset-1",
            assetCode = "AST-01",
            assetName = "Pump 1",
            severity = "WARNING",
            assignedUserId = "tech-1",
            status = "OPEN",
            summary = "Fix motor",
            openedAt = "2026-03-01T10:00:00Z",
            completedAt = "2026-03-01T12:00:00Z",
            version = 1L
        )

        val result = WorkOrderMapper.toDomain(dto)

        assertTrue(result is Outcome.Success)
        val order = (result as Outcome.Success).data
        assertEquals("wo-1", order.id.value)
        assertEquals("alert-1", order.alertId.value)
        assertEquals("asset-1", order.assetId.value)
        assertEquals("AST-01", order.assetCode)
        assertEquals("Pump 1", order.assetName)
        assertEquals(AlertSeverity.WARNING, order.severity)
        assertEquals("tech-1", order.assignedUserId?.value)
        assertEquals(WorkOrderStatus.OPEN, order.status)
        assertEquals("Fix motor", order.summary)
        assertEquals("2026-03-01T10:00:00Z", order.openedAt.toString())
        assertEquals("2026-03-01T12:00:00Z", order.completedAt?.toString())
        assertEquals(1L, order.version)
    }

    @Test
    fun `toDomain with null assignedUserId summary and completedAt maps successfully`() {
        val dto = WorkOrderDto(
            id = "wo-1",
            alertId = "alert-1",
            assetId = "asset-1",
            assetCode = "AST-01",
            assetName = "Pump 1",
            severity = "CRITICAL",
            assignedUserId = null,
            status = "OPEN",
            summary = null,
            openedAt = "2026-03-01T10:00:00Z",
            completedAt = null,
            version = 0L
        )

        val result = WorkOrderMapper.toDomain(dto)

        assertTrue(result is Outcome.Success)
        val order = (result as Outcome.Success).data
        assertNull(order.assignedUserId)
        assertNull(order.summary)
        assertNull(order.completedAt)
    }

    @Test
    fun `toDomain with unknown status maps status to UNKNOWN`() {
        val dto = WorkOrderDto(
            id = "wo-1",
            alertId = "alert-1",
            assetId = "asset-1",
            assetCode = "AST-01",
            assetName = "Pump 1",
            severity = "UNKNOWN_SEV",
            assignedUserId = null,
            status = "UNKNOWN_STAT",
            summary = null,
            openedAt = "2026-03-01T10:00:00Z",
            completedAt = null,
            version = 0L
        )

        val result = WorkOrderMapper.toDomain(dto)

        assertTrue(result is Outcome.Success)
        val order = (result as Outcome.Success).data
        assertEquals(WorkOrderStatus.UNKNOWN, order.status)
        assertEquals(AlertSeverity.UNKNOWN, order.severity)
    }

    @Test
    fun `toDomain with invalid date fails with InvalidResponse`() {
        val dto = WorkOrderDto(
            id = "wo-1",
            alertId = "alert-1",
            assetId = "asset-1",
            assetCode = "AST-01",
            assetName = "Pump 1",
            severity = "WARNING",
            assignedUserId = null,
            status = "OPEN",
            summary = null,
            openedAt = "invalid-date",
            completedAt = null,
            version = 0L
        )

        val result = WorkOrderMapper.toDomain(dto)

        assertTrue(result is Outcome.Failure)
        val error = (result as Outcome.Failure).error
        assertTrue(error is AppError.InvalidResponse)
        assertEquals("openedAt", (error as AppError.InvalidResponse).field)
    }
}
