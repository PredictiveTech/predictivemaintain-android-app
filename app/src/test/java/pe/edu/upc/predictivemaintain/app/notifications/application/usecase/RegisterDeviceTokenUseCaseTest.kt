package pe.edu.upc.predictivemaintain.app.notifications.application.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.AccessToken
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.TenantId
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeSessionRepository
import pe.edu.upc.predictivemaintain.app.notifications.domain.repository.DeviceTokenRepository
import pe.edu.upc.predictivemaintain.app.notifications.domain.service.PushTokenProvider
import java.time.Instant

class RegisterDeviceTokenUseCaseTest {

    private class FakePushTokenProvider(var result: Outcome<String> = Outcome.Success("fcm-current")) : PushTokenProvider {
        var calls = 0
        override suspend fun currentToken(): Outcome<String> {
            calls++
            return result
        }
    }

    private class FakeDeviceTokenRepository(var result: Outcome<Unit> = Outcome.Success(Unit)) : DeviceTokenRepository {
        val registered = mutableListOf<String>()
        override suspend fun register(token: String): Outcome<Unit> {
            registered += token
            return result
        }
    }

    private val session = AuthSession(
        userId = UserId("u-1"),
        tenantId = TenantId("t-1"),
        roles = listOf(Role.TECHNICIAN),
        accessToken = AccessToken("token"),
        expiresAt = Instant.now().plusSeconds(3600)
    )

    private val sessions = FakeSessionRepository()
    private val provider = FakePushTokenProvider()
    private val repository = FakeDeviceTokenRepository()
    private val useCase = RegisterDeviceTokenUseCase(sessions, provider, repository)

    @Test
    fun `fails without session and sends nothing`() = runTest {
        val result = useCase()

        assertEquals(Outcome.Failure(AppError.SessionExpired), result)
        assertTrue(repository.registered.isEmpty())
        assertEquals(0, provider.calls)
    }

    @Test
    fun `without explicit token it asks FCM and sends the current one`() = runTest {
        sessions.saveSession(session)

        val result = useCase()

        assertTrue(result is Outcome.Success)
        assertEquals(listOf("fcm-current"), repository.registered)
    }

    @Test
    fun `rotated token is sent as is without asking FCM`() = runTest {
        sessions.saveSession(session)

        useCase("rotated")

        assertEquals(listOf("rotated"), repository.registered)
        assertEquals(0, provider.calls)
    }

    @Test
    fun `FCM failure is returned and nothing is sent`() = runTest {
        sessions.saveSession(session)
        provider.result = Outcome.Failure(AppError.Unknown(IllegalStateException("no firebase")))

        val result = useCase()

        assertTrue(result is Outcome.Failure)
        assertTrue(repository.registered.isEmpty())
    }

    @Test
    fun `backend failure is returned`() = runTest {
        sessions.saveSession(session)
        repository.result = Outcome.Failure(AppError.Api(httpStatus = 404, code = "NOT_FOUND", detail = "no route"))

        val result = useCase()

        assertEquals(404, ((result as Outcome.Failure).error as AppError.Api).httpStatus)
    }
}
