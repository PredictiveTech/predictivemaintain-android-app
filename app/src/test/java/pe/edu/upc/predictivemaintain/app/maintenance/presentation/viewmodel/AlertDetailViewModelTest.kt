package pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeSessionRepository
import pe.edu.upc.predictivemaintain.app.maintenance.application.policy.AlertActionPolicy
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.GetAlertUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.ReviewAlertUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Alert
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertStatus
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeAlertRepository
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class AlertDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeAlertRepository
    private lateinit var fakeSessionRepository: FakeSessionRepository
    private lateinit var getAlertUseCase: GetAlertUseCase
    private lateinit var reviewAlertUseCase: ReviewAlertUseCase
    private lateinit var alertActionPolicy: AlertActionPolicy
    private lateinit var viewModel: AlertDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAlertRepository()
        fakeSessionRepository = FakeSessionRepository()
        getAlertUseCase = GetAlertUseCase(fakeRepository)
        reviewAlertUseCase = ReviewAlertUseCase(fakeRepository)
        alertActionPolicy = AlertActionPolicy()
        viewModel = AlertDetailViewModel(getAlertUseCase, reviewAlertUseCase, fakeSessionRepository, alertActionPolicy)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful review updates alert state`() = runTest {
        val alert = Alert(
            id = AlertId("a-1"),
            assetId = "ast-1",
            assetCode = "C-1",
            assetName = "Compressor",
            severity = AlertSeverity.CRITICAL,
            status = AlertStatus.IN_REVIEW,
            raisedAt = Instant.now(),
            discardReason = null,
            version = 1L,
            diagnostic = null
        )
        val updatedAlert = alert.copy(status = AlertStatus.CONFIRMED, version = 2L)

        fakeRepository.getAlertResult = Outcome.Success(alert)
        fakeRepository.reviewAlertResult = Outcome.Success(updatedAlert)

        fakeSessionRepository.saveSession(
            AuthSession(
                userId = UserId("u-1"),
                tenantId = TenantId("t-1"),
                roles = listOf(Role.MAINTENANCE_MANAGER),
                accessToken = AccessToken("token"),
                expiresAt = Instant.now().plusSeconds(3600)
            )
        )

        viewModel.loadAlert("a-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.confirmAlert()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AlertStatus.CONFIRMED, state.alert?.status)
        assertEquals(2L, state.alert?.version)
        assertTrue(state.successMessage != null)
    }

    @Test
    fun `409 conflict during review triggers reload and sets error detail`() = runTest {
        val alert = Alert(
            id = AlertId("a-1"),
            assetId = "ast-1",
            assetCode = "C-1",
            assetName = "Compressor",
            severity = AlertSeverity.CRITICAL,
            status = AlertStatus.IN_REVIEW,
            raisedAt = Instant.now(),
            discardReason = null,
            version = 1L,
            diagnostic = null
        )

        fakeRepository.getAlertResult = Outcome.Success(alert)
        fakeRepository.reviewAlertResult = Outcome.Failure(
            AppError.Api(409, "CONFLICT", "Alert state was modified by another user.")
        )

        fakeSessionRepository.saveSession(
            AuthSession(
                userId = UserId("u-1"),
                tenantId = TenantId("t-1"),
                roles = listOf(Role.MAINTENANCE_MANAGER),
                accessToken = AccessToken("token"),
                expiresAt = Instant.now().plusSeconds(3600)
            )
        )

        viewModel.loadAlert("a-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.confirmAlert()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Alert state was modified by another user.", state.errorMessage)
    }
}
