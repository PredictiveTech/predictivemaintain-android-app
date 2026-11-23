package pe.edu.upc.predictivemaintain.app.subscription.infrastructure.remote

import kotlinx.serialization.Serializable

// Copied from the OpenAPI spec (SubscriptionResource, PlanInfo, CapacityInfo, ExpiryInfo,
// SubscriptionPlanResource, ChangePlanResource). The spec marks no response field as required,
// so the mapper checks what the screens depend on.

@Serializable
data class PlanDto(
    val id: String? = null,
    val name: String? = null,
    val assetLimit: Int? = null,
    val amount: Double? = null,
    val currency: String? = null
)

@Serializable
data class CapacityDto(
    val limit: Int? = null,
    val used: Long? = null,
    val available: Int? = null,
    val usagePercent: Double? = null,
    val nearLimit: Boolean = false
)

@Serializable
data class ExpiryDto(
    val daysRemaining: Long? = null,
    val expiresSoon: Boolean = false,
    val expired: Boolean = false
)

@Serializable
data class SubscriptionDto(
    val id: String? = null,
    val status: String? = null,
    val startsAt: String? = null,
    val endsAt: String? = null,
    val monitoringAllowed: Boolean? = null,
    val plan: PlanDto? = null,
    val capacity: CapacityDto? = null,
    val expiry: ExpiryDto? = null
)

@Serializable
data class ChangePlanRequestDto(val planId: String)
