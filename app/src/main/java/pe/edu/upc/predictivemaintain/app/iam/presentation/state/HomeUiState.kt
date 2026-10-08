package pe.edu.upc.predictivemaintain.app.iam.presentation.state

import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role

data class HomeUiState(
    val displayName: String = "",
    val email: String = "",
    val roles: List<Role> = emptyList(),
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val hasNoConnection: Boolean = false,
    val errorMessage: String? = null
)
