package pe.edu.upc.predictivemaintain.app.iam.presentation.state

sealed interface LoginUiEvent {
    data object NavigateToHome : LoginUiEvent
}
