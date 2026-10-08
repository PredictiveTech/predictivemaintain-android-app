package pe.edu.upc.predictivemaintain.app.maintenance.presentation.state

import pe.edu.upc.predictivemaintain.app.iam.domain.entity.Technician
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder

data class WorkOrderDetailUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val order: WorkOrder? = null,
    val assignedTechnicianName: String? = null,
    val availableTechnicians: List<Technician> = emptyList(),
    val canAssign: Boolean = false,
    val isManager: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
