package pe.edu.upc.predictivemaintain.app.iam.presentation.state

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isOffline: Boolean = false,
    val email: String = "",
    val roles: List<Role> = emptyList(),
    /** Value currently typed in the field. */
    val displayName: String = "",
    /** Last value known to be stored on the server. */
    val savedDisplayName: String = "",
    val saveSucceeded: Boolean = false,
    /** Failure loading the profile (nothing to show). */
    val loadError: AppError? = null,
    /** Failure saving the profile (the form stays visible). */
    val saveError: AppError? = null
) {
    val isDisplayNameBlank: Boolean get() = displayName.isBlank()
    val canSave: Boolean
        get() = !isLoading && !isSaving && !isOffline && !isDisplayNameBlank &&
            displayName.trim() != savedDisplayName
}
