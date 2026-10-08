package pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.ListTechniciansUseCase
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.SessionRepository
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.maintenance.application.policy.WorkOrderActionPolicy
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.AssignWorkOrderUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.GetWorkOrderUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Actor
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.TechnicianId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.WorkOrderDetailUiState
import javax.inject.Inject

@HiltViewModel
class WorkOrderDetailViewModel @Inject constructor(
    private val getWorkOrderUseCase: GetWorkOrderUseCase,
    private val assignWorkOrderUseCase: AssignWorkOrderUseCase,
    private val listTechniciansUseCase: ListTechniciansUseCase,
    private val workOrderActionPolicy: WorkOrderActionPolicy,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkOrderDetailUiState())
    val uiState: StateFlow<WorkOrderDetailUiState> = _uiState.asStateFlow()

    fun loadOrder(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val session = sessionRepository.currentSession()
            val userId = session?.userId?.value ?: ""
            val isManager = session?.roles?.contains(Role.MAINTENANCE_MANAGER) == true
            val isTechnician = session?.roles?.contains(Role.TECHNICIAN) == true
            val actor = Actor(userId = userId, isManager = isManager, isTechnician = isTechnician)

            var techList = _uiState.value.availableTechnicians
            if (isManager && techList.isEmpty()) {
                when (val techResult = listTechniciansUseCase()) {
                    is Outcome.Success -> techList = techResult.data
                    is Outcome.Failure -> {}
                }
            }

            when (val result = getWorkOrderUseCase(WorkOrderId(id))) {
                is Outcome.Success -> {
                    val order = result.data
                    val canAssign = workOrderActionPolicy.canAssign(actor, order)
                    val assignedName = order.assignedUserId?.let { techId ->
                        techList.find { it.id.value == techId.value }?.displayName?.value
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            order = order,
                            assignedTechnicianName = assignedName,
                            availableTechnicians = techList,
                            canAssign = canAssign,
                            isManager = isManager,
                            errorMessage = null
                        )
                    }
                }
                is Outcome.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = formatError(result.error)
                        )
                    }
                }
            }
        }
    }

    fun assignTechnician(technicianId: String) {
        val order = _uiState.value.order ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            val result = assignWorkOrderUseCase(
                id = order.id,
                technicianId = TechnicianId(technicianId),
                expectedVersion = order.version
            )
            when (result) {
                is Outcome.Success -> {
                    val updatedOrder = result.data
                    val session = sessionRepository.currentSession()
                    val userId = session?.userId?.value ?: ""
                    val isManager = session?.roles?.contains(Role.MAINTENANCE_MANAGER) == true
                    val isTechnician = session?.roles?.contains(Role.TECHNICIAN) == true
                    val actor = Actor(userId = userId, isManager = isManager, isTechnician = isTechnician)
                    val canAssign = workOrderActionPolicy.canAssign(actor, updatedOrder)
                    val assignedName = updatedOrder.assignedUserId?.let { techId ->
                        _uiState.value.availableTechnicians.find { it.id.value == techId.value }?.displayName?.value
                    }
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            order = updatedOrder,
                            assignedTechnicianName = assignedName,
                            canAssign = canAssign,
                            errorMessage = null,
                            successMessage = "Work order assigned successfully."
                        )
                    }
                }
                is Outcome.Failure -> {
                    val err = result.error
                    if (err is AppError.Api && err.httpStatus == 409) {
                        val detail = err.detail
                        val session = sessionRepository.currentSession()
                        val userId = session?.userId?.value ?: ""
                        val isManager = session?.roles?.contains(Role.MAINTENANCE_MANAGER) == true
                        val isTechnician = session?.roles?.contains(Role.TECHNICIAN) == true
                        val actor = Actor(userId = userId, isManager = isManager, isTechnician = isTechnician)
                        when (val reloadResult = getWorkOrderUseCase(order.id)) {
                            is Outcome.Success -> {
                                val reloaded = reloadResult.data
                                val canAssign = workOrderActionPolicy.canAssign(actor, reloaded)
                                val assignedName = reloaded.assignedUserId?.let { techId ->
                                    _uiState.value.availableTechnicians.find { it.id.value == techId.value }?.displayName?.value
                                }
                                _uiState.update {
                                    it.copy(
                                        isSubmitting = false,
                                        order = reloaded,
                                        assignedTechnicianName = assignedName,
                                        canAssign = canAssign,
                                        errorMessage = detail
                                    )
                                }
                            }
                            is Outcome.Failure -> {
                                _uiState.update {
                                    it.copy(
                                        isSubmitting = false,
                                        errorMessage = detail
                                    )
                                }
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                errorMessage = formatError(err)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun formatError(error: AppError): String {
        return when (error) {
            is AppError.Api -> error.detail
            is AppError.Network -> "Network error. Please check your connection."
            is AppError.InvalidResponse -> "Unexpected server response."
            is AppError.SessionExpired -> "Session expired."
            is AppError.Unknown -> "An unexpected error occurred."
        }
    }
}
