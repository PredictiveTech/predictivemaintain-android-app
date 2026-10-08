package pe.edu.upc.predictivemaintain.app.telemetry.presentation.state

import pe.edu.upc.predictivemaintain.app.telemetry.domain.entity.SensorPanelItem

data class SensorPanelUiState(
    val isLoading: Boolean = false,
    val items: List<SensorPanelItem> = emptyList(),
    val errorMessage: String? = null
)
