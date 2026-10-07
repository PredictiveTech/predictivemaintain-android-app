package pe.edu.upc.predictivemaintain.app.iam.application.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.UserProfile
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.AccessToken
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.DisplayName
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Email
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.TenantId
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeAuthRepository
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeSessionRepository
import java.time.Instant

class RestoreSessionUseCaseTest {

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeSessionRepository: FakeSessionRepository
    private lateinit var restoreSessionUseCase: RestoreSessionUseCase

    private val now = Instant.parse("2026-03-01T12:00:00Z")

    @Before
    fun setUp() {
        fakeAuthRepository = FakeAuthRepository()
        fakeSessionRepository = FakeSessionRepository()
        restoreSessionUseCase = RestoreSessionUseCase(fakeSessionRepository, fakeAuthRepository)
    }

    @Test
    fun `restore session succeeds when stored session is valid and online profile fetch succeeds`() = runTest {
        val validSession = AuthSession(
            userId = UserId("user-1"),
            tenantId = TenantId("tenant-1"),
            roles = listOf(Role.OPERATOR),
            accessToken = AccessToken("valid-token"),
            expiresAt = now.plusSeconds(1800)
        )
        fakeSessionRepository.saveSession(validSession)
        fakeAuthRepository.fetchProfileResult = Outcome.Success(
            UserProfile(
                id = UserId("user-1"),
                tenantId = TenantId("tenant-1"),
                email = Email("operator@plant.com"),
                displayName = DisplayName("Operator"),
                active = true,
                roles = listOf(Role.OPERATOR)
            )
        )

        val result = restoreSessionUseCase(now)

        assertTrue(result is Outcome.Success)
        assertEquals(validSession, (result as Outcome.Success).data)
        assertNotNull(fakeSessionRepository.currentSession())
    }

    @Test
    fun `restore session keeps stored session when network is offline`() = runTest {
        val validSession = AuthSession(
            userId = UserId("user-1"),
            tenantId = TenantId("tenant-1"),
            roles = listOf(Role.OPERATOR),
            accessToken = AccessToken("valid-token"),
            expiresAt = now.plusSeconds(1800)
        )
        fakeSessionRepository.saveSession(validSession)
        fakeAuthRepository.fetchProfileResult = Outcome.Failure(AppError.Network())

        val result = restoreSessionUseCase(now)

        assertTrue(result is Outcome.Success)
        assertEquals(validSession, (result as Outcome.Success).data)
        assertEquals(validSession, fakeSessionRepository.currentSession())
    }

    @Test
    fun `restore session keeps stored session when the server response is invalid`() = runTest {
        val validSession = AuthSession(
            userId = UserId("user-1"),
            tenantId = TenantId("tenant-1"),
            roles = listOf(Role.OPERATOR),
            accessToken = AccessToken("valid-token"),
            expiresAt = now.plusSeconds(1800)
        )
        fakeSessionRepository.saveSession(validSession)
        fakeAuthRepository.fetchProfileResult = Outcome.Failure(AppError.InvalidResponse("roles"))

        val result = restoreSessionUseCase(now)

        assertTrue(result is Outcome.Success)
        assertEquals(validSession, (result as Outcome.Success).data)
        assertEquals(validSession, fakeSessionRepository.currentSession())
    }


    @Test
    fun `restore session clears session when stored session is expired`() = runTest {
        val expiredSession = AuthSession(
            userId = UserId("user-1"),
            tenantId = TenantId("tenant-1"),
            roles = listOf(Role.OPERATOR),
            accessToken = AccessToken("old-token"),
            expiresAt = now.minusSeconds(60) // Expired 1 minute ago
        )
        fakeSessionRepository.saveSession(expiredSession)

        val result = restoreSessionUseCase(now)

        assertTrue(result is Outcome.Failure)
        val error = (result as Outcome.Failure).error
        assertTrue(error is AppError.SessionExpired)
        assertNull(fakeSessionRepository.currentSession())
    }

    @Test
    fun `restore session clears session when server returns explicit 401 SessionExpired`() = runTest {
        val validSession = AuthSession(
            userId = UserId("user-1"),
            tenantId = TenantId("tenant-1"),
            roles = listOf(Role.OPERATOR),
            accessToken = AccessToken("invalid-token"),
            expiresAt = now.plusSeconds(1800)
        )
        fakeSessionRepository.saveSession(validSession)
        fakeAuthRepository.fetchProfileResult = Outcome.Failure(AppError.SessionExpired)

        val result = restoreSessionUseCase(now)

        assertTrue(result is Outcome.Failure)
        val error = (result as Outcome.Failure).error
        assertTrue(error is AppError.SessionExpired)
        assertNull(fakeSessionRepository.currentSession())
    }
}
