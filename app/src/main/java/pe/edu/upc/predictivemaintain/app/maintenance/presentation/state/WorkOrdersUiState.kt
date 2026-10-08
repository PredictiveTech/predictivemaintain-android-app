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
    val isErrorState: Boolean = false
)
