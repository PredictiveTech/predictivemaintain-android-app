package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {

    // rowid grows with every insert, so ordering by it gives the queue its first-in-first-out order.
    @Query("SELECT * FROM pending_operations ORDER BY rowid")
    fun observeAll(): Flow<List<PendingOperationEntity>>

    @Query("SELECT * FROM pending_operations ORDER BY rowid")
    suspend fun getAll(): List<PendingOperationEntity>

    /** Deduplicates by operationId: an operation that is already queued is left untouched. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(operation: PendingOperationEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(operations: List<PendingOperationEntity>)

    @Query("DELETE FROM pending_operations WHERE operation_id IN (:operationIds)")
    suspend fun deleteByIds(operationIds: List<String>)
}
