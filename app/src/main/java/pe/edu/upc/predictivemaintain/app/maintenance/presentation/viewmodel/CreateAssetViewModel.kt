package pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.CreateAssetUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.NewAsset
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.AssetFormField
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.CreateAssetError
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.CreateAssetUiState
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.FieldError
import javax.inject.Inject

@HiltViewModel
class CreateAssetViewModel @Inject constructor(
    private val createAssetUseCase: CreateAssetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAssetUiState())
    val uiState: StateFlow<CreateAssetUiState> = _uiState.asStateFlow()

    fun onFieldChange(field: AssetFormField, value: String) {
        _uiState.update { state ->
            val updated = when (field) {
                AssetFormField.CODE -> state.copy(code = value)
                AssetFormField.NAME -> state.copy(name = value)
                AssetFormField.LOCATION -> state.copy(location = value)
                AssetFormField.ASSET_TYPE -> state.copy(assetType = value)
                AssetFormField.PRODUCTION_LINE -> state.copy(productionLine = value)
                AssetFormField.LATITUDE -> state.copy(latitude = value)
                AssetFormField.LONGITUDE -> state.copy(longitude = value)
            }
            // Editing a field clears its error; coordinates are validated together.
            val cleared = if (field == AssetFormField.LATITUDE || field == AssetFormField.LONGITUDE) {
                updated.fieldErrors - AssetFormField.LATITUDE - AssetFormField.LONGITUDE
            } else {
                updated.fieldErrors - field
            }
            updated.copy(fieldErrors = cleared, submitError = null)
        }
    }

    fun onCriticalityChange(criticality: Criticality) {
        _uiState.update { it.copy(criticality = criticality, submitError = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val errors = validate(state)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors, submitError = null) }
            return
        }

        val newAsset = NewAsset(
            code = state.code.trim(),
            name = state.name.trim(),
            location = state.location.trim(),
            assetType = state.assetType.trim(),
            criticality = state.criticality,
            productionLine = state.productionLine.trim().ifEmpty { null },
            latitude = parseCoordinate(state.latitude),
            longitude = parseCoordinate(state.longitude)
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, fieldErrors = emptyMap(), submitError = null) }
            when (val result = createAssetUseCase(newAsset)) {
                is Outcome.Success -> _uiState.update { it.copy(isSubmitting = false, created = true) }
                is Outcome.Failure -> {
                    val error = result.error.toCreateAssetError()
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submitError = error,
                            fieldErrors = if (error == CreateAssetError.DuplicateCode) {
                                mapOf(AssetFormField.CODE to FieldError.DUPLICATE_CODE)
                            } else {
                                emptyMap()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun validate(state: CreateAssetUiState): Map<AssetFormField, FieldError> {
        val errors = linkedMapOf<AssetFormField, FieldError>()
        requiredText(state.code, NewAsset.CODE_MAX)?.let { errors[AssetFormField.CODE] = it }
        requiredText(state.name, NewAsset.NAME_MAX)?.let { errors[AssetFormField.NAME] = it }
        requiredText(state.location, NewAsset.LOCATION_MAX)?.let { errors[AssetFormField.LOCATION] = it }
        requiredText(state.assetType, NewAsset.ASSET_TYPE_MAX)?.let { errors[AssetFormField.ASSET_TYPE] = it }
        if (state.productionLine.trim().length > NewAsset.PRODUCTION_LINE_MAX) {
            errors[AssetFormField.PRODUCTION_LINE] = FieldError.TOO_LONG
        }

        val latText = state.latitude.trim()
        val lonText = state.longitude.trim()
        if (latText.isNotEmpty() || lonText.isNotEmpty()) {
            val lat = parseCoordinate(latText)
            val lon = parseCoordinate(lonText)
            when {
                latText.isNotEmpty() && lat == null -> errors[AssetFormField.LATITUDE] = FieldError.INVALID_NUMBER
                latText.isEmpty() -> errors[AssetFormField.LATITUDE] = FieldError.COORDINATES_INCOMPLETE
                lat!! !in NewAsset.LATITUDE_RANGE -> errors[AssetFormField.LATITUDE] = FieldError.OUT_OF_RANGE
            }
            when {
                lonText.isNotEmpty() && lon == null -> errors[AssetFormField.LONGITUDE] = FieldError.INVALID_NUMBER
                lonText.isEmpty() -> errors[AssetFormField.LONGITUDE] = FieldError.COORDINATES_INCOMPLETE
                lon!! !in NewAsset.LONGITUDE_RANGE -> errors[AssetFormField.LONGITUDE] = FieldError.OUT_OF_RANGE
            }
        }
        return errors
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
    private fun parseCoordinate(text: String): Double? {
        val value = text.trim().replace(',', '.').toDoubleOrNull() ?: return null
        return value.takeIf { it.isFinite() }
    }

    private fun AppError.toCreateAssetError(): CreateAssetError = when {
        this is AppError.Api && httpStatus == 409 && code == CODE_ASSET_EXISTS -> CreateAssetError.DuplicateCode
        this is AppError.Api && httpStatus == 403 && code == CODE_CAPACITY_EXCEEDED -> CreateAssetError.CapacityExceeded
        this is AppError.Api && httpStatus == 403 -> CreateAssetError.Forbidden
        else -> CreateAssetError.Other(this)
    }

    private companion object {
        const val CODE_ASSET_EXISTS = "ASSET_CODE_ALREADY_EXISTS"
        const val CODE_CAPACITY_EXCEEDED = "CAPACITY_EXCEEDED"
    }
}
