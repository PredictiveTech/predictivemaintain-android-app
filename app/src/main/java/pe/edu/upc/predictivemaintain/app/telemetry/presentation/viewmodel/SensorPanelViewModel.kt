package pe.edu.upc.predictivemaintain.app.telemetry.presentation.viewmodel

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
import pe.edu.upc.predictivemaintain.app.telemetry.application.usecase.GetSensorPanelUseCase
import pe.edu.upc.predictivemaintain.app.telemetry.presentation.state.SensorPanelUiState
import javax.inject.Inject

@HiltViewModel
class SensorPanelViewModel @Inject constructor(
    private val getSensorPanelUseCase: GetSensorPanelUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SensorPanelUiState())
    val uiState: StateFlow<SensorPanelUiState> = _uiState.asStateFlow()

    fun loadSensors(assetId: String) {
        viewModelScope.launch {
            val isInitial = _uiState.value.items.isEmpty()
            if (isInitial) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            when (val result = getSensorPanelUseCase(assetId)) {
                is Outcome.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = result.data,
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
