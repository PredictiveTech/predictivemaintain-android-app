package pe.edu.upc.predictivemaintain.app.maintenance.presentation.state

import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Alert

data class AlertsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val items: List<Alert> = emptyList(),
    val page: Int = 0,
    val totalPages: Int = 0,
    val hasMore: Boolean = false,
    val selectedStatusFilter: String? = "IN_REVIEW",
    val selectedSeverityFilter: String? = null,
    val assetIdFilter: String? = null,
    val errorMessage: String? = null
)
