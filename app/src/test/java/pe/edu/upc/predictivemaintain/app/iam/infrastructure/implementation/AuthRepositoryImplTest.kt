package pe.edu.upc.predictivemaintain.app.iam.infrastructure.implementation

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.network.ErrorParser
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Email
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Password
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeAuthApiService

class AuthRepositoryImplTest {

    private lateinit var fakeAuthApiService: FakeAuthApiService
    private lateinit var errorParser: ErrorParser
    private lateinit var authRepository: AuthRepositoryImpl

    @Before
    fun setUp() {
        fakeAuthApiService = FakeAuthApiService()
        errorParser = ErrorParser()
        authRepository = AuthRepositoryImpl(fakeAuthApiService, errorParser)
    }

    @Test
    fun `login converts request and returns domain AuthSession on success`() = runTest {
        val email = Email("user@plant.com")
        val password = Password("Secret123")

        val result = authRepository.login(email, password)

        assertTrue(result is Outcome.Success)
        val session = (result as Outcome.Success).data
        assertEquals("u-fake", session.userId.value)
        assertEquals("t-fake", session.tenantId.value)
        assertEquals("fake-api-token", session.accessToken.value)
        assertEquals(Role.TECHNICIAN, session.roles.first())

        assertEquals("user@plant.com", fakeAuthApiService.lastLoginRequest?.email)
        assertEquals("Secret123", fakeAuthApiService.lastLoginRequest?.password)
    }

    @Test
    fun `fetchProfile returns domain UserProfile on success`() = runTest {
        val result = authRepository.fetchProfile()

        assertTrue(result is Outcome.Success)
        val profile = (result as Outcome.Success).data
        assertEquals("u-fake", profile.id.value)
        assertEquals("t-fake", profile.tenantId.value)
        assertEquals("user@plant.com", profile.email.value)
        assertEquals("Fake User", profile.displayName.value)
        assertTrue(profile.active)
    }
}
