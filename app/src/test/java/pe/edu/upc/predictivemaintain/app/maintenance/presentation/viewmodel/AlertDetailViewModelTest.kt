package pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.ObserveSessionUseCase
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.AccessToken
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.TenantId
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeSessionRepository
import pe.edu.upc.predictivemaintain.app.maintenance.application.policy.AlertActionPolicy
import pe.edu.upc.predictivemaintain.app.maintenance.application.policy.WorkOrderActionPolicy
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.CreateWorkOrderUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.GetAlertUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.ReviewAlertUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Alert
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertStatus
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeAlertRepository
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeWorkOrderRepository
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class AlertDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeAlertRepository
    private lateinit var fakeWorkOrderRepository: FakeWorkOrderRepository
    private lateinit var fakeSessionRepository: FakeSessionRepository
    private lateinit var getAlertUseCase: GetAlertUseCase
    private lateinit var reviewAlertUseCase: ReviewAlertUseCase
    private lateinit var createWorkOrderUseCase: CreateWorkOrderUseCase
    private lateinit var alertActionPolicy: AlertActionPolicy
    private lateinit var workOrderActionPolicy: WorkOrderActionPolicy
    private lateinit var observeSessionUseCase: ObserveSessionUseCase
    private lateinit var viewModel: AlertDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAlertRepository()
        fakeWorkOrderRepository = FakeWorkOrderRepository()
        fakeSessionRepository = FakeSessionRepository()
        getAlertUseCase = GetAlertUseCase(fakeRepository)
        reviewAlertUseCase = ReviewAlertUseCase(fakeRepository)
        createWorkOrderUseCase = CreateWorkOrderUseCase(fakeWorkOrderRepository)
        alertActionPolicy = AlertActionPolicy()
        workOrderActionPolicy = WorkOrderActionPolicy()
        observeSessionUseCase = ObserveSessionUseCase(fakeSessionRepository)

        viewModel = AlertDetailViewModel(
            getAlertUseCase = getAlertUseCase,
            reviewAlertUseCase = reviewAlertUseCase,
            createWorkOrderUseCase = createWorkOrderUseCase,
            sessionRepository = fakeSessionRepository,
            observeSessionUseCase = observeSessionUseCase,
            alertActionPolicy = alertActionPolicy,
            workOrderActionPolicy = workOrderActionPolicy
        )
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

    @Test
    fun `create work order success emits navigation event`() = runTest {
        val confirmedAlert = Alert(
            id = AlertId("a-1"),
            assetId = "ast-1",
            assetCode = "C-1",
            assetName = "Compressor",
            severity = AlertSeverity.CRITICAL,
            status = AlertStatus.CONFIRMED,
            raisedAt = Instant.now(),
            discardReason = null,
            version = 1L,
            diagnostic = null
        )
        fakeRepository.getAlertResult = Outcome.Success(confirmedAlert)

        val createdOrder = WorkOrder(
            id = WorkOrderId("wo-100"),
            alertId = AlertId("a-1"),
            assetId = AssetId("ast-1"),
            assetCode = "C-1",
            assetName = "Compressor",
            severity = AlertSeverity.CRITICAL,
            assignedUserId = null,
            status = WorkOrderStatus.OPEN,
            summary = null,
            openedAt = Instant.now(),
            completedAt = null,
            version = 1L
        )
        fakeWorkOrderRepository.createWorkOrderResult = Outcome.Success(createdOrder)

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

        assertTrue(viewModel.uiState.value.canCreateWorkOrder)

        viewModel.createWorkOrder()
        testDispatcher.scheduler.advanceUntilIdle()

        val event = viewModel.uiEvent.first()
        assertTrue(event is AlertDetailUiEvent.NavigateToWorkOrder)
        assertEquals("wo-100", (event as AlertDetailUiEvent.NavigateToWorkOrder).workOrderId)
    }

    @Test
    fun `create work order 409 error shows detail and stays on screen`() = runTest {
        val confirmedAlert = Alert(
            id = AlertId("a-1"),
            assetId = "ast-1",
            assetCode = "C-1",
            assetName = "Compressor",
            severity = AlertSeverity.CRITICAL,
            status = AlertStatus.CONFIRMED,
            raisedAt = Instant.now(),
            discardReason = null,
            version = 1L,
            diagnostic = null
        )
        fakeRepository.getAlertResult = Outcome.Success(confirmedAlert)

        fakeWorkOrderRepository.createWorkOrderResult = Outcome.Failure(
            AppError.Api(409, "WORK_ORDER_ALREADY_EXISTS", "A work order already exists for this alert")
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

        viewModel.createWorkOrder()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("A work order already exists for this alert", state.errorMessage)
    }
}
