package pe.edu.upc.predictivemaintain.app.subscription.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.Subscription
import pe.edu.upc.predictivemaintain.app.subscription.domain.repository.SubscriptionRepository
import javax.inject.Inject

class ChangePlanUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(planId: String): Outcome<Subscription> = repository.changePlan(planId)
}
