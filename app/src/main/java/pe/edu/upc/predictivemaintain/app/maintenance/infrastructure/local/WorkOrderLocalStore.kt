package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local

import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SQLite (Room) copy of the work orders the app has seen. It lets the technician open an order and act on it
 * without connection, also after the app was killed, and holds the optimistic state of actions waiting to be
 * synchronized.
 */
@Singleton
class WorkOrderLocalStore @Inject constructor(
    private val dao: WorkOrderDao
) {
    suspend fun put(order: WorkOrder) = dao.upsert(order.toEntity(System.currentTimeMillis()))

    suspend fun putAll(orders: List<WorkOrder>) {
        if (orders.isEmpty()) return
        val now = System.currentTimeMillis()
        dao.upsertAll(orders.map { it.toEntity(now) })
    }

    suspend fun get(id: String): WorkOrder? = dao.getById(id)?.toDomainOrNull()

    /** Replaces the local state with what the server said after a synchronization. */
    suspend fun applyServerState(id: String, status: String?, version: Long?) {
        dao.applyServerState(
            id = id,
            status = status?.let { WorkOrderStatus.fromString(it).name },
            version = version
        )
    }
}
