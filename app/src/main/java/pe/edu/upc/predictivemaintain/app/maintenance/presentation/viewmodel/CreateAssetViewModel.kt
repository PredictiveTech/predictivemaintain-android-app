package pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.CreateAssetUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.NewAsset
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.form.AssetFormValidator
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.form.toAssetFormError
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.form.withField
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
        _uiState.update { it.withField(field, value) }
    }

    fun onCriticalityChange(criticality: Criticality) {
        _uiState.update { it.copy(criticality = criticality, submitError = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val errors = AssetFormValidator.validate(state)
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
            latitude = AssetFormValidator.parseCoordinate(state.latitude),
            longitude = AssetFormValidator.parseCoordinate(state.longitude)
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, fieldErrors = emptyMap(), submitError = null) }
            when (val result = createAssetUseCase(newAsset)) {
                is Outcome.Success -> _uiState.update { it.copy(isSubmitting = false, saved = true) }
                is Outcome.Failure -> {
                    val error = result.error.toAssetFormError()
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
}
