package pe.edu.upc.predictivemaintain.app.subscription.infrastructure.mapper

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.subscription.domain.entity.SubscriptionStatus
import pe.edu.upc.predictivemaintain.app.subscription.infrastructure.remote.CapacityDto
import pe.edu.upc.predictivemaintain.app.subscription.infrastructure.remote.ExpiryDto
import pe.edu.upc.predictivemaintain.app.subscription.infrastructure.remote.PlanDto
import pe.edu.upc.predictivemaintain.app.subscription.infrastructure.remote.SubscriptionDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class SubscriptionMapperTest {

    private val plan = PlanDto("p-1", "Pro", 50, 149.0, "USD")

    private fun dto(
        plan: PlanDto? = this.plan,
        capacity: CapacityDto? = CapacityDto(limit = 50, used = 46, available = 4, usagePercent = 92.0, nearLimit = true),
        expiry: ExpiryDto? = ExpiryDto(daysRemaining = 5, expiresSoon = true, expired = false),
        endsAt: String? = "2026-12-01T00:00:00Z",
        monitoringAllowed: Boolean? = true,
        status: String? = "ACTIVE"
    ) = SubscriptionDto(
        id = "s-1", status = status, startsAt = "2026-11-01T00:00:00Z", endsAt = endsAt,
        monitoringAllowed = monitoringAllowed, plan = plan, capacity = capacity, expiry = expiry
    )

    private fun field(result: Outcome<*>) = ((result as Outcome.Failure).error as AppError.InvalidResponse).field

    @Test
    fun `maps the whole subscription, taking the flags from capacity and expiry`() {
        val s = (SubscriptionMapper.toDomain(dto()) as Outcome.Success).data

        assertEquals("s-1", s.id)
        assertEquals(SubscriptionStatus.ACTIVE, s.status)
        assertEquals(Instant.parse("2026-12-01T00:00:00Z"), s.endsAt)
        assertEquals("Pro", s.plan.name)
        assertEquals(50, s.plan.assetLimit)
        assertEquals(149.0, s.plan.amount, 0.0)
        assertEquals(46L, s.capacity.used)
        assertEquals(4, s.capacity.available)
        assertEquals(92.0, s.capacity.usagePercent, 0.0)
        assertTrue(s.capacity.nearLimit)
        assertTrue(s.expiry.expiresSoon)
        assertFalse(s.expiry.expired)
        assertEquals(5L, s.expiry.daysRemaining)
        assertTrue(s.monitoringAllowed)
    }

    @Test
    fun `flags default to false when the server leaves them out`() {
        val s = (SubscriptionMapper.toDomain(
            dto(capacity = CapacityDto(10, 2, 8, 20.0), expiry = ExpiryDto(daysRemaining = 30))
        ) as Outcome.Success).data

        assertFalse(s.capacity.nearLimit)
        assertFalse(s.expiry.expiresSoon)
    }

    @Test
    fun `an expired subscription does not allow monitoring when the flag is missing`() {
        val s = (SubscriptionMapper.toDomain(
            dto(monitoringAllowed = null, expiry = ExpiryDto(daysRemaining = 0, expired = true))
        ) as Outcome.Success).data

        assertFalse(s.monitoringAllowed)
    }

    @Test
    fun `unknown status maps to UNKNOWN and a missing start is allowed`() {
        val s = (SubscriptionMapper.toDomain(dto(status = "SOMETHING_NEW").copy(startsAt = null)) as Outcome.Success).data

        assertEquals(SubscriptionStatus.UNKNOWN, s.status)
        assertNull(s.startsAt)
    }

    @Test
    fun `missing plan capacity or expiry is invalid`() {
        assertEquals("plan", field(SubscriptionMapper.toDomain(dto(plan = null))))
        assertEquals("capacity", field(SubscriptionMapper.toDomain(dto(capacity = null))))
        assertEquals("expiry", field(SubscriptionMapper.toDomain(dto(expiry = null))))
    }

    @Test
    fun `bad end date is invalid`() {
        assertEquals("endsAt", field(SubscriptionMapper.toDomain(dto(endsAt = "soon"))))
        assertEquals("endsAt", field(SubscriptionMapper.toDomain(dto(endsAt = null))))
    }

    @Test
    fun `negative or missing usage numbers are invalid`() {
        val negative = CapacityDto(limit = 50, used = -1, available = 4, usagePercent = 1.0)
        val noPercent = CapacityDto(limit = 50, used = 1, available = 4, usagePercent = null)

        assertEquals("capacity.used", field(SubscriptionMapper.toDomain(dto(capacity = negative))))
        assertEquals("capacity.usagePercent", field(SubscriptionMapper.toDomain(dto(capacity = noPercent))))
    }

    @Test
    fun `plan with blank id or missing limit is invalid`() {
        assertEquals("plan.id", field(SubscriptionMapper.toDomain(dto(plan = plan.copy(id = " ")))))
        assertEquals("plan.assetLimit", field(SubscriptionMapper.toDomain(dto(plan = plan.copy(assetLimit = null)))))
    }

    @Test
    fun `plans map in the given order`() {
        val plans = (SubscriptionMapper.toDomain(
            listOf(plan, PlanDto("p-2", "Basic", 10, 49.0, "USD"))
        ) as Outcome.Success).data

        assertEquals(listOf("Pro", "Basic"), plans.map { it.name })
        assertEquals("USD", plans[1].currency)
    }

    @Test
    fun `a plan without amount is invalid and an empty list is fine`() {
        assertEquals("plan.amount", field(SubscriptionMapper.toDomain(listOf(plan.copy(amount = null)))))
        assertEquals(emptyList<Any>(), (SubscriptionMapper.toDomain(emptyList<PlanDto>()) as Outcome.Success).data)
    }
}
