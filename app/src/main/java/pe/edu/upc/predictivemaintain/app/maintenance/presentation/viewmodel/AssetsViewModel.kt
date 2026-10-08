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
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.ListAssetsUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.AssetsUiState
import javax.inject.Inject

@HiltViewModel
class AssetsViewModel @Inject constructor(
    private val listAssetsUseCase: ListAssetsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssetsUiState())
    val uiState: StateFlow<AssetsUiState> = _uiState.asStateFlow()

    init {
        loadAssets(page = 0, isRefresh = false)
    }

    fun setStatusFilter(status: String?) {
        if (_uiState.value.selectedStatusFilter == status) return
        _uiState.update { it.copy(selectedStatusFilter = status, page = 0, items = emptyList()) }
        loadAssets(page = 0, isRefresh = false)
    }

    fun refresh() {
        loadAssets(page = 0, isRefresh = true)
    }

    fun loadMore() {
        val currentState = _uiState.value
        if (currentState.isLoadingMore || !currentState.hasMore) return
        loadAssets(page = currentState.page + 1, isLoadMore = true)
    }

    private fun loadAssets(page: Int, isRefresh: Boolean = false, isLoadMore: Boolean = false) {
        viewModelScope.launch {
            val filter = _uiState.value.selectedStatusFilter
            _uiState.update {
                it.copy(
                    isLoading = page == 0 && !isRefresh,
                    isRefreshing = isRefresh,
                    isLoadingMore = isLoadMore,
                    errorMessage = null
                )
            }

            when (val result = listAssetsUseCase(status = filter, page = page)) {
                is Outcome.Success -> {
                    val pageResult = result.data
                    _uiState.update { state ->
                        val newItems = if (page == 0) pageResult.items else state.items + pageResult.items
                        state.copy(
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
