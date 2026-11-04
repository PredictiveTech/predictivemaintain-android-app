package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.implementation

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.network.ErrorParser
import pe.edu.upc.predictivemaintain.app.core.network.safeApiCall
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.PendingOperation
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncItemResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncOutcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncRepository
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local.WorkOrderCache
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.SyncApiService
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.SyncOperationDto
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.SyncRequestDto
import java.time.Instant
import javax.inject.Inject

class SyncRepositoryImpl @Inject constructor(
    private val apiService: SyncApiService,
    private val errorParser: ErrorParser,
    private val cache: WorkOrderCache
) : SyncRepository {

    override suspend fun synchronize(
        deviceSentAt: Instant,
        operations: List<PendingOperation>
    ): Outcome<List<SyncItemResult>> {
        val request = SyncRequestDto(
            deviceSentAt = deviceSentAt.toString(),
            operations = operations.map {
                SyncOperationDto(
                    operationId = it.operationId,
                    workOrderId = it.workOrderId,
                    type = it.type.name,
                    performedAt = it.performedAt.toString(),
                    summary = it.summary
                )
            }
        )
        val orderByOperation = operations.associate { it.operationId to it.workOrderId }

        return when (val result = safeApiCall(errorParser) { apiService.synchronizeWorkOrders(request) }) {
            is Outcome.Failure -> result
            is Outcome.Success -> Outcome.Success(
                result.data.results.map { dto ->
                    val workOrderId = orderByOperation[dto.operationId].orEmpty()
                    cache.applyServerState(workOrderId, dto.workOrderStatus, dto.workOrderVersion)
                    SyncItemResult(
                        operationId = dto.operationId,
                        workOrderId = workOrderId,
                        outcome = SyncOutcome.fromString(dto.outcome),
                        message = dto.message,
                        workOrderStatus = dto.workOrderStatus,
                        workOrderVersion = dto.workOrderVersion
                    )
                }
            )
        }
    }
}
