package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local

import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory copy of the work orders the app has seen. It lets the technician open an order and act on it
 * without connection, and holds the optimistic state of actions waiting to be synchronized.
 */
@Singleton
class WorkOrderCache @Inject constructor() {

    private val orders = ConcurrentHashMap<String, WorkOrder>()

    fun put(order: WorkOrder) {
        orders[order.id.value] = order
    }

    fun putAll(list: List<WorkOrder>) = list.forEach(::put)

    fun get(id: String): WorkOrder? = orders[id]

    /** Replaces the local state with what the server said after a synchronization. */
    fun applyServerState(id: String, status: String?, version: Long?) {
        val current = orders[id] ?: return
        orders[id] = current.copy(
            status = if (status != null) WorkOrderStatus.fromString(status) else current.status,
            version = version ?: current.version
        )
    }
}
