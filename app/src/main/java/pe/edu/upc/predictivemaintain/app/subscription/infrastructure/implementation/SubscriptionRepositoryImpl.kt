package pe.edu.upc.predictivemaintain.app.subscription.infrastructure.implementation

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.network.ErrorParser
import pe.edu.upc.predictivemaintain.app.core.network.safeApiCall
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.Subscription
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionPlan
import pe.edu.upc.predictivemaintain.app.subscription.domain.repository.SubscriptionRepository
import pe.edu.upc.predictivemaintain.app.subscription.infrastructure.mapper.SubscriptionMapper
import pe.edu.upc.predictivemaintain.app.subscription.infrastructure.remote.ChangePlanRequestDto
import pe.edu.upc.predictivemaintain.app.subscription.infrastructure.remote.SubscriptionApiService
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val apiService: SubscriptionApiService,
    private val errorParser: ErrorParser
) : SubscriptionRepository {

    override suspend fun getSubscription(): Outcome<Subscription> =
        when (val result = safeApiCall(errorParser) { apiService.getSubscription() }) {
            is Outcome.Success -> SubscriptionMapper.toDomain(result.data)
            is Outcome.Failure -> result
        }

    override suspend fun listPlans(): Outcome<List<SubscriptionPlan>> =
        when (val result = safeApiCall(errorParser) { apiService.getPlans() }) {
            is Outcome.Success -> SubscriptionMapper.toDomain(result.data)
            is Outcome.Failure -> result
        }

    override suspend fun changePlan(planId: String): Outcome<Subscription> =
        when (val result = safeApiCall(errorParser) { apiService.changePlan(ChangePlanRequestDto(planId)) }) {
            is Outcome.Success -> SubscriptionMapper.toDomain(result.data)
            is Outcome.Failure -> result
        }
}
