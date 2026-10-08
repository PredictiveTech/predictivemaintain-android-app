package pe.edu.upc.predictivemaintain.app.iam.application.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

class GetProfileUseCaseTest {

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeSessionRepository: FakeSessionRepository
    private lateinit var getProfileUseCase: GetProfileUseCase

    @Before
    fun setUp() {
        fakeAuthRepository = FakeAuthRepository()
        fakeSessionRepository = FakeSessionRepository()
        getProfileUseCase = GetProfileUseCase(fakeAuthRepository, fakeSessionRepository)
    }

    @Test
    fun `network failure with a saved profile returns it (offline success)`() = runTest {
        fakeAuthRepository.fetchProfileResult = Outcome.Failure(AppError.Network())
        fakeSessionRepository.saveUserProfile(DisplayName("Saved Name"), Email("saved@plant.com"))
        fakeSessionRepository.saveSession(
            AuthSession(
                userId = UserId("u-1"),
                tenantId = TenantId("t-1"),
                roles = listOf(Role.TECHNICIAN),
                accessToken = AccessToken("token"),
                expiresAt = Instant.now().plusSeconds(3600)
            )
        )

        val result = getProfileUseCase()

        assertTrue(result is Outcome.Success)
        val profileResult = (result as Outcome.Success).data
        assertTrue(profileResult.isOffline)
        assertEquals("Saved Name", profileResult.profile.displayName.value)
        assertEquals("saved@plant.com", profileResult.profile.email.value)
    }

    @Test
    fun `network failure without saved profile returns Failure Network`() = runTest {
        fakeAuthRepository.fetchProfileResult = Outcome.Failure(AppError.Network())
        // No saved profile or session

        val result = getProfileUseCase()

        assertTrue(result is Outcome.Failure)
        val error = (result as Outcome.Failure).error
        assertTrue(error is AppError.Network)
    }

    @Test
    fun `successful request updates the saved profile`() = runTest {
        val freshProfile = UserProfile(
            id = UserId("u-1"),
            tenantId = TenantId("t-1"),
            email = Email("fresh@plant.com"),
            displayName = DisplayName("Fresh Name"),
            active = true,
            roles = listOf(Role.MAINTENANCE_MANAGER)
        )
        fakeAuthRepository.fetchProfileResult = Outcome.Success(freshProfile)

        val result = getProfileUseCase()

        assertTrue(result is Outcome.Success)
        val profileResult = (result as Outcome.Success).data
        assertFalse(profileResult.isOffline)
        assertEquals("Fresh Name", profileResult.profile.displayName.value)
        assertEquals("fresh@plant.com", profileResult.profile.email.value)

        val saved = fakeSessionRepository.getSavedUserProfile()
        assertNotNull(saved)
        assertEquals("Fresh Name", saved?.first?.value)
        assertEquals("fresh@plant.com", saved?.second?.value)
    }
}
