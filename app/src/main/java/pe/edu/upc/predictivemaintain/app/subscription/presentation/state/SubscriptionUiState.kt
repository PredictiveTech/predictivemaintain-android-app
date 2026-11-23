package pe.edu.upc.predictivemaintain.app.subscription.presentation.state

import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.Subscription
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionPlan

/** US-18. */
data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val subscription: Subscription? = null,
    val errorMessage: String? = null
)

/** Why a plan change did not happen. */
sealed interface PlanChangeFailure {
    /** The 409 of a downgrade: the destination plan holds fewer assets than are in use. */
    data class PlanTooSmall(val plan: SubscriptionPlan, val assetsInUse: Long, val assetsToDeactivate: Int) : PlanChangeFailure

    data class Other(val message: String) : PlanChangeFailure
}

/** US-17. */
data class ChangePlanUiState(
    val isLoading: Boolean = false,
    val subscription: Subscription? = null,
    /** Cheapest/smallest first. */
    val plans: List<SubscriptionPlan> = emptyList(),
    val loadError: String? = null,
    /** The plan the manager tapped and has to confirm. */
    val planToConfirm: SubscriptionPlan? = null,
    val isChanging: Boolean = false,
    val failure: PlanChangeFailure? = null,
    /** Set once the server confirmed the change, so the screen can go back and refresh. */
    val changed: Boolean = false
) {
    val currentPlanId: String? get() = subscription?.plan?.id
}
