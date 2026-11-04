package pe.edu.upc.predictivemaintain.app.maintenance.domain.sync

import kotlinx.coroutines.flow.Flow

interface PendingOperationQueue {
    fun observe(): Flow<List<PendingOperation>>
    suspend fun list(): List<PendingOperation>
    suspend fun enqueue(operation: PendingOperation)
    suspend fun remove(operationIds: Set<String>)
}
