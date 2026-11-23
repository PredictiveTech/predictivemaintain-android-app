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
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.ExportHistoryUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.GetAssetAvailabilityUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.DateRange
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.AssetAvailabilityUiState
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** US-14 (availability of one asset over a date range) and the export action of US-16. */
@HiltViewModel
class AssetAvailabilityViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAvailability: GetAssetAvailabilityUseCase,
    private val exportHistory: ExportHistoryUseCase,
    private val clock: Clock
) : ViewModel() {

    private val assetId: String = checkNotNull(savedStateHandle[ARG_ASSET_ID]) { "assetId argument is required" }

    private val _uiState = MutableStateFlow(
        AssetAvailabilityUiState(range = DateRange.lastDays(LocalDate.now(clock)))
    )
    val uiState: StateFlow<AssetAvailabilityUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRangeChange(range: DateRange) {
        _uiState.update { it.copy(range = range) }
        load()
    }

    fun load() {
        val range = _uiState.value.range
        val error = range.validate(clock.zone, clock.instant())
        if (error != null) {
            _uiState.update { it.copy(rangeError = error, errorMessage = null, report = null, isLoading = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, rangeError = null, errorMessage = null) }
            val period = range.toPeriod(clock.zone, clock.instant())
            when (val result = getAvailability(assetId, period)) {
                is Outcome.Success -> _uiState.update { it.copy(isLoading = false, report = result.data) }
                is Outcome.Failure -> _uiState.update {
                    it.copy(isLoading = false, report = null, errorMessage = result.error.toReportMessage())
                }
            }
        }
    }

    fun export() {
        val state = _uiState.value
        if (state.isExporting) return
        val error = state.range.validate(clock.zone, clock.instant())
        if (error != null) {
            _uiState.update { it.copy(rangeError = error) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null) }
            val period = state.range.toPeriod(clock.zone, clock.instant())
            when (val result = exportHistory(listOf(assetId), period)) {
                is Outcome.Success -> _uiState.update { it.copy(isExporting = false, exported = result.data) }
                is Outcome.Failure -> _uiState.update {
                    it.copy(isExporting = false, exportError = result.error.toReportMessage())
                }
            }
        }
    }

    /** The screen opened the share sheet for the exported file. */
    fun onExportShared() {
        _uiState.update { it.copy(exported = null) }
    }

    fun dismissExportError() {
        _uiState.update { it.copy(exportError = null) }
    }

    companion object {
        const val ARG_ASSET_ID = "assetId"
    }
}
