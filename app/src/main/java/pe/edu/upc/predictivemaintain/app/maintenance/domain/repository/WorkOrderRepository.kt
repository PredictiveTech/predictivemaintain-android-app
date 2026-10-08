package pe.edu.upc.predictivemaintain.app.maintenance.domain.repository

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.TechnicianId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus

interface WorkOrderRepository {
    suspend fun listWorkOrders(
        status: WorkOrderStatus?,
        page: Int,
        size: Int
    ): Outcome<PageResult<WorkOrder>>

    suspend fun getWorkOrder(id: WorkOrderId): Outcome<WorkOrder>

    suspend fun createWorkOrder(alertId: AlertId): Outcome<WorkOrder>

    suspend fun assignWorkOrder(
        id: WorkOrderId,
        technicianId: TechnicianId,
        expectedVersion: Long
    ): Outcome<WorkOrder>

    suspend fun startWorkOrder(
        id: WorkOrderId,
        expectedVersion: Long
    ): Outcome<WorkOrder>

    suspend fun completeWorkOrder(
        id: WorkOrderId,
        summary: String,
        expectedVersion: Long
    ): Outcome<WorkOrder>
}
