package pe.edu.upc.predictivemaintain.app.iam.fakes

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.UserProfile
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.AuthRepository
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.AccessToken
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.DisplayName
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Email
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Password
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.TenantId
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId
import java.time.Instant

class FakeAuthRepository : AuthRepository {

    var loginResult: Outcome<AuthSession>? = null
    var fetchProfileResult: Outcome<UserProfile>? = null

    var lastLoginEmail: Email? = null
    var lastLoginPassword: Password? = null

    override suspend fun login(email: Email, password: Password): Outcome<AuthSession> {
        lastLoginEmail = email
        lastLoginPassword = password
        return loginResult ?: Outcome.Success(
            AuthSession(
                userId = UserId("user-123"),
                tenantId = TenantId("tenant-456"),
                roles = listOf(Role.MAINTENANCE_MANAGER),
                accessToken = AccessToken("fake-token"),
                expiresAt = Instant.now().plusSeconds(3600)
            )
        )
    }

    override suspend fun fetchProfile(): Outcome<UserProfile> {
        return fetchProfileResult ?: Outcome.Success(
            UserProfile(
                id = UserId("user-123"),
                tenantId = TenantId("tenant-456"),
                email = Email("operator@plant.com"),
                displayName = DisplayName("Test User"),
                active = true,
                roles = listOf(Role.MAINTENANCE_MANAGER)
            )
        )
    }
}
