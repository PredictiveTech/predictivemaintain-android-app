package pe.edu.upc.predictivemaintain.app.maintenance.presentation.state

import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetDetail

enum class DeactivateDialog { NONE, CONFIRM, CONFIRM_WITH_OPEN_ORDERS }

data class AssetDetailUiState(
    val isLoading: Boolean = false,
    val asset: AssetDetail? = null,
    val errorMessage: String? = null,
    val deactivateDialog: DeactivateDialog = DeactivateDialog.NONE,
    val isDeactivating: Boolean = false,
    val deactivateError: CreateAssetError? = null,
    /** Set once the server confirmed the deactivation, so the screen can notify the list. */
    val deactivated: Boolean = false
)
