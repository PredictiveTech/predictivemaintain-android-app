package pe.edu.upc.predictivemaintain.app.subscription.infrastructure.mapper

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.Subscription
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionCapacity
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionExpiry
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionPlan
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionStatus
import pe.edu.upc.predictivemaintain.app.subscription.infrastructure.remote.PlanDto
import pe.edu.upc.predictivemaintain.app.subscription.infrastructure.remote.SubscriptionDto
import java.time.Instant
import java.time.format.DateTimeParseException

object SubscriptionMapper {

    fun toDomain(dto: SubscriptionDto): Outcome<Subscription> = mapping {
        val capacityDto = dto.capacity ?: throw InvalidField("capacity")
        val expiryDto = dto.expiry ?: throw InvalidField("expiry")
        val expiry = SubscriptionExpiry(
            daysRemaining = expiryDto.daysRemaining ?: throw InvalidField("expiry.daysRemaining"),
            expiresSoon = expiryDto.expiresSoon,
            expired = expiryDto.expired
        )
        Subscription(
            id = text("id", dto.id),
            status = SubscriptionStatus.fromString(dto.status),
            startsAt = dto.startsAt?.let { instant("startsAt", it) },
            endsAt = instant("endsAt", dto.endsAt),
            monitoringAllowed = dto.monitoringAllowed ?: !expiry.expired,
            plan = plan(dto.plan ?: throw InvalidField("plan")),
            capacity = SubscriptionCapacity(
                limit = nonNegative("capacity.limit", capacityDto.limit?.toLong()).toInt(),
                used = nonNegative("capacity.used", capacityDto.used),
                available = nonNegative("capacity.available", capacityDto.available?.toLong()).toInt(),
                usagePercent = capacityDto.usagePercent?.takeIf { it.isFinite() && it >= 0 }
                    ?: throw InvalidField("capacity.usagePercent"),
                nearLimit = capacityDto.nearLimit
            ),
            expiry = expiry
        )
    }

    fun toDomain(dtos: List<PlanDto>): Outcome<List<SubscriptionPlan>> = mapping { dtos.map(::plan) }

    private fun plan(dto: PlanDto) = SubscriptionPlan(
        id = text("plan.id", dto.id),
        name = text("plan.name", dto.name),
        assetLimit = nonNegative("plan.assetLimit", dto.assetLimit?.toLong()).toInt(),
        amount = dto.amount?.takeIf { it.isFinite() && it >= 0 } ?: throw InvalidField("plan.amount"),
        currency = dto.currency.orEmpty()
    )

    private class InvalidField(val field: String) : RuntimeException("Invalid field: $field")

    private fun text(name: String, value: String?): String =
        value?.takeIf { it.isNotBlank() } ?: throw InvalidField(name)

    private fun instant(name: String, value: String?): Instant = try {
        Instant.parse(value ?: throw InvalidField(name))
    } catch (_: DateTimeParseException) {
        throw InvalidField(name)
    }

    private fun nonNegative(name: String, value: Long?): Long =
        value?.takeIf { it >= 0 } ?: throw InvalidField(name)

    private inline fun <T> mapping(build: () -> T): Outcome<T> = try {
        Outcome.Success(build())
    } catch (e: InvalidField) {
        Outcome.Failure(AppError.InvalidResponse(e.field))
    }
}
