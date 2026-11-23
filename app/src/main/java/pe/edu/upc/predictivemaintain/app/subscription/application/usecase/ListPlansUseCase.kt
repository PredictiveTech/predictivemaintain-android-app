package pe.edu.upc.predictivemaintain.app.subscription.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionPlan
import pe.edu.upc.predictivemaintain.app.subscription.domain.repository.SubscriptionRepository
import javax.inject.Inject

class ListPlansUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(): Outcome<List<SubscriptionPlan>> = repository.listPlans()
}
