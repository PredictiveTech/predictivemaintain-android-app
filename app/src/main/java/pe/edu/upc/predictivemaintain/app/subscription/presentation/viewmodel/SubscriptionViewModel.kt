package pe.edu.upc.predictivemaintain.app.subscription.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.subscription.application.usecase.GetSubscriptionUseCase
import pe.edu.upc.predictivemaintain.app.subscription.presentation.state.SubscriptionUiState
import javax.inject.Inject

/** US-18: current plan, assets used against the limit and renewal date. */
@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val getSubscription: GetSubscriptionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getSubscription()) {
                is Outcome.Success -> _uiState.update { it.copy(isLoading = false, subscription = result.data) }
                is Outcome.Failure -> _uiState.update {
                    // Keep the last known data on screen when a refresh fails.
                    it.copy(isLoading = false, errorMessage = result.error.toSubscriptionMessage())
                }
            }
        }
    }
}
