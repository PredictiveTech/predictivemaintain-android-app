package pe.edu.upc.predictivemaintain.app.iam.application.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.AccessToken
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.TenantId
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeAuthRepository
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeSessionRepository
import java.time.Instant

class LoginUseCaseTest {

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeSessionRepository: FakeSessionRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setUp() {
        fakeAuthRepository = FakeAuthRepository()
        fakeSessionRepository = FakeSessionRepository()
        loginUseCase = LoginUseCase(fakeAuthRepository, fakeSessionRepository)
    }

    @Test
    fun `successful login saves session to session repository`() = runTest {
        val expectedSession = AuthSession(
            userId = UserId("user-1"),
            tenantId = TenantId("tenant-1"),
            roles = listOf(Role.TECHNICIAN),
            accessToken = AccessToken("valid-token"),
            expiresAt = Instant.now().plusSeconds(3600)
        )
        fakeAuthRepository.loginResult = Outcome.Success(expectedSession)

        val result = loginUseCase("tech@plant.com", "secret123")

        assertTrue(result is Outcome.Success)
        val savedSession = fakeSessionRepository.currentSession()
        assertEquals(expectedSession, savedSession)
    }

    @Test
    fun `wrong credentials return Failure and save nothing`() = runTest {
        fakeAuthRepository.loginResult = Outcome.Failure(
            AppError.Api(401, "INVALID_CREDENTIALS", "Invalid username or password")
        )

        val result = loginUseCase("user@plant.com", "wrongpass")

        assertTrue(result is Outcome.Failure)
        val savedSession = fakeSessionRepository.currentSession()
        assertNull(savedSession)
    }

    @Test
    fun `blank inputs return validation error and do not invoke auth repository`() = runTest {
        val result = loginUseCase("", "")

        assertTrue(result is Outcome.Failure)
        val error = (result as Outcome.Failure).error
        assertTrue(error is AppError.Api)
        val apiError = error as AppError.Api
        assertEquals("VALIDATION_ERROR", apiError.code)
        assertEquals(2, apiError.fieldErrors.size)
        assertNull(fakeSessionRepository.currentSession())
    }
}
