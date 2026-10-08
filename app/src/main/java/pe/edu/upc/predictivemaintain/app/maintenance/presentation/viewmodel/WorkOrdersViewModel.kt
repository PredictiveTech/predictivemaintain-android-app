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
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.ListTechniciansUseCase
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.SessionRepository
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.ListWorkOrdersUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import pe.edu.upc.predictivemaintain.app.maintenance.presentation.state.WorkOrdersUiState
import javax.inject.Inject

@HiltViewModel
class WorkOrdersViewModel @Inject constructor(
    private val listWorkOrdersUseCase: ListWorkOrdersUseCase,
    private val listTechniciansUseCase: ListTechniciansUseCase,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkOrdersUiState())
    val uiState: StateFlow<WorkOrdersUiState> = _uiState.asStateFlow()

    private var initialLoaded = false

    fun init() {
        if (initialLoaded) return
        initialLoaded = true
        setupActorAndLoad()
    }

    private fun setupActorAndLoad() {
        viewModelScope.launch {
            val session = sessionRepository.currentSession()
            val isManager = session?.roles?.contains(Role.MAINTENANCE_MANAGER) == true
            val isTechnician = session?.roles?.contains(Role.TECHNICIAN) == true
            _uiState.update { it.copy(isManager = isManager, isTechnician = isTechnician) }

            if (isManager) {
                when (val result = listTechniciansUseCase()) {
                    is Outcome.Success -> {
                        val map = result.data.associate { it.id.value to it.displayName.value }
                        _uiState.update { it.copy(technicianNames = map) }
                    }
                    is Outcome.Failure -> { /* Ignore error on background technician load */ }
                }
            }
            loadOrdersInternal(page = 0, isRefresh = false)
        }
    }

    fun filterByStatus(status: WorkOrderStatus?) {
        if (_uiState.value.selectedStatus == status) return
        _uiState.update { it.copy(selectedStatus = status, currentPage = 0) }
        loadOrders(page = 0, isRefresh = false)
    }

    fun refresh() {
        loadOrders(page = 0, isRefresh = true)
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || state.currentPage + 1 >= state.totalPages) return
        loadOrders(page = state.currentPage + 1, isRefresh = false, isLoadMore = true)
    }

    private fun loadOrders(page: Int, isRefresh: Boolean, isLoadMore: Boolean = false) {
        viewModelScope.launch {
            loadOrdersInternal(page, isRefresh, isLoadMore)
        }
    }

    private suspend fun loadOrdersInternal(page: Int, isRefresh: Boolean, isLoadMore: Boolean = false) {
        _uiState.update {
            when {
                isRefresh -> it.copy(isRefreshing = true, errorMessage = null)
                isLoadMore -> it.copy(isLoadingMore = true, errorMessage = null)
                else -> it.copy(isLoading = true, isErrorState = false, errorMessage = null)
            }
        }

        val currentStatus = _uiState.value.selectedStatus
        when (val result = listWorkOrdersUseCase(status = currentStatus, page = page)) {
            is Outcome.Success -> {
                val pageResult = result.data
                val newOrders = if (isLoadMore) {
                    _uiState.value.orders + pageResult.items
                } else {
                    pageResult.items
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        orders = newOrders,
                        currentPage = pageResult.page,
                        totalPages = pageResult.totalPages,
                        isErrorState = false,
                        errorMessage = null
                    )
                }
            }
            is Outcome.Failure -> {
                val message = formatError(result.error)
                val currentOrders = _uiState.value.orders
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        errorMessage = message,
                        isErrorState = currentOrders.isEmpty() && !isRefresh && !isLoadMore
                    )
                }
            }
        }
    }

    private fun formatError(error: AppError): String {
        return when (error) {
            is AppError.Api -> error.detail
            is AppError.Network -> "Network error. Please check your connection."
            is AppError.InvalidResponse -> "Unexpected server response."
            is AppError.SessionExpired -> "Session expired."
            is AppError.Unknown -> "An unexpected error occurred."
        }
    }
}
