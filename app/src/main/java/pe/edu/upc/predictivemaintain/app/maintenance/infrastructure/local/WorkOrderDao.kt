package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface WorkOrderDao {

    @Upsert
    suspend fun upsert(order: WorkOrderEntity)

    @Upsert
    suspend fun upsertAll(orders: List<WorkOrderEntity>)

    @Query("SELECT * FROM work_orders WHERE id = :id")
    suspend fun getById(id: String): WorkOrderEntity?

    /** Applies what the server said after a synchronization; a null argument keeps the current value. */
    @Query(
        "UPDATE work_orders SET status = COALESCE(:status, status), version = COALESCE(:version, version) " +
            "WHERE id = :id"
    )
    suspend fun applyServerState(id: String, status: String?, version: Long?)
}
