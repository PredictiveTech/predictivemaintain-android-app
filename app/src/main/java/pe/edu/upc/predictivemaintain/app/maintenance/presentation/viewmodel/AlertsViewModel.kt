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
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.ListAlertsUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.AlertsUiState
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val listAlertsUseCase: ListAlertsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    init {
        loadAlerts(page = 0, isRefresh = false)
    }

    fun setAssetIdArg(raw: String?) {
        val parsedAssetId = parseAssetId(raw)
        val newFilter = parsedAssetId?.value
        if (_uiState.value.assetIdFilter == newFilter) return
        _uiState.update { it.copy(assetIdFilter = newFilter, page = 0, items = emptyList()) }
        loadAlerts(page = 0, isRefresh = false)
    }

    private fun parseAssetId(raw: String?): AssetId? {
        if (raw.isNullOrBlank() || raw == "{assetId}") return null
        val trimmed = raw.trim()
        return try {
            UUID.fromString(trimmed)
            AssetId(trimmed)
        } catch (_: Exception) {
            null
        }
    }

    fun setStatusFilter(status: String?) {
        if (_uiState.value.selectedStatusFilter == status) return
        _uiState.update { it.copy(selectedStatusFilter = status, page = 0, items = emptyList()) }
        loadAlerts(page = 0, isRefresh = false)
    }

    fun refresh() {
        loadAlerts(page = 0, isRefresh = true)
    }

    fun loadMore() {
        val currentState = _uiState.value
        if (currentState.isLoadingMore || !currentState.hasMore) return
        loadAlerts(page = currentState.page + 1, isLoadMore = true)
    }

    private fun loadAlerts(page: Int, isRefresh: Boolean = false, isLoadMore: Boolean = false) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update {
                it.copy(
                    isLoading = page == 0 && !isRefresh,
                    isRefreshing = isRefresh,
                    isLoadingMore = isLoadMore,
                    errorMessage = null
                )
            }

            when (val result = listAlertsUseCase(
                severity = state.selectedSeverityFilter,
                status = state.selectedStatusFilter,
                assetId = state.assetIdFilter,
                page = page
            )) {
                is Outcome.Success -> {
                    val pageResult = result.data
                    _uiState.update { current ->
                        val newItems = if (page == 0) pageResult.items else current.items + pageResult.items
                        current.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                            items = newItems,
                            page = pageResult.page,
                            totalPages = pageResult.totalPages,
                            hasMore = pageResult.hasMore,
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
                            isRefreshing = false,
                            isLoadingMore = false,
                            errorMessage = message
                        )
                    }
                }
            }
        }
    }
}
