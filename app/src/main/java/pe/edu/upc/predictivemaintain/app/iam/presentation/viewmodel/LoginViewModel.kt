package pe.edu.upc.predictivemaintain.app.iam.presentation.viewmodel

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
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.LoginUseCase
import pe.edu.upc.predictivemaintain.app.iam.presentation.state.LoginUiEvent
import pe.edu.upc.predictivemaintain.app.iam.presentation.state.LoginUiState
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<LoginUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, generalError = null, fieldErrors = it.fieldErrors - "email") }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, generalError = null, fieldErrors = it.fieldErrors - "password") }
    }

    fun login() {
        val currentState = _uiState.value
        if (currentState.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null, fieldErrors = emptyMap()) }

            val result = loginUseCase(currentState.email, currentState.password)

            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is Outcome.Success -> {
                    _uiEvent.send(LoginUiEvent.NavigateToHome)
                }
                is Outcome.Failure -> {
                    handleError(result.error)
                }
            }
        }
    }

    private fun handleError(error: AppError) {
        when (error) {
            is AppError.Api -> {
                _uiState.update {
                    it.copy(
                        generalError = error.detail,
                        fieldErrors = error.fieldErrors
                    )
                }
            }
            is AppError.Network -> {
                _uiState.update { it.copy(generalError = "NETWORK") }
            }
            is AppError.InvalidResponse -> {
                _uiState.update { it.copy(generalError = "INVALID_RESPONSE") }
            }
            is AppError.SessionExpired -> {
                _uiState.update { it.copy(generalError = "SESSION_EXPIRED") }
            }
            is AppError.Unknown -> {
                _uiState.update { it.copy(generalError = "UNKNOWN") }
            }
        }
    }
}
