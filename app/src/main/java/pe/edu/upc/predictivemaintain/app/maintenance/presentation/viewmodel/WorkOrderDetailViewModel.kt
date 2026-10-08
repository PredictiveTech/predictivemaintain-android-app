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
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.CompleteWorkOrderUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.GetWorkOrderUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.StartWorkOrderUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Actor
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.TechnicianId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.WorkOrderDetailUiState
import javax.inject.Inject

@HiltViewModel
class WorkOrderDetailViewModel @Inject constructor(
    private val getWorkOrderUseCase: GetWorkOrderUseCase,
    private val assignWorkOrderUseCase: AssignWorkOrderUseCase,
    private val startWorkOrderUseCase: StartWorkOrderUseCase,
    private val completeWorkOrderUseCase: CompleteWorkOrderUseCase,
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
                    val canStart = workOrderActionPolicy.canStart(actor, order)
                    val canComplete = workOrderActionPolicy.canComplete(actor, order)
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
                            canStart = canStart,
                            canComplete = canComplete,
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
            handleMutationResult(result, order.id, "Work order assigned successfully.")
        }
    }

    fun startWorkOrder() {
        val order = _uiState.value.order ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            val result = startWorkOrderUseCase(
                id = order.id,
                expectedVersion = order.version
            )
            handleMutationResult(result, order.id, "Work order started successfully.")
        }
    }

    fun completeWorkOrder(summary: String) {
        val order = _uiState.value.order ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            val result = completeWorkOrderUseCase(
                id = order.id,
                summary = summary,
                expectedVersion = order.version
            )
            handleMutationResult(result, order.id, "Work order completed successfully.")
        }
    }

    private suspend fun handleMutationResult(
        result: Outcome<WorkOrder>,
        orderId: WorkOrderId,
        successMsg: String
    ) {
        val session = sessionRepository.currentSession()
        val userId = session?.userId?.value ?: ""
        val isManager = session?.roles?.contains(Role.MAINTENANCE_MANAGER) == true
        val isTechnician = session?.roles?.contains(Role.TECHNICIAN) == true
        val actor = Actor(userId = userId, isManager = isManager, isTechnician = isTechnician)

        when (result) {
            is Outcome.Success -> {
                val updatedOrder = result.data
                val canAssign = workOrderActionPolicy.canAssign(actor, updatedOrder)
                val canStart = workOrderActionPolicy.canStart(actor, updatedOrder)
                val canComplete = workOrderActionPolicy.canComplete(actor, updatedOrder)
                val assignedName = updatedOrder.assignedUserId?.let { techId ->
                    _uiState.value.availableTechnicians.find { it.id.value == techId.value }?.displayName?.value
                }
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        order = updatedOrder,
                        assignedTechnicianName = assignedName,
                        canAssign = canAssign,
                        canStart = canStart,
                        canComplete = canComplete,
                        errorMessage = null,
                        successMessage = successMsg
                    )
                }
            }
            is Outcome.Failure -> {
                val err = result.error
                if (err is AppError.Api && (err.httpStatus == 409 || err.httpStatus == 403)) {
                    val detail = err.detail
                    if (err.httpStatus == 409) {
                        when (val reloadResult = getWorkOrderUseCase(orderId)) {
                            is Outcome.Success -> {
                                val reloaded = reloadResult.data
                                val canAssign = workOrderActionPolicy.canAssign(actor, reloaded)
                                val canStart = workOrderActionPolicy.canStart(actor, reloaded)
                                val canComplete = workOrderActionPolicy.canComplete(actor, reloaded)
                                val assignedName = reloaded.assignedUserId?.let { techId ->
                                    _uiState.value.availableTechnicians.find { it.id.value == techId.value }?.displayName?.value
                                }
                                _uiState.update {
                                    it.copy(
                                        isSubmitting = false,
                                        order = reloaded,
                                        assignedTechnicianName = assignedName,
                                        canAssign = canAssign,
                                        canStart = canStart,
                                        canComplete = canComplete,
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
                                errorMessage = detail
                            )
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
