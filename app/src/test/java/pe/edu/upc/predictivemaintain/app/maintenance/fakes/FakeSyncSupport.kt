package pe.edu.upc.predictivemaintain.app.maintenance.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pe.edu.upc.predictivemaintain.app.core.connectivity.ConnectivityObserver
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.PendingOperation
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.PendingOperationQueue
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncItemResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncOutcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncRepository
import java.time.Instant

class FakeConnectivityObserver(initial: Boolean = true) : ConnectivityObserver {
    val online = MutableStateFlow(initial)
    override fun observe(): Flow<Boolean> = online
}

class FakePendingOperationQueue : PendingOperationQueue {
    val operations = MutableStateFlow<List<PendingOperation>>(emptyList())
    override fun observe(): Flow<List<PendingOperation>> = operations
    override suspend fun list(): List<PendingOperation> = operations.value
    override suspend fun enqueue(operation: PendingOperation) {
        operations.value = operations.value + operation
    }
    override suspend fun remove(operationIds: Set<String>) {
        operations.value = operations.value.filterNot { it.operationId in operationIds }
    }
}

class FakeSyncRepository : SyncRepository {
    var result: Outcome<List<SyncItemResult>>? = null
    var calls = 0
    var lastSent: List<PendingOperation> = emptyList()

    override suspend fun synchronize(
        deviceSentAt: Instant,
        operations: List<PendingOperation>
    ): Outcome<List<SyncItemResult>> {
        calls++
        lastSent = operations
        return result ?: Outcome.Success(
            operations.map {
                SyncItemResult(it.operationId, it.workOrderId, SyncOutcome.APPLIED, null, null, null)
            }
        )
    }
}
