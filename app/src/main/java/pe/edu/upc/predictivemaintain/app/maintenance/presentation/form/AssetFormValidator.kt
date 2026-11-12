package pe.edu.upc.predictivemaintain.app.maintenance.presentation.form

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.NewAsset
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.AssetFormField
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.CreateAssetError
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.CreateAssetUiState
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.FieldError

/** Validation and error mapping shared by the create and edit forms. */
object AssetFormValidator {

    fun validate(state: CreateAssetUiState, validateCode: Boolean = true): Map<AssetFormField, FieldError> {
        val errors = linkedMapOf<AssetFormField, FieldError>()
        if (validateCode) {
            requiredText(state.code, NewAsset.CODE_MAX)?.let { errors[AssetFormField.CODE] = it }
        }
        requiredText(state.name, NewAsset.NAME_MAX)?.let { errors[AssetFormField.NAME] = it }
        requiredText(state.location, NewAsset.LOCATION_MAX)?.let { errors[AssetFormField.LOCATION] = it }
        requiredText(state.assetType, NewAsset.ASSET_TYPE_MAX)?.let { errors[AssetFormField.ASSET_TYPE] = it }
        if (state.productionLine.trim().length > NewAsset.PRODUCTION_LINE_MAX) {
            errors[AssetFormField.PRODUCTION_LINE] = FieldError.TOO_LONG
        }

        val latText = state.latitude.trim()
        val lonText = state.longitude.trim()
        if (latText.isNotEmpty() || lonText.isNotEmpty()) {
            coordinateError(latText, NewAsset.LATITUDE_RANGE)?.let { errors[AssetFormField.LATITUDE] = it }
            coordinateError(lonText, NewAsset.LONGITUDE_RANGE)?.let { errors[AssetFormField.LONGITUDE] = it }
        }
        return errors
    }

    private fun coordinateError(text: String, range: ClosedFloatingPointRange<Double>): FieldError? {
        if (text.isEmpty()) return FieldError.COORDINATES_INCOMPLETE
        val value = parseCoordinate(text) ?: return FieldError.INVALID_NUMBER
        return if (value in range) null else FieldError.OUT_OF_RANGE
    }

    private fun requiredText(value: String, max: Int): FieldError? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> FieldError.REQUIRED
            trimmed.length > max -> FieldError.TOO_LONG
            else -> null
        }
    }

    /** Accepts a decimal comma; rejects NaN and infinities. */
    fun parseCoordinate(text: String): Double? {
        val value = text.trim().replace(',', '.').toDoubleOrNull() ?: return null
        return value.takeIf { it.isFinite() }
    }

    fun format(value: Double?): String = value?.toString().orEmpty()
}

/** Returns the state with [field] replaced; clears that field's error (coordinates are validated together). */
fun CreateAssetUiState.withField(field: AssetFormField, value: String): CreateAssetUiState {
    val updated = when (field) {
        AssetFormField.CODE -> copy(code = value)
        AssetFormField.NAME -> copy(name = value)
        AssetFormField.LOCATION -> copy(location = value)
        AssetFormField.ASSET_TYPE -> copy(assetType = value)
        AssetFormField.PRODUCTION_LINE -> copy(productionLine = value)
        AssetFormField.LATITUDE -> copy(latitude = value)
        AssetFormField.LONGITUDE -> copy(longitude = value)
    }
    val cleared = if (field == AssetFormField.LATITUDE || field == AssetFormField.LONGITUDE) {
        updated.fieldErrors - AssetFormField.LATITUDE - AssetFormField.LONGITUDE
    } else {
        updated.fieldErrors - field
    }
    return updated.copy(fieldErrors = cleared, submitError = null)
}

fun AppError.toAssetFormError(): CreateAssetError = when {
    this is AppError.Api && httpStatus == 409 && code == "ASSET_CODE_ALREADY_EXISTS" -> CreateAssetError.DuplicateCode
    this is AppError.Api && httpStatus == 409 && code == "ASSET_INACTIVE" -> CreateAssetError.AssetInactive
    this is AppError.Api && httpStatus == 409 && code == "ASSET_HAS_OPEN_WORK_ORDERS" -> CreateAssetError.HasOpenWorkOrders
    this is AppError.Api && httpStatus == 404 && code == "ASSET_NOT_FOUND" -> CreateAssetError.NotFound
    this is AppError.Api && httpStatus == 403 && code == "CAPACITY_EXCEEDED" -> CreateAssetError.CapacityExceeded
    this is AppError.Api && httpStatus == 403 -> CreateAssetError.Forbidden
    else -> CreateAssetError.Other(this)
}
