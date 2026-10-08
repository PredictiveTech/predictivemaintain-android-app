package pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.ObserveSessionUseCase
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.SessionRepository
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.maintenance.application.policy.AlertActionPolicy
import pe.edu.upc.predictivemaintain.app.maintenance.application.policy.WorkOrderActionPolicy
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.CreateWorkOrderUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.GetAlertUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.ReviewAlertUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Actor
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertStatus
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.AlertDetailUiState
import javax.inject.Inject

sealed interface AlertDetailUiEvent {
    data class NavigateToWorkOrder(val workOrderId: String) : AlertDetailUiEvent
}

@HiltViewModel
class AlertDetailViewModel @Inject constructor(
    private val getAlertUseCase: GetAlertUseCase,
    private val reviewAlertUseCase: ReviewAlertUseCase,
    private val createWorkOrderUseCase: CreateWorkOrderUseCase,
    private val sessionRepository: SessionRepository,
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val alertActionPolicy: AlertActionPolicy,
    private val workOrderActionPolicy: WorkOrderActionPolicy
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertDetailUiState())
    val uiState: StateFlow<AlertDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<AlertDetailUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun loadAlert(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            when (val result = getAlertUseCase(id)) {
                is Outcome.Success -> {
                    val alert = result.data
                    val session = sessionRepository.currentSession()
                    val roles = session?.roles ?: emptyList()
                    val canReview = alertActionPolicy.canReview(roles, alert)
                    val actor = Actor(
                        userId = session?.userId?.value ?: "",
                        isManager = roles.contains(Role.MAINTENANCE_MANAGER),
                        isTechnician = roles.contains(Role.TECHNICIAN)
                    )
                    val canCreateWorkOrder = workOrderActionPolicy.canCreate(actor, alert.status == AlertStatus.CONFIRMED)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            alert = alert,
                            canReview = canReview,
                            canCreateWorkOrder = canCreateWorkOrder,
                            errorMessage = null
                        )
                    }
                }
                is Outcome.Failure -> {
                    val message = formatError(result.error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = message
                        )
                    }
                }
            }
        }
    }

    fun confirmAlert() {
        val alert = _uiState.value.alert ?: return
        review(alert.id.value, "CONFIRMED", null, alert.version)
    }

    fun discardAlert(reason: String) {
        val alert = _uiState.value.alert ?: return
        review(alert.id.value, "DISCARDED", reason, alert.version)
    }

    fun createWorkOrder() {
        val alert = _uiState.value.alert ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            when (val result = createWorkOrderUseCase(AlertId(alert.id.value))) {
                is Outcome.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _uiEvent.send(AlertDetailUiEvent.NavigateToWorkOrder(result.data.id.value))
                }
                is Outcome.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = formatError(result.error)
                        )
                    }
                }
            }
        }
    }

    private fun review(id: String, status: String, reason: String?, expectedVersion: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            when (val result = reviewAlertUseCase(id, status, reason, expectedVersion)) {
                is Outcome.Success -> {
                    val updatedAlert = result.data
                    val session = sessionRepository.currentSession()
                    val roles = session?.roles ?: emptyList()
                    val canReview = alertActionPolicy.canReview(roles, updatedAlert)
                    val actor = Actor(
                        userId = session?.userId?.value ?: "",
                        isManager = roles.contains(Role.MAINTENANCE_MANAGER),
                        isTechnician = roles.contains(Role.TECHNICIAN)
                    )
                    val canCreateWorkOrder = workOrderActionPolicy.canCreate(actor, updatedAlert.status == AlertStatus.CONFIRMED)
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            alert = updatedAlert,
                            canReview = canReview,
                            canCreateWorkOrder = canCreateWorkOrder,
                            successMessage = "Alert successfully reviewed."
                        )
                    }
                }
                is Outcome.Failure -> {
                    val err = result.error
                    if (err is AppError.Api && err.httpStatus == 409) {
                        val detail = err.detail
                        when (val reloadResult = getAlertUseCase(id)) {
                            is Outcome.Success -> {
                                val reloaded = reloadResult.data
                                val session = sessionRepository.currentSession()
                                val roles = session?.roles ?: emptyList()
                                val canReview = alertActionPolicy.canReview(roles, reloaded)
                                val actor = Actor(
                                    userId = session?.userId?.value ?: "",
                                    isManager = roles.contains(Role.MAINTENANCE_MANAGER),
                                    isTechnician = roles.contains(Role.TECHNICIAN)
                                )
                                val canCreateWorkOrder = workOrderActionPolicy.canCreate(actor, reloaded.status == AlertStatus.CONFIRMED)
                                _uiState.update {
                                    it.copy(
                                        isSubmitting = false,
                                        alert = reloaded,
                                        canReview = canReview,
                                        canCreateWorkOrder = canCreateWorkOrder,
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
