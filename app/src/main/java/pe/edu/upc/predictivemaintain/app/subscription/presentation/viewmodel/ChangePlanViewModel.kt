package pe.edu.upc.predictivemaintain.app.subscription.presentation.viewmodel

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
import pe.edu.upc.predictivemaintain.app.subscription.application.usecase.ChangePlanUseCase
import pe.edu.upc.predictivemaintain.app.subscription.application.usecase.GetSubscriptionUseCase
import pe.edu.upc.predictivemaintain.app.subscription.application.usecase.ListPlansUseCase
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionPlan
import pe.edu.upc.predictivemaintain.app.subscription.presentation.state.ChangePlanUiState
import pe.edu.upc.predictivemaintain.app.subscription.presentation.state.PlanChangeFailure
import javax.inject.Inject

/** US-17: lists the plans (marking the current one) and changes the plan. */
@HiltViewModel
class ChangePlanViewModel @Inject constructor(
    private val getSubscription: GetSubscriptionUseCase,
    private val listPlans: ListPlansUseCase,
    private val changePlan: ChangePlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePlanUiState())
    val uiState: StateFlow<ChangePlanUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            val subscription = when (val result = getSubscription()) {
                is Outcome.Success -> result.data
                is Outcome.Failure -> return@launch fail(result.error)
            }
            val plans = when (val result = listPlans()) {
                is Outcome.Success -> result.data
                is Outcome.Failure -> return@launch fail(result.error)
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    subscription = subscription,
                    plans = plans.sortedWith(compareBy({ plan -> plan.assetLimit }, { plan -> plan.amount }))
                )
            }
        }
    }

    private fun fail(error: AppError) {
        _uiState.update { it.copy(isLoading = false, loadError = error.toSubscriptionMessage()) }
    }

    /** The manager tapped a plan. Asking for the plan already in use does nothing. */
    fun onPlanSelected(plan: SubscriptionPlan) {
        val state = _uiState.value
        if (state.isChanging || plan.id == state.currentPlanId) return
        _uiState.update { it.copy(planToConfirm = plan, failure = null) }
    }

    fun dismissConfirmation() {
        _uiState.update { it.copy(planToConfirm = null) }
    }

    fun dismissFailure() {
        _uiState.update { it.copy(failure = null) }
    }

    fun confirmChange() {
        val state = _uiState.value
        val plan = state.planToConfirm ?: return
        if (state.isChanging) return
        viewModelScope.launch {
            _uiState.update { it.copy(planToConfirm = null, isChanging = true, failure = null) }
            when (val result = changePlan(plan.id)) {
                is Outcome.Success -> _uiState.update {
                    it.copy(isChanging = false, subscription = result.data, changed = true)
                }
                is Outcome.Failure -> onChangeFailed(plan, result.error)
            }
        }
    }

    private suspend fun onChangeFailed(plan: SubscriptionPlan, error: AppError) {
        if (error is AppError.Api && error.httpStatus == 409) {
            // The count of assets in use may have moved since the screen opened: read it again so the
            // number shown to the manager is the one the server just judged against.
            val fresh = (getSubscription() as? Outcome.Success)?.data ?: _uiState.value.subscription
            val toDeactivate = fresh?.assetsToDeactivateFor(plan) ?: 0
            val failure = if (fresh != null && toDeactivate > 0) {
                PlanChangeFailure.PlanTooSmall(plan, fresh.capacity.used, toDeactivate)
            } else {
                PlanChangeFailure.Other(error.detail)
            }
            _uiState.update { it.copy(isChanging = false, subscription = fresh ?: it.subscription, failure = failure) }
        } else {
            _uiState.update {
                it.copy(isChanging = false, failure = PlanChangeFailure.Other(error.toSubscriptionMessage()))
            }
        }
    }
}
