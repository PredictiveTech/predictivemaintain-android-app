package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.implementation

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.network.ErrorParser
import pe.edu.upc.predictivemaintain.app.core.network.safeApiCall
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.SessionRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.WorkOrderRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.PendingOperation
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.PendingOperationQueue
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncOperationType
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local.WorkOrderCache
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
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class WorkOrderRepositoryImpl @Inject constructor(
    private val apiService: WorkOrderApiService,
    private val errorParser: ErrorParser,
    private val cache: WorkOrderCache,
    private val pendingQueue: PendingOperationQueue,
    private val sessionRepository: SessionRepository
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
            is Outcome.Success -> WorkOrderMapper.toDomain(apiResult.data).also { mapped ->
                if (mapped is Outcome.Success) cache.putAll(mapped.data.items)
            }
            is Outcome.Failure -> apiResult
        }
    }

    override suspend fun getWorkOrder(id: WorkOrderId): Outcome<WorkOrder> {
        return when (val apiResult = safeApiCall(errorParser) {
            apiService.getWorkOrder(id.value)
        }) {
            is Outcome.Success -> cached(WorkOrderMapper.toDomain(apiResult.data))
            is Outcome.Failure -> {
                val local = if (apiResult.error is AppError.Network) cache.get(id.value) else null
                if (local != null) Outcome.Success(local) else apiResult
            }
        }
    }

    override suspend fun createWorkOrder(alertId: AlertId): Outcome<WorkOrder> {
        val request = CreateWorkOrderRequestDto(alertId = alertId.value)
        return when (val apiResult = safeApiCall(errorParser) {
            apiService.createWorkOrder(request)
        }) {
            is Outcome.Success -> cached(WorkOrderMapper.toDomain(apiResult.data))
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
            is Outcome.Success -> cached(WorkOrderMapper.toDomain(apiResult.data))
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
            is Outcome.Success -> cached(WorkOrderMapper.toDomain(apiResult.data))
            is Outcome.Failure -> queueIfOffline(apiResult, id, SyncOperationType.START, null)
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
            is Outcome.Success -> cached(WorkOrderMapper.toDomain(apiResult.data))
            is Outcome.Failure -> queueIfOffline(apiResult, id, SyncOperationType.COMPLETE, summary)
        }
    }

    private fun cached(result: Outcome<WorkOrder>): Outcome<WorkOrder> {
        if (result is Outcome.Success) cache.put(result.data)
        return result
    }

    /**
     * US-28. When the call failed because there is no connection, the action is stored in the queue to be
     * sent later and the order is returned with its optimistic state. Any other failure is returned as is.
     */
    private suspend fun queueIfOffline(
        failure: Outcome.Failure,
        id: WorkOrderId,
        type: SyncOperationType,
        summary: String?
    ): Outcome<WorkOrder> {
        if (failure.error !is AppError.Network) return failure
        val current = cache.get(id.value) ?: return failure
        val userId = sessionRepository.currentSession()?.userId?.value ?: return failure

        val performedAt = Instant.now()
        pendingQueue.enqueue(
            PendingOperation(
                operationId = UUID.randomUUID().toString(),
                workOrderId = id.value,
                userId = userId,
                type = type,
                performedAt = performedAt,
                summary = summary
            )
        )
        val optimistic = when (type) {
            SyncOperationType.START -> current.copy(status = WorkOrderStatus.IN_PROGRESS)
            SyncOperationType.COMPLETE -> current.copy(
                status = WorkOrderStatus.COMPLETED,
                summary = summary,
                completedAt = performedAt
            )
        }
        cache.put(optimistic)
        return Outcome.Success(optimistic)
    }
}
