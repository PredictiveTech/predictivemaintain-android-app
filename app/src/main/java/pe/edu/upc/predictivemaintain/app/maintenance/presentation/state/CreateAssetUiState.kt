package pe.edu.upc.predictivemaintain.app.maintenance.presentation.state

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality

enum class AssetFormField { CODE, NAME, LOCATION, ASSET_TYPE, PRODUCTION_LINE, LATITUDE, LONGITUDE }

enum class FieldError { REQUIRED, TOO_LONG, INVALID_NUMBER, OUT_OF_RANGE, COORDINATES_INCOMPLETE, DUPLICATE_CODE }

/** Why the server refused the creation. */
sealed interface CreateAssetError {
    /** 409 ASSET_CODE_ALREADY_EXISTS (also marks the code field). */
    data object DuplicateCode : CreateAssetError

    /** 403 CAPACITY_EXCEEDED: the subscription plan's asset limit is used up. */
    data object CapacityExceeded : CreateAssetError

    /** Any other 403: the user lacks the MAINTENANCE_MANAGER role. */
    data object Forbidden : CreateAssetError

    data class Other(val error: AppError) : CreateAssetError
}

data class CreateAssetUiState(
    val code: String = "",
    val name: String = "",
    val location: String = "",
    val assetType: String = "",
    val criticality: Criticality = Criticality.MEDIUM,
    val productionLine: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val fieldErrors: Map<AssetFormField, FieldError> = emptyMap(),
    val isSubmitting: Boolean = false,
    val submitError: CreateAssetError? = null,
    val created: Boolean = false
)
