package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.PendingOperation
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.PendingOperationQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomPendingOperationQueue @Inject constructor(
    private val database: MaintenanceDatabase,
    private val dao: PendingOperationDao,
    private val legacyMigrator: LegacyPendingOperationMigrator
) : PendingOperationQueue {

    override fun observe(): Flow<List<PendingOperation>> = flow {
        legacyMigrator.migrateIfNeeded()
        emitAll(dao.observeAll().map { rows -> rows.mapNotNull { it.toDomainOrNull() } })
    }

    override suspend fun list(): List<PendingOperation> {
        legacyMigrator.migrateIfNeeded()
        return dao.getAll().mapNotNull { it.toDomainOrNull() }
    }

    override suspend fun enqueue(operation: PendingOperation) {
        legacyMigrator.migrateIfNeeded()
        dao.insert(operation.toEntity())
    }

    override suspend fun remove(operationIds: Set<String>) {
        if (operationIds.isEmpty()) return
        legacyMigrator.migrateIfNeeded()
        // SQLite limits the number of bound variables per statement, so large sets are deleted in chunks.
        database.withTransaction {
            operationIds.chunked(DELETE_CHUNK).forEach { dao.deleteByIds(it) }
        }
    }

    private companion object {
        const val DELETE_CHUNK = 500
    }
}
