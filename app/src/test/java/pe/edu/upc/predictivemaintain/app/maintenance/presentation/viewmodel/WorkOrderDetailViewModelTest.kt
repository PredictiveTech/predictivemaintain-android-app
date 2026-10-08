package pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.ListTechniciansUseCase
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.AccessToken
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.TenantId
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeSessionRepository
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeUserDirectoryRepository
import pe.edu.upc.predictivemaintain.app.maintenance.application.policy.WorkOrderActionPolicy
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.AssignWorkOrderUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.CompleteWorkOrderUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.GetWorkOrderUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.StartWorkOrderUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.TechnicianId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeWorkOrderRepository
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class WorkOrderDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeWorkOrderRepository: FakeWorkOrderRepository
    private lateinit var fakeUserDirectoryRepository: FakeUserDirectoryRepository
    private lateinit var fakeSessionRepository: FakeSessionRepository
    private lateinit var viewModel: WorkOrderDetailViewModel

    private val sampleOrder = WorkOrder(
        id = WorkOrderId("wo-1"),
        alertId = AlertId("alert-1"),
        assetId = AssetId("asset-1"),
        assetCode = "AST-01",
        assetName = "Pump 1",
        severity = AlertSeverity.WARNING,
        assignedUserId = null,
        status = WorkOrderStatus.OPEN,
        summary = null,
        openedAt = Instant.now(),
        completedAt = null,
        version = 1L
    )

    private val managerSession = AuthSession(
        userId = UserId("mgr-1"),
        tenantId = TenantId("tenant-1"),
        roles = listOf(Role.MAINTENANCE_MANAGER),
        accessToken = AccessToken("token"),
        expiresAt = Instant.now().plusSeconds(3600)
    )

    private val technicianSession = AuthSession(
        userId = UserId("tech-1"),
        tenantId = TenantId("tenant-1"),
        roles = listOf(Role.TECHNICIAN),
        accessToken = AccessToken("token"),
        expiresAt = Instant.now().plusSeconds(3600)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeWorkOrderRepository = FakeWorkOrderRepository()
        fakeUserDirectoryRepository = FakeUserDirectoryRepository()
        fakeSessionRepository = FakeSessionRepository()

        val getWorkOrderUseCase = GetWorkOrderUseCase(fakeWorkOrderRepository)
        val assignWorkOrderUseCase = AssignWorkOrderUseCase(fakeWorkOrderRepository)
        val startWorkOrderUseCase = StartWorkOrderUseCase(fakeWorkOrderRepository)
        val completeWorkOrderUseCase = CompleteWorkOrderUseCase(fakeWorkOrderRepository)
        val listTechniciansUseCase = ListTechniciansUseCase(fakeUserDirectoryRepository)
        val workOrderActionPolicy = WorkOrderActionPolicy()

        viewModel = WorkOrderDetailViewModel(
            getWorkOrderUseCase = getWorkOrderUseCase,
            assignWorkOrderUseCase = assignWorkOrderUseCase,
            startWorkOrderUseCase = startWorkOrderUseCase,
            completeWorkOrderUseCase = completeWorkOrderUseCase,
            listTechniciansUseCase = listTechniciansUseCase,
            workOrderActionPolicy = workOrderActionPolicy,
            sessionRepository = fakeSessionRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `assign updates state when successful`() = runTest {
        fakeSessionRepository.saveSession(managerSession)
        fakeWorkOrderRepository.getWorkOrderResult = Outcome.Success(sampleOrder)
        viewModel.loadOrder("wo-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val assignedOrder = sampleOrder.copy(
            assignedUserId = TechnicianId("tech-1"),
            status = WorkOrderStatus.ASSIGNED,
            version = 2L
        )
        fakeWorkOrderRepository.assignWorkOrderResult = Outcome.Success(assignedOrder)

        viewModel.assignTechnician("tech-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("tech-1", state.order?.assignedUserId?.value)
        assertEquals(WorkOrderStatus.ASSIGNED, state.order?.status)
        assertNotNull(state.successMessage)
    }

    @Test
    fun `assign 409 conflict displays error and reloads order`() = runTest {
        fakeSessionRepository.saveSession(managerSession)
        fakeWorkOrderRepository.getWorkOrderResult = Outcome.Success(sampleOrder)
        viewModel.loadOrder("wo-1")
        testDispatcher.scheduler.advanceUntilIdle()

        fakeWorkOrderRepository.assignWorkOrderResult = Outcome.Failure(
            AppError.Api(httpStatus = 409, code = "VERSION_MISMATCH", detail = "Version conflict")
        )

        val reloadedOrder = sampleOrder.copy(version = 2L)
        fakeWorkOrderRepository.getWorkOrderResult = Outcome.Success(reloadedOrder)

        viewModel.assignTechnician("tech-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Version conflict", state.errorMessage)
        assertEquals(2L, state.order?.version)
    }

    @Test
    fun `start moves order to IN_PROGRESS`() = runTest {
        fakeSessionRepository.saveSession(technicianSession)
        val assignedOrder = sampleOrder.copy(
            assignedUserId = TechnicianId("tech-1"),
            status = WorkOrderStatus.ASSIGNED,
            version = 2L
        )
        fakeWorkOrderRepository.getWorkOrderResult = Outcome.Success(assignedOrder)
        viewModel.loadOrder("wo-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val inProgressOrder = assignedOrder.copy(status = WorkOrderStatus.IN_PROGRESS, version = 3L)
        fakeWorkOrderRepository.startWorkOrderResult = Outcome.Success(inProgressOrder)

        viewModel.startWorkOrder()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(WorkOrderStatus.IN_PROGRESS, state.order?.status)
        assertNotNull(state.successMessage)
    }

    @Test
    fun `complete shows summary and moves order to COMPLETED`() = runTest {
        fakeSessionRepository.saveSession(technicianSession)
        val inProgressOrder = sampleOrder.copy(
            assignedUserId = TechnicianId("tech-1"),
            status = WorkOrderStatus.IN_PROGRESS,
            version = 3L
        )
        fakeWorkOrderRepository.getWorkOrderResult = Outcome.Success(inProgressOrder)
        viewModel.loadOrder("wo-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val completedOrder = inProgressOrder.copy(
            status = WorkOrderStatus.COMPLETED,
            summary = "Replaced faulty seal",
            completedAt = Instant.now(),
            version = 4L
        )
        fakeWorkOrderRepository.completeWorkOrderResult = Outcome.Success(completedOrder)

        viewModel.completeWorkOrder("Replaced faulty seal")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(WorkOrderStatus.COMPLETED, state.order?.status)
        assertEquals("Replaced faulty seal", state.order?.summary)
        assertNotNull(state.successMessage)
    }

    @Test
    fun `403 error displays server detail`() = runTest {
        fakeSessionRepository.saveSession(technicianSession)
        val assignedOrder = sampleOrder.copy(
            assignedUserId = TechnicianId("tech-1"),
            status = WorkOrderStatus.ASSIGNED,
            version = 2L
        )
        fakeWorkOrderRepository.getWorkOrderResult = Outcome.Success(assignedOrder)
        viewModel.loadOrder("wo-1")
        testDispatcher.scheduler.advanceUntilIdle()

        fakeWorkOrderRepository.startWorkOrderResult = Outcome.Failure(
            AppError.Api(httpStatus = 403, code = "NOT_ASSIGNED_TECHNICIAN", detail = "Not assigned technician")
        )

        viewModel.startWorkOrder()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Not assigned technician", state.errorMessage)
    }
}
