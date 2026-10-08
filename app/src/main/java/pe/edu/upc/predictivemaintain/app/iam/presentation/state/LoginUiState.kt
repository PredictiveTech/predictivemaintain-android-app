package pe.edu.upc.predictivemaintain.app.iam.presentation.state

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)
