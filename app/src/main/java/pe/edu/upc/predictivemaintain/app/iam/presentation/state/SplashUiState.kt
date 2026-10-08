package pe.edu.upc.predictivemaintain.app.iam.presentation.state

import pe.edu.upc.predictivemaintain.app.core.error.AppError

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data object NavigateToHome : SplashUiState
    data object NavigateToLogin : SplashUiState
    data class Error(val error: AppError) : SplashUiState
}
