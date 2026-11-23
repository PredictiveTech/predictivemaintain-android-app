package pe.edu.upc.predictivemaintain.app.subscription.fakes

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.Subscription
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionCapacity
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionExpiry
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionPlan
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionStatus
import pe.edu.upc.predictivemaintain.app.subscription.domain.repository.SubscriptionRepository
import java.time.Instant

val basicPlan = SubscriptionPlan("p-basic", "Basic", assetLimit = 10, amount = 49.0, currency = "USD")
val proPlan = SubscriptionPlan("p-pro", "Pro", assetLimit = 50, amount = 149.0, currency = "USD")
val tinyPlan = SubscriptionPlan("p-tiny", "Starter", assetLimit = 3, amount = 9.0, currency = "USD")

fun subscription(
    plan: SubscriptionPlan = proPlan,
    used: Long = 12,
    nearLimit: Boolean = false,
    expiresSoon: Boolean = false,
    expired: Boolean = false,
    daysRemaining: Long = 20
) = Subscription(
    id = "s-1",
    status = SubscriptionStatus.ACTIVE,
    startsAt = Instant.parse("2026-10-01T00:00:00Z"),
    endsAt = Instant.parse("2026-12-13T00:00:00Z"),
    monitoringAllowed = !expired,
    plan = plan,
    capacity = SubscriptionCapacity(
        limit = plan.assetLimit,
        used = used,
        available = (plan.assetLimit - used).toInt().coerceAtLeast(0),
        usagePercent = used * 100.0 / plan.assetLimit,
        nearLimit = nearLimit
    ),
    expiry = SubscriptionExpiry(daysRemaining, expiresSoon, expired)
)

class FakeSubscriptionRepository : SubscriptionRepository {
    var subscriptionResult: Outcome<Subscription> = Outcome.Success(subscription())
    var plansResult: Outcome<List<SubscriptionPlan>> = Outcome.Success(listOf(proPlan, tinyPlan, basicPlan))
    var changeResult: Outcome<Subscription> = Outcome.Failure(AppError.Unknown())

    var getCalls = 0
    val changeCalls = mutableListOf<String>()

    override suspend fun getSubscription(): Outcome<Subscription> {
        getCalls++
        return subscriptionResult
    }

    override suspend fun listPlans(): Outcome<List<SubscriptionPlan>> = plansResult

    override suspend fun changePlan(planId: String): Outcome<Subscription> {
        changeCalls += planId
        return changeResult
    }
}
