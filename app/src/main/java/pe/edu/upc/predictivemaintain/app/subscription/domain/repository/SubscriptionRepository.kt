package pe.edu.upc.predictivemaintain.app.subscription.domain.repository

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.Subscription
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionPlan

interface SubscriptionRepository {
    suspend fun getSubscription(): Outcome<Subscription>
    suspend fun listPlans(): Outcome<List<SubscriptionPlan>>
    suspend fun changePlan(planId: String): Outcome<Subscription>
}
