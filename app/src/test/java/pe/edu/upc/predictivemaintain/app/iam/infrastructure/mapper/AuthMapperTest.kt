package pe.edu.upc.predictivemaintain.app.iam.infrastructure.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.local.SessionLocalModel
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.LoginRequestDto
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.LoginResponseDto
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.UserDto

class AuthMapperTest {

    @Test
    fun `map LoginResponseDto to AuthSession domain entity`() {
        val dto = LoginResponseDto(
            accessToken = "secret-access-token-123",
            tokenType = "Bearer",
            expiresAt = "2026-03-01T15:00:00Z",
            userId = "user-123",
            tenantId = "tenant-456",
            roles = listOf("MAINTENANCE_MANAGER", "TECHNICIAN")
        )

        val session = AuthMapper.toDomain(dto)

        assertEquals("user-123", session.userId.value)
        assertEquals("tenant-456", session.tenantId.value)
        assertEquals("secret-access-token-123", session.accessToken.value)
        assertEquals(2, session.roles.size)
        assertTrue(session.roles.contains(Role.MAINTENANCE_MANAGER))
        assertTrue(session.roles.contains(Role.TECHNICIAN))
    }

    @Test
    fun `map UserDto to UserProfile domain entity`() {
        val dto = UserDto(
            id = "user-789",
            tenantId = "tenant-456",
            email = "tech@plant.com",
            displayName = "Jane Doe",
            active = true,
            roles = listOf("OPERATOR")
        )

        val profile = AuthMapper.toDomain(dto)

        assertEquals("user-789", profile.id.value)
        assertEquals("tenant-456", profile.tenantId.value)
        assertEquals("tech@plant.com", profile.email.value)
        assertEquals("Jane Doe", profile.displayName.value)
        assertTrue(profile.active)
        assertEquals(1, profile.roles.size)
        assertEquals(Role.OPERATOR, profile.roles.first())
    }

    @Test
    fun `DTOs and LocalModel toString mask sensitive passwords and tokens`() {
        val loginReq = LoginRequestDto("user@test.com", "MyRawPassword")
        assertFalse(loginReq.toString().contains("MyRawPassword"))
        assertTrue(loginReq.toString().contains("password=***"))

        val loginRes = LoginResponseDto(
            accessToken = "MySecretJwtToken",
            tokenType = "Bearer",
            expiresAt = "2026-03-01T15:00:00Z",
            userId = "u1",
            tenantId = "t1",
            roles = emptyList()
        )
        assertFalse(loginRes.toString().contains("MySecretJwtToken"))
        assertTrue(loginRes.toString().contains("accessToken=***"))

        val localModel = SessionLocalModel(
            userId = "u1",
            tenantId = "t1",
            roles = emptyList(),
            accessToken = "MyStoredSecretToken",
            expiresAt = "2026-03-01T15:00:00Z"
        )
        assertFalse(localModel.toString().contains("MyStoredSecretToken"))
        assertTrue(localModel.toString().contains("accessToken=***"))
    }
}
