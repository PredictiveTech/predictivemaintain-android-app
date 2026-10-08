package pe.edu.upc.predictivemaintain.app.maintenance.presentation.state

import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Alert

data class AlertDetailUiState(
    val isLoading: Boolean = false,
    val alert: Alert? = null,
    val canReview: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
