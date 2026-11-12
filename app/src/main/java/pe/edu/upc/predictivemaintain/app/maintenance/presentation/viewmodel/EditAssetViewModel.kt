package pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.GetAssetUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.UpdateAssetUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetChanges
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetDetail
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.form.AssetFormValidator
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.form.toAssetFormError
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.form.withField
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.AssetFormField
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.CreateAssetUiState
import javax.inject.Inject

/** Edit mode of the asset form: loads the current values, then saves them with PUT /assets/{id}. */
@HiltViewModel
class EditAssetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAssetUseCase: GetAssetUseCase,
    private val updateAssetUseCase: UpdateAssetUseCase
) : ViewModel() {

    private val assetId: String = checkNotNull(savedStateHandle[ARG_ASSET_ID]) { "assetId argument is required" }

    private val _uiState = MutableStateFlow(CreateAssetUiState(isLoading = true))
    val uiState: StateFlow<CreateAssetUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            when (val result = getAssetUseCase(assetId)) {
                is Outcome.Success -> {
                    val asset = result.data
                    _uiState.value = asset.toFormState()
                }
                is Outcome.Failure -> _uiState.update { it.copy(isLoading = false, loadError = result.error) }
            }
        }
    }

    fun onFieldChange(field: AssetFormField, value: String) {
        if (field == AssetFormField.CODE) return // the code never changes
        _uiState.update { it.withField(field, value) }
    }

    fun onCriticalityChange(criticality: Criticality) {
        _uiState.update { it.copy(criticality = criticality, submitError = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting || state.isLoading || state.loadError != null) return

        val errors = AssetFormValidator.validate(state, validateCode = false)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors, submitError = null) }
            return
        }

        val changes = AssetChanges(
            name = state.name.trim(),
            location = state.location.trim(),
            assetType = state.assetType.trim(),
            criticality = state.criticality,
            plantId = state.plantId,
            productionLine = state.productionLine.trim().ifEmpty { null },
            latitude = AssetFormValidator.parseCoordinate(state.latitude),
            longitude = AssetFormValidator.parseCoordinate(state.longitude)
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, fieldErrors = emptyMap(), submitError = null) }
            when (val result = updateAssetUseCase(assetId, changes)) {
                is Outcome.Success -> _uiState.update { it.copy(isSubmitting = false, saved = true) }
                is Outcome.Failure -> _uiState.update {
                    it.copy(isSubmitting = false, submitError = result.error.toAssetFormError())
                }
            }
        }
    }

    private fun AssetDetail.toFormState() = CreateAssetUiState(
        code = code.value,
        name = name,
        location = location.orEmpty(),
        assetType = assetType,
        // An UNKNOWN criticality (value not recognised) falls back to the form's default instead of blocking the edit.
        criticality = criticality.takeIf { it != Criticality.UNKNOWN } ?: Criticality.MEDIUM,
        productionLine = productionLine.orEmpty(),
        latitude = AssetFormValidator.format(latitude),
        longitude = AssetFormValidator.format(longitude),
        plantId = plantId,
        isLoading = false
    )

    companion object {
        const val ARG_ASSET_ID = "assetId"
    }
}
