package pe.edu.upc.predictivemaintain.app.iam.infrastructure.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.AccessToken
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.DisplayName
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Email
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.TenantId
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.LoginResponseDto
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.UserDto
import java.time.Instant

class AuthMapperTest {

    private fun loginDto(
        accessToken: String = "token-123",
        expiresAt: String = "2026-10-07T18:00:00Z",
        userId: String = "user-1",
        tenantId: String = "tenant-1",
        roles: List<String> = listOf("MAINTENANCE_MANAGER")
    ) = LoginResponseDto(
        accessToken = accessToken, tokenType = "Bearer", expiresAt = expiresAt,
        userId = userId, tenantId = tenantId, roles = roles
    )

    private fun userDto(
        id: String = "user-1",
        tenantId: String = "tenant-1",
        email: String = "jefe.demo@example.com",
        displayName: String = "Jefe Demo",
        active: Boolean = true,
        roles: List<String> = listOf("TECHNICIAN")
    ) = UserDto(
        id = id, tenantId = tenantId, email = email, displayName = displayName,
        active = active, roles = roles
    )

    private fun assertInvalid(field: String, outcome: Outcome<*>) {
        assertTrue("expected a Failure but got $outcome", outcome is Outcome.Failure)
        assertEquals(AppError.InvalidResponse(field), (outcome as Outcome.Failure).error)
    }

    // ---- login response -> AuthSession

    @Test
    fun validLoginResponseBecomesSession() {
        val outcome = AuthMapper.toDomain(loginDto())

        assertTrue("expected a Success but got $outcome", outcome is Outcome.Success)
        val session = (outcome as Outcome.Success).data
        assertEquals(UserId("user-1"), session.userId)
        assertEquals(TenantId("tenant-1"), session.tenantId)
        assertEquals(listOf(Role.MAINTENANCE_MANAGER), session.roles)
        assertEquals(AccessToken("token-123"), session.accessToken)
        assertEquals(Instant.parse("2026-10-07T18:00:00Z"), session.expiresAt)
    }

    @Test
    fun unknownRoleRejectsTheWholeResponseInsteadOfDroppingIt() {
        assertInvalid("roles", AuthMapper.toDomain(loginDto(roles = listOf("MAINTENANCE_MANAGER", "SUPERUSER"))))
    }

    @Test
    fun emptyRoleListIsRejected() {
        assertInvalid("roles", AuthMapper.toDomain(loginDto(roles = emptyList())))
    }

    @Test
    fun roleNamesAreMatchedIgnoringCase() {
        val outcome = AuthMapper.toDomain(loginDto(roles = listOf("technician")))

        assertTrue(outcome is Outcome.Success)
        assertEquals(listOf(Role.TECHNICIAN), (outcome as Outcome.Success).data.roles)
    }

    @Test
    fun malformedExpirationIsRejectedAndNeverInvented() {
        assertInvalid("expiresAt", AuthMapper.toDomain(loginDto(expiresAt = "not-a-date")))
        assertInvalid("expiresAt", AuthMapper.toDomain(loginDto(expiresAt = "")))
        assertInvalid("expiresAt", AuthMapper.toDomain(loginDto(expiresAt = "2026-10-07 18:00:00")))
    }

    @Test
    fun blankIdsAndTokenAreRejected() {
        assertInvalid("userId", AuthMapper.toDomain(loginDto(userId = " ")))
        assertInvalid("tenantId", AuthMapper.toDomain(loginDto(tenantId = "")))
        assertInvalid("accessToken", AuthMapper.toDomain(loginDto(accessToken = "  ")))
    }

    @Test
    fun loginDtoNeverPrintsTheToken() {
        val printed = loginDto(accessToken = "super-secret-token").toString()

        assertFalse("the token leaked in toString(): $printed", printed.contains("super-secret-token"))
    }

    // ---- user response -> UserProfile

    @Test
    fun validUserBecomesProfileWithTrimmedEmail() {
        val outcome = AuthMapper.toDomain(userDto(email = "  jefe.demo@example.com "))

        assertTrue("expected a Success but got $outcome", outcome is Outcome.Success)
        val profile = (outcome as Outcome.Success).data
        assertEquals(Email("jefe.demo@example.com"), profile.email)
        assertEquals(DisplayName("Jefe Demo"), profile.displayName)
        assertEquals(listOf(Role.TECHNICIAN), profile.roles)
        assertTrue(profile.active)
    }

    @Test
    fun invalidEmailIsRejected() {
        assertInvalid("email", AuthMapper.toDomain(userDto(email = "not-an-email")))
    }

    @Test
    fun blankDisplayNameIsRejected() {
        assertInvalid("displayName", AuthMapper.toDomain(userDto(displayName = "  ")))
    }

    @Test
    fun userWithUnknownOrNoRolesIsRejected() {
        assertInvalid("roles", AuthMapper.toDomain(userDto(roles = listOf("GUEST"))))
        assertInvalid("roles", AuthMapper.toDomain(userDto(roles = emptyList())))
    }

    @Test
    fun parseRolesKeepsTheOrderAndRejectsBadInput() {
        assertEquals(
            listOf(Role.OPERATOR, Role.TECHNICIAN),
            AuthMapper.parseRoles(listOf("OPERATOR", "TECHNICIAN"))
        )
        var rejected = false
        try {
            AuthMapper.parseRoles(listOf("OPERATOR", "NOPE"))
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
    }
}