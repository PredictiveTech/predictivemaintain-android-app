package pe.edu.upc.predictivemaintain.app.iam.application.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.DisplayName
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeAuthRepository
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeSessionRepository

class UpdateMyProfileUseCaseTest {

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var sessionRepository: FakeSessionRepository
    private lateinit var useCase: UpdateMyProfileUseCase

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
        sessionRepository = FakeSessionRepository()
        useCase = UpdateMyProfileUseCase(authRepository, sessionRepository)
    }

    @Test
    fun `success returns the updated profile and refreshes the saved one`() = runTest {
        val result = useCase(DisplayName("Ana"))

        assertTrue(result is Outcome.Success)
        assertEquals("Ana", (result as Outcome.Success).data.displayName.value)
        assertEquals("Ana", authRepository.lastUpdatedDisplayName?.value)
        val saved = sessionRepository.getSavedUserProfile()
        assertEquals("Ana", saved?.first?.value)
        assertEquals("operator@plant.com", saved?.second?.value)
    }

    @Test
    fun `failure is propagated and the saved profile is untouched`() = runTest {
        authRepository.updateProfileResult = Outcome.Failure(AppError.Network())

        val result = useCase(DisplayName("Ana"))

        assertTrue(result is Outcome.Failure)
        assertNull(sessionRepository.getSavedUserProfile())
    }
}
