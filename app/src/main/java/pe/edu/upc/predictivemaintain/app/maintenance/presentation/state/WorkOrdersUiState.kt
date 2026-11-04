package pe.edu.upc.predictivemaintain.app.maintenance.presentation.state

import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus

data class WorkOrdersUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val orders: List<WorkOrder> = emptyList(),
    val selectedStatus: WorkOrderStatus? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val isManager: Boolean = false,
    val isTechnician: Boolean = false,
    val technicianNames: Map<String, String> = emptyMap(),
    val errorMessage: String? = null,
    val isErrorState: Boolean = false,
    val isOffline: Boolean = false,
    val isSyncing: Boolean = false,
    val pendingCount: Int = 0,
    val syncResult: SyncResultUi? = null
)

/** What the user must be told after a synchronization. [issues] are server messages for refused actions. */
data class SyncResultUi(
    val appliedCount: Int,
    val issues: List<String> = emptyList(),
    val errorMessage: String? = null
) {
    val needsAttention: Boolean get() = issues.isNotEmpty() || errorMessage != null
}
