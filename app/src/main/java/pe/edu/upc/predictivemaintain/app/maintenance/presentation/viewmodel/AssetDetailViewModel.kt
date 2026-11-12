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
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.DeactivateAssetUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.GetAssetUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.form.toAssetFormError
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.CreateAssetError
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.DeactivateDialog
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.AssetDetailUiState
import javax.inject.Inject

@HiltViewModel
class AssetDetailViewModel @Inject constructor(
    private val getAssetUseCase: GetAssetUseCase,
    private val deactivateAssetUseCase: DeactivateAssetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssetDetailUiState())
    val uiState: StateFlow<AssetDetailUiState> = _uiState.asStateFlow()

    fun loadAsset(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getAssetUseCase(id)) {
                is Outcome.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            asset = result.data,
                            errorMessage = null
                        )
                    }
                }
                is Outcome.Failure -> {
                    val message = when (val err = result.error) {
                        is AppError.Api -> err.detail
                        is AppError.Network -> "Network error. Please check your connection."
                        is AppError.InvalidResponse -> "Unexpected server response."
                        is AppError.SessionExpired -> "Session expired."
                        is AppError.Unknown -> "An unexpected error occurred."
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = message
                        )
                    }
                }
            }
        }
    }

    fun requestDeactivate() {
        val state = _uiState.value
        if (state.asset?.active != true || state.isDeactivating) return
        _uiState.update { it.copy(deactivateDialog = DeactivateDialog.CONFIRM, deactivateError = null) }
    }

    fun dismissDeactivateDialog() {
        _uiState.update { it.copy(deactivateDialog = DeactivateDialog.NONE) }
    }

    fun confirmDeactivate() {
        val state = _uiState.value
        val asset = state.asset ?: return
        if (state.isDeactivating || state.deactivateDialog == DeactivateDialog.NONE) return
        val force = state.deactivateDialog == DeactivateDialog.CONFIRM_WITH_OPEN_ORDERS

        viewModelScope.launch {
            _uiState.update { it.copy(isDeactivating = true, deactivateDialog = DeactivateDialog.NONE, deactivateError = null) }
            when (val result = deactivateAssetUseCase(asset.id.value, force)) {
                is Outcome.Success -> _uiState.update {
                    it.copy(isDeactivating = false, asset = result.data, deactivated = true)
                }
                is Outcome.Failure -> {
                    val error = result.error.toAssetFormError()
                    _uiState.update {
                        if (error == CreateAssetError.HasOpenWorkOrders && !force) {
                            // The server only warned: ask again, explaining the open work orders.
                            it.copy(isDeactivating = false, deactivateDialog = DeactivateDialog.CONFIRM_WITH_OPEN_ORDERS)
                        } else {
                            it.copy(isDeactivating = false, deactivateError = error)
                        }
                    }
                }
            }
        }
    }
}
