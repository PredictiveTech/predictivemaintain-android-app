package pe.edu.upc.predictivemaintain.app.maintenance.application.policy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Actor
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import java.time.Instant

class WorkOrderActionPolicyTest {

    private val policy = WorkOrderActionPolicy()

    private val manager = Actor(userId = "mgr-1", isManager = true, isTechnician = false)
    private val technician = Actor(userId = "tech-1", isManager = false, isTechnician = true)
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

        // Manager
        assertTrue(policy.canAssign(manager, openOrder))
        assertTrue(policy.canAssign(manager, assignedOrder))
        assertFalse(policy.canAssign(manager, inProgressOrder))
        assertFalse(policy.canAssign(manager, completedOrder))
        assertFalse(policy.canAssign(manager, cancelledOrder))
        assertFalse(policy.canAssign(manager, unknownOrder))

        // Technician and Operator cannot assign regardless of status
        assertFalse(policy.canAssign(technician, openOrder))
        assertFalse(policy.canAssign(operator, openOrder))
    }

    private fun createOrderWithStatus(status: WorkOrderStatus): WorkOrder {
        return WorkOrder(
            id = WorkOrderId("wo-1"),
            alertId = AlertId("alert-1"),
            assetId = AssetId("asset-1"),
            assetCode = "AST-01",
            assetName = "Pump 1",
            severity = AlertSeverity.WARNING,
            assignedUserId = null,
            status = status,
            summary = null,
            openedAt = Instant.now(),
            completedAt = null,
            version = 1L
        )
    }
}
