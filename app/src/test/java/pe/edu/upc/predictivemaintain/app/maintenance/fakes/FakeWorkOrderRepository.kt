package pe.edu.upc.predictivemaintain.app.maintenance.fakes

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.WorkOrderRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.TechnicianId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus

class FakeWorkOrderRepository : WorkOrderRepository {

    var listWorkOrdersResult: Outcome<PageResult<WorkOrder>> = Outcome.Success(
        PageResult(items = emptyList(), page = 0, totalPages = 1, totalElements = 0)
    )
    var getWorkOrderResult: Outcome<WorkOrder>? = null
    var createWorkOrderResult: Outcome<WorkOrder>? = null
    var assignWorkOrderResult: Outcome<WorkOrder>? = null
    var startWorkOrderResult: Outcome<WorkOrder>? = null
    var completeWorkOrderResult: Outcome<WorkOrder>? = null

    var lastListStatusFilter: WorkOrderStatus? = null
    var lastListPage: Int? = null

    override suspend fun listWorkOrders(
        status: WorkOrderStatus?,
        page: Int,
        size: Int
    ): Outcome<PageResult<WorkOrder>> {
        lastListStatusFilter = status
        lastListPage = page
        return listWorkOrdersResult
    }

    override suspend fun getWorkOrder(id: WorkOrderId): Outcome<WorkOrder> {
        return getWorkOrderResult ?: error("getWorkOrderResult not set")
    }

    override suspend fun createWorkOrder(alertId: AlertId): Outcome<WorkOrder> {
        return createWorkOrderResult ?: error("createWorkOrderResult not set")
    }

    override suspend fun assignWorkOrder(
        id: WorkOrderId,
        technicianId: TechnicianId,
        expectedVersion: Long
    ): Outcome<WorkOrder> {
        return assignWorkOrderResult ?: error("assignWorkOrderResult not set")
    }

    override suspend fun startWorkOrder(
        id: WorkOrderId,
        expectedVersion: Long
    ): Outcome<WorkOrder> {
        return startWorkOrderResult ?: error("startWorkOrderResult not set")
    }

    override suspend fun completeWorkOrder(
        id: WorkOrderId,
        summary: String,
        expectedVersion: Long
    ): Outcome<WorkOrder> {
        return completeWorkOrderResult ?: error("completeWorkOrderResult not set")
    }
}
