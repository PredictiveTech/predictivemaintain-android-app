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
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.CompareAssetsUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.ExportHistoryUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.ListAssetsUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Asset
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.ComparisonSort
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.DateRange
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.AssetComparisonUiState
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** US-15 (compare several assets over a date range) and the export action of US-16. */
@HiltViewModel
class AssetComparisonViewModel @Inject constructor(
    private val listAssets: ListAssetsUseCase,
    private val compareAssets: CompareAssetsUseCase,
    private val exportHistory: ExportHistoryUseCase,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AssetComparisonUiState(range = DateRange.lastDays(LocalDate.now(clock)))
    )
    val uiState: StateFlow<AssetComparisonUiState> = _uiState.asStateFlow()

    init {
        loadAssets()
    }

    fun loadAssets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAssets = true, assetsError = null) }
            val all = mutableListOf<Asset>()
            var page = 0
            while (page < MAX_PAGES) {
                when (val result = listAssets(status = null, page = page, size = PAGE_SIZE)) {
                    is Outcome.Success -> {
                        all += result.data.items
                        if (!result.data.hasMore) break
                        page++
                    }
                    is Outcome.Failure -> {
                        _uiState.update {
                            it.copy(isLoadingAssets = false, assetsError = result.error.toReportMessage())
                        }
                        return@launch
                    }
                }
            }
            // An inactive asset has no monitoring to compare.
            val selectable = all.filter { it.active }
            _uiState.update { state ->
                state.copy(
                    isLoadingAssets = false,
                    assets = selectable,
                    selectedIds = state.selectedIds.intersect(selectable.map { it.id.value }.toSet())
                )
            }
        }
    }

    fun toggleAsset(assetId: String) {
        _uiState.update { state ->
            when {
                assetId in state.selectedIds ->
                    state.copy(selectedIds = state.selectedIds - assetId, maxSelectionReached = false)
                state.selectedIds.size >= CompareAssetsUseCase.MAX_ASSETS ->
                    state.copy(maxSelectionReached = true)
                else -> state.copy(selectedIds = state.selectedIds + assetId, maxSelectionReached = false)
            }
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedIds = emptySet(), maxSelectionReached = false) }
    }

    fun onRangeChange(range: DateRange) {
        _uiState.update { it.copy(range = range, rangeError = null) }
    }

    fun onSortChange(sort: ComparisonSort) {
        _uiState.update { state -> state.copy(sort = sort, rows = state.rows?.let(sort::sort)) }
    }

    fun compare() {
        val state = _uiState.value
        if (!state.canRun) return
        val error = state.range.validate(clock.zone, clock.instant())
        if (error != null) {
            _uiState.update { it.copy(rangeError = error) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isComparing = true, rangeError = null, errorMessage = null) }
            val period = state.range.toPeriod(clock.zone, clock.instant())
            when (val result = compareAssets(state.selectedIds.toList(), period)) {
                is Outcome.Success -> _uiState.update {
                    it.copy(isComparing = false, rows = it.sort.sort(result.data))
                }
                is Outcome.Failure -> _uiState.update {
                    it.copy(isComparing = false, errorMessage = result.error.toReportMessage())
                }
            }
        }
    }

    fun export() {
        val state = _uiState.value
        if (!state.canRun) return
        val error = state.range.validate(clock.zone, clock.instant())
        if (error != null) {
            _uiState.update { it.copy(rangeError = error) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, rangeError = null, exportError = null) }
            val period = state.range.toPeriod(clock.zone, clock.instant())
            when (val result = exportHistory(state.selectedIds.toList(), period)) {
                is Outcome.Success -> _uiState.update { it.copy(isExporting = false, exported = result.data) }
                is Outcome.Failure -> _uiState.update {
                    it.copy(isExporting = false, exportError = result.error.toReportMessage())
                }
            }
        }
    }

    fun onExportShared() {
        _uiState.update { it.copy(exported = null) }
    }

    fun dismissExportError() {
        _uiState.update { it.copy(exportError = null) }
    }

    private companion object {
        const val PAGE_SIZE = 50
        const val MAX_PAGES = 20
    }
}
