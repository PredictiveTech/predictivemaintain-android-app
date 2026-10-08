package pe.edu.upc.predictivemaintain.app.iam.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.LogoutUseCase
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.RestoreSessionUseCase
import pe.edu.upc.predictivemaintain.app.iam.presentation.state.SplashUiState
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val restoreSessionUseCase: RestoreSessionUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            _uiState.value = SplashUiState.Loading
            when (val result = restoreSessionUseCase()) {
                is Outcome.Success -> {
                    _uiState.value = SplashUiState.NavigateToHome
                }
                is Outcome.Failure -> {
                    when (result.error) {
                        is AppError.SessionExpired -> {
                            _uiState.value = SplashUiState.NavigateToLogin
                        }
                        is AppError.Api,
                        is AppError.Network,
                        is AppError.InvalidResponse,
                        is AppError.Unknown -> {
                            _uiState.value = SplashUiState.Error(result.error)
                        }
                    }
                }
            }
        }
    }

    fun logoutAndNavigateToLogin() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.value = SplashUiState.NavigateToLogin
        }
    }
}
