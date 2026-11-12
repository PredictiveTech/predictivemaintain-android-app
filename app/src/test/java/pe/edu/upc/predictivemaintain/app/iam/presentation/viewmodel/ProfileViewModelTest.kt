package pe.edu.upc.predictivemaintain.app.iam.presentation.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.GetProfileUseCase
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.LogoutUseCase
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.UpdateMyProfileUseCase
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.AccessToken
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.DisplayName
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Email
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.TenantId
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeAuthRepository
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeSessionRepository
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var sessionRepository: FakeSessionRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        sessionRepository = FakeSessionRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ProfileViewModel(
        GetProfileUseCase(authRepository, sessionRepository),
        UpdateMyProfileUseCase(authRepository, sessionRepository),
        LogoutUseCase(sessionRepository)
    )

    @Test
    fun `starts loading and then shows the profile`() = runTest {
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.isLoading)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("operator@plant.com", state.email)
        assertEquals("Test User", state.displayName)
        assertEquals(listOf(Role.MAINTENANCE_MANAGER), state.roles)
        assertNull(state.loadError)
        assertFalse(state.canSave)
    }

    @Test
    fun `load failure without cache exposes loadError and retry recovers`() = runTest {
        authRepository.fetchProfileResult = Outcome.Failure(AppError.Network())
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadError is AppError.Network)
        assertFalse(viewModel.uiState.value.isLoading)

        authRepository.fetchProfileResult = null
        viewModel.loadProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.loadError)
        assertEquals("Test User", viewModel.uiState.value.displayName)
    }

    @Test
    fun `offline fallback shows saved data and blocks saving`() = runTest {
        authRepository.fetchProfileResult = Outcome.Failure(AppError.Network())
        sessionRepository.saveUserProfile(DisplayName("Saved"), Email("saved@plant.com"))
        sessionRepository.saveSession(
            AuthSession(UserId("u"), TenantId("t"), listOf(Role.TECHNICIAN), AccessToken("tok"), Instant.now().plusSeconds(60))
        )
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onDisplayNameChange("Other")
        val state = viewModel.uiState.value
        assertTrue(state.isOffline)
        assertEquals("saved@plant.com", state.email)
        assertFalse(state.canSave)
    }

    @Test
    fun `editing enables save only for a changed non blank name`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onDisplayNameChange("New Name")
        assertTrue(viewModel.uiState.value.canSave)

        viewModel.onDisplayNameChange("   ")
        assertTrue(viewModel.uiState.value.isDisplayNameBlank)
        assertFalse(viewModel.uiState.value.canSave)

        viewModel.onDisplayNameChange("Test User")
        assertFalse(viewModel.uiState.value.canSave)
    }

    @Test
    fun `save sends the trimmed name, updates state and session cache`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onDisplayNameChange("  New Name  ")
        viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("New Name", authRepository.lastUpdatedDisplayName?.value)
        assertFalse(state.isSaving)
        assertTrue(state.saveSucceeded)
        assertNull(state.saveError)
        assertEquals("New Name", state.displayName)
        assertEquals("New Name", state.savedDisplayName)
        assertFalse(state.canSave)
        assertEquals("New Name", sessionRepository.getSavedUserProfile()?.first?.value)
    }

    @Test
    fun `save failure keeps the typed name and exposes saveError`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        authRepository.updateProfileResult = Outcome.Failure(AppError.Api(400, "VALIDATION", "Too long"))

        viewModel.onDisplayNameChange("New Name")
        viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertFalse(state.saveSucceeded)
        assertEquals("Too long", (state.saveError as AppError.Api).detail)
        assertEquals("New Name", state.displayName)
        assertEquals("Test User", state.savedDisplayName)
        assertTrue(state.canSave)
    }

    @Test
    fun `save is ignored when nothing changed or name is blank`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.save()
        viewModel.onDisplayNameChange(" ")
        viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, authRepository.updateProfileCalls)
    }

    @Test
    fun `editing after a successful save clears the success flag`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onDisplayNameChange("A")
        viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.saveSucceeded)

        viewModel.onDisplayNameChange("AB")

        assertFalse(viewModel.uiState.value.saveSucceeded)
    }

    @Test
    fun `logout clears the session and notifies the caller`() = runTest {
        sessionRepository.saveSession(
            AuthSession(UserId("u"), TenantId("t"), listOf(Role.TECHNICIAN), AccessToken("tok"), Instant.now().plusSeconds(60))
        )
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        var loggedOut = false

        viewModel.logout { loggedOut = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(loggedOut)
        assertNull(sessionRepository.currentSession())
    }
}
