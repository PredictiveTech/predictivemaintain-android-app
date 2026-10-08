package pe.edu.upc.predictivemaintain.app.maintenance.application.policy

import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Actor
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import javax.inject.Inject

class WorkOrderActionPolicy @Inject constructor() {

    fun canCreate(actor: Actor, alertIsConfirmed: Boolean): Boolean {
        if (!alertIsConfirmed) return false
        return actor.isManager || actor.isTechnician
    }

    fun canAssign(actor: Actor, order: WorkOrder): Boolean {
        if (!actor.isManager) return false
        return when (order.status) {
            WorkOrderStatus.OPEN, WorkOrderStatus.ASSIGNED -> true
            else -> false
        }
    }

    fun canStart(actor: Actor, order: WorkOrder): Boolean {
        if (!actor.isTechnician) return false
        return order.status == WorkOrderStatus.ASSIGNED && order.assignedUserId?.value == actor.userId
    }

    fun canComplete(actor: Actor, order: WorkOrder): Boolean {
        if (!actor.isTechnician) return false
        return order.status == WorkOrderStatus.IN_PROGRESS && order.assignedUserId?.value == actor.userId
    }
}
