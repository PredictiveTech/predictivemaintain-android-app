package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.implementation

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.network.ErrorParser
import pe.edu.upc.predictivemaintain.app.core.network.safeApiCall
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.WorkOrderRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.TechnicianId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.mapper.WorkOrderMapper
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AssignWorkOrderRequestDto
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.CompleteWorkOrderRequestDto
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.CreateWorkOrderRequestDto
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.StartWorkOrderRequestDto
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.WorkOrderApiService
import javax.inject.Inject

class WorkOrderRepositoryImpl @Inject constructor(
    private val apiService: WorkOrderApiService,
    private val errorParser: ErrorParser
) : WorkOrderRepository {

    override suspend fun listWorkOrders(
        status: WorkOrderStatus?,
        page: Int,
        size: Int
    ): Outcome<PageResult<WorkOrder>> {
        val statusQuery = WorkOrderMapper.statusToApiQuery(status)
        return when (val apiResult = safeApiCall(errorParser) {
            apiService.getWorkOrders(status = statusQuery, page = page, size = size)
        }) {
            is Outcome.Success -> WorkOrderMapper.toDomain(apiResult.data)
            is Outcome.Failure -> apiResult
        }
    }

    override suspend fun getWorkOrder(id: WorkOrderId): Outcome<WorkOrder> {
        return when (val apiResult = safeApiCall(errorParser) {
            apiService.getWorkOrder(id.value)
        }) {
            is Outcome.Success -> WorkOrderMapper.toDomain(apiResult.data)
            is Outcome.Failure -> apiResult
        }
    }

    override suspend fun createWorkOrder(alertId: AlertId): Outcome<WorkOrder> {
        val request = CreateWorkOrderRequestDto(alertId = alertId.value)
        return when (val apiResult = safeApiCall(errorParser) {
            apiService.createWorkOrder(request)
        }) {
            is Outcome.Success -> WorkOrderMapper.toDomain(apiResult.data)
            is Outcome.Failure -> apiResult
        }
    }

    override suspend fun assignWorkOrder(
        id: WorkOrderId,
        technicianId: TechnicianId,
        expectedVersion: Long
    ): Outcome<WorkOrder> {
        val request = AssignWorkOrderRequestDto(
            technicianId = technicianId.value,
            expectedVersion = expectedVersion
        )
        return when (val apiResult = safeApiCall(errorParser) {
            apiService.assignWorkOrder(id.value, request)
        }) {
            is Outcome.Success -> WorkOrderMapper.toDomain(apiResult.data)
            is Outcome.Failure -> apiResult
        }
    }

    override suspend fun startWorkOrder(
        id: WorkOrderId,
        expectedVersion: Long
    ): Outcome<WorkOrder> {
        val request = StartWorkOrderRequestDto(expectedVersion = expectedVersion)
        return when (val apiResult = safeApiCall(errorParser) {
            apiService.startWorkOrder(id.value, request)
        }) {
            is Outcome.Success -> WorkOrderMapper.toDomain(apiResult.data)
            is Outcome.Failure -> apiResult
        }
    }

    override suspend fun completeWorkOrder(
        id: WorkOrderId,
        summary: String,
        expectedVersion: Long
    ): Outcome<WorkOrder> {
        val request = CompleteWorkOrderRequestDto(
            status = "COMPLETED",
            summary = summary,
            expectedVersion = expectedVersion
        )
        return when (val apiResult = safeApiCall(errorParser) {
            apiService.completeWorkOrder(id.value, request)
        }) {
            is Outcome.Success -> WorkOrderMapper.toDomain(apiResult.data)
            is Outcome.Failure -> apiResult
        }
    }
}
