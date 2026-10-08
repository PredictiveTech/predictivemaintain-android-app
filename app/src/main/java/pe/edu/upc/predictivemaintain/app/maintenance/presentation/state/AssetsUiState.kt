package pe.edu.upc.predictivemaintain.app.maintenance.presentation.state

import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Asset

data class AssetsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val items: List<Asset> = emptyList(),
    val page: Int = 0,
    val totalPages: Int = 0,
    val hasMore: Boolean = false,
    val selectedStatusFilter: String? = null,
    val errorMessage: String? = null
)
