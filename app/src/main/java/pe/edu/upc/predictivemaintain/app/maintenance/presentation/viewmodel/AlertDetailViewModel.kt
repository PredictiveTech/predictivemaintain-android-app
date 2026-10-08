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
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.SessionRepository
import pe.edu.upc.predictivemaintain.app.maintenance.application.policy.AlertActionPolicy
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.GetAlertUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.ReviewAlertUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.AlertDetailUiState
import javax.inject.Inject

@HiltViewModel
class AlertDetailViewModel @Inject constructor(
    private val getAlertUseCase: GetAlertUseCase,
    private val reviewAlertUseCase: ReviewAlertUseCase,
    private val sessionRepository: SessionRepository,
    private val alertActionPolicy: AlertActionPolicy
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertDetailUiState())
    val uiState: StateFlow<AlertDetailUiState> = _uiState.asStateFlow()

    fun loadAlert(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            when (val result = getAlertUseCase(id)) {
                is Outcome.Success -> {
                    val alert = result.data
                    val session = sessionRepository.currentSession()
                    val roles = session?.roles ?: emptyList()
                    val canReview = alertActionPolicy.canReview(roles, alert)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            alert = alert,
                            canReview = canReview,
                            errorMessage = null
                        )
                    }
                }
                is Outcome.Failure -> {
                    val message = when (val err = result.error) {
                        is AppError.Api -> err.detail
                        is AppError.Network -> "Network error. Please check your connection."
                        is AppError.InvalidResponse -> "Unexpected server response."
                        is AppError.SessionExpired -> "Session expired."
                        is AppError.Unknown -> "An unexpected error occurred."
                    }
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

    private fun review(id: String, status: String, reason: String?, expectedVersion: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            when (val result = reviewAlertUseCase(id, status, reason, expectedVersion)) {
                is Outcome.Success -> {
                    val updatedAlert = result.data
                    val session = sessionRepository.currentSession()
                    val roles = session?.roles ?: emptyList()
                    val canReview = alertActionPolicy.canReview(roles, updatedAlert)
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            alert = updatedAlert,
                            canReview = canReview,
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
                                _uiState.update {
                                    it.copy(
                                        isSubmitting = false,
                                        alert = reloaded,
                                        canReview = canReview,
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
                        val message = when (err) {
                            is AppError.Api -> err.detail
                            is AppError.Network -> "Network error. Please check your connection."
                            is AppError.InvalidResponse -> "Unexpected server response."
                            is AppError.SessionExpired -> "Session expired."
                            is AppError.Unknown -> "An unexpected error occurred."
                        }
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                errorMessage = message
                            )
                        }
                    }
                }
            }
        }
    }
}
