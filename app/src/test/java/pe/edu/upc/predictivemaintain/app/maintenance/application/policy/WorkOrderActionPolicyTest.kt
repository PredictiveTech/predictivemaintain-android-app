package pe.edu.upc.predictivemaintain.app.maintenance.application.policy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Actor
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.TechnicianId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import java.time.Instant

class WorkOrderActionPolicyTest {

    private val policy = WorkOrderActionPolicy()

    private val manager = Actor(userId = "mgr-1", isManager = true, isTechnician = false)
    private val technician = Actor(userId = "tech-1", isManager = false, isTechnician = true)
    private val otherTechnician = Actor(userId = "tech-2", isManager = false, isTechnician = true)
    private val operator = Actor(userId = "op-1", isManager = false, isTechnician = false)

    @Test
    fun `canCreate returns true for manager and technician only when alert is confirmed`() {
        assertTrue(policy.canCreate(manager, alertIsConfirmed = true))
        assertTrue(policy.canCreate(technician, alertIsConfirmed = true))
        assertFalse(policy.canCreate(operator, alertIsConfirmed = true))

        assertFalse(policy.canCreate(manager, alertIsConfirmed = false))
        assertFalse(policy.canCreate(technician, alertIsConfirmed = false))
        assertFalse(policy.canCreate(operator, alertIsConfirmed = false))
    }

    @Test
    fun `canAssign returns true for manager on OPEN and ASSIGNED status only`() {
        val openOrder = createOrderWithStatus(WorkOrderStatus.OPEN)
        val assignedOrder = createOrderWithStatus(WorkOrderStatus.ASSIGNED)
        val inProgressOrder = createOrderWithStatus(WorkOrderStatus.IN_PROGRESS)
        val completedOrder = createOrderWithStatus(WorkOrderStatus.COMPLETED)
        val cancelledOrder = createOrderWithStatus(WorkOrderStatus.CANCELLED)
        val unknownOrder = createOrderWithStatus(WorkOrderStatus.UNKNOWN)

        assertTrue(policy.canAssign(manager, openOrder))
        assertTrue(policy.canAssign(manager, assignedOrder))
        assertFalse(policy.canAssign(manager, inProgressOrder))
        assertFalse(policy.canAssign(manager, completedOrder))
        assertFalse(policy.canAssign(manager, cancelledOrder))
        assertFalse(policy.canAssign(manager, unknownOrder))

        assertFalse(policy.canAssign(technician, openOrder))
        assertFalse(policy.canAssign(operator, openOrder))
    }

    @Test
    fun `canStart returns true for assigned technician on ASSIGNED status only`() {
        val assignedOrder = createOrderWithStatus(WorkOrderStatus.ASSIGNED, assignedTechId = "tech-1")
        val openOrder = createOrderWithStatus(WorkOrderStatus.OPEN, assignedTechId = "tech-1")
        val inProgressOrder = createOrderWithStatus(WorkOrderStatus.IN_PROGRESS, assignedTechId = "tech-1")

        // Assigned technician
        assertTrue(policy.canStart(technician, assignedOrder))
        assertFalse(policy.canStart(technician, openOrder))
        assertFalse(policy.canStart(technician, inProgressOrder))

        // Another technician
        assertFalse(policy.canStart(otherTechnician, assignedOrder))

        // Manager
        assertFalse(policy.canStart(manager, assignedOrder))
    }

    @Test
    fun `canComplete returns true for assigned technician on IN_PROGRESS status only`() {
        val inProgressOrder = createOrderWithStatus(WorkOrderStatus.IN_PROGRESS, assignedTechId = "tech-1")
        val assignedOrder = createOrderWithStatus(WorkOrderStatus.ASSIGNED, assignedTechId = "tech-1")
        val completedOrder = createOrderWithStatus(WorkOrderStatus.COMPLETED, assignedTechId = "tech-1")

        // Assigned technician
        assertTrue(policy.canComplete(technician, inProgressOrder))
        assertFalse(policy.canComplete(technician, assignedOrder))
        assertFalse(policy.canComplete(technician, completedOrder))

        // Another technician
        assertFalse(policy.canComplete(otherTechnician, inProgressOrder))

        // Manager
        assertFalse(policy.canComplete(manager, inProgressOrder))
    }

    private fun createOrderWithStatus(
        status: WorkOrderStatus,
        assignedTechId: String? = null
    ): WorkOrder {
        return WorkOrder(
            id = WorkOrderId("wo-1"),
            alertId = AlertId("alert-1"),
            assetId = AssetId("asset-1"),
            assetCode = "AST-01",
            assetName = "Pump 1",
            severity = AlertSeverity.WARNING,
            assignedUserId = assignedTechId?.let { TechnicianId(it) },
            status = status,
            summary = null,
            openedAt = Instant.now(),
            completedAt = null,
            version = 1L
        )
    }
}
