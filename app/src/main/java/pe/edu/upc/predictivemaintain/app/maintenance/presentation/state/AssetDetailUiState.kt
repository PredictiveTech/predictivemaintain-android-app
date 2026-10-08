package pe.edu.upc.predictivemaintain.app.maintenance.presentation.state

import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetDetail

data class AssetDetailUiState(
    val isLoading: Boolean = false,
    val asset: AssetDetail? = null,
    val errorMessage: String? = null
)
