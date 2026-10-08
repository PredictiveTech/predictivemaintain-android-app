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
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.GetAssetUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.AssetDetailUiState
import javax.inject.Inject

@HiltViewModel
class AssetDetailViewModel @Inject constructor(
    private val getAssetUseCase: GetAssetUseCase
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
}
