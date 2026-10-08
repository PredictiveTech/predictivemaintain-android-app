package pe.edu.upc.predictivemaintain.app.maintenance.presentation.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.ListTechniciansUseCase
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.Technician
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.AccessToken
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.DisplayName
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Email
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.TenantId
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeSessionRepository
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeUserDirectoryRepository
import pe.edu.upc.predictivemaintain.app.maintenance.application.usecase.ListWorkOrdersUseCase
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeWorkOrderRepository
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class WorkOrdersViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeWorkOrderRepository: FakeWorkOrderRepository
    private lateinit var fakeUserDirectoryRepository: FakeUserDirectoryRepository
    private lateinit var fakeSessionRepository: FakeSessionRepository
    private lateinit var viewModel: WorkOrdersViewModel

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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeWorkOrderRepository = FakeWorkOrderRepository()
        fakeUserDirectoryRepository = FakeUserDirectoryRepository()
        fakeSessionRepository = FakeSessionRepository()

        val listWorkOrdersUseCase = ListWorkOrdersUseCase(fakeWorkOrderRepository)
        val listTechniciansUseCase = ListTechniciansUseCase(fakeUserDirectoryRepository)

        viewModel = WorkOrdersViewModel(
            listWorkOrdersUseCase = listWorkOrdersUseCase,
            listTechniciansUseCase = listTechniciansUseCase,
            sessionRepository = fakeSessionRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads manager session technician names and success work orders`() = runTest {
        val managerSession = AuthSession(
            userId = UserId("mgr-1"),
            tenantId = TenantId("tenant-1"),
            roles = listOf(Role.MAINTENANCE_MANAGER),
            accessToken = AccessToken("token"),
            expiresAt = Instant.now().plusSeconds(3600)
        )
        fakeSessionRepository.saveSession(managerSession)

        fakeUserDirectoryRepository.listTechniciansResult = Outcome.Success(
            listOf(Technician(id = UserId("tech-1"), displayName = DisplayName("Tech One"), email = Email("tech1@plant.com")))
        )
        fakeWorkOrderRepository.listWorkOrdersResult = Outcome.Success(
            PageResult(items = listOf(sampleOrder), page = 0, totalPages = 1, totalElements = 1)
        )

        viewModel.init()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isManager)
        assertFalse(state.isErrorState)
        assertEquals(1, state.orders.size)
        assertEquals("Tech One", state.technicianNames["tech-1"])
    }

    @Test
    fun `technician names are NOT requested for a technician`() = runTest {
        val technicianSession = AuthSession(
            userId = UserId("tech-1"),
            tenantId = TenantId("tenant-1"),
            roles = listOf(Role.TECHNICIAN),
            accessToken = AccessToken("token"),
            expiresAt = Instant.now().plusSeconds(3600)
        )
        fakeSessionRepository.saveSession(technicianSession)

        fakeWorkOrderRepository.listWorkOrdersResult = Outcome.Success(
            PageResult(items = listOf(sampleOrder), page = 0, totalPages = 1, totalElements = 1)
        )

        viewModel.init()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isTechnician)
        assertFalse(state.isManager)
        assertTrue(state.technicianNames.isEmpty())
    }

    @Test
    fun `failed initial request with no items results in error state`() = runTest {
        fakeWorkOrderRepository.listWorkOrdersResult = Outcome.Failure(AppError.Network())

        viewModel.init()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isErrorState)
        assertTrue(state.orders.isEmpty())
    }

    @Test
    fun `a failed refresh keeps previous items`() = runTest {
        fakeWorkOrderRepository.listWorkOrdersResult = Outcome.Success(
            PageResult(items = listOf(sampleOrder), page = 0, totalPages = 1, totalElements = 1)
        )
        viewModel.init()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.orders.size)

        // Now refresh fails
        fakeWorkOrderRepository.listWorkOrdersResult = Outcome.Failure(AppError.Network())
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.orders.size)
        assertFalse(state.isErrorState)
        assertEquals("Network error. Please check your connection.", state.errorMessage)
    }

    @Test
    fun `changing filter restarts at page 0`() = runTest {
        fakeWorkOrderRepository.listWorkOrdersResult = Outcome.Success(
            PageResult(items = listOf(sampleOrder), page = 0, totalPages = 1, totalElements = 1)
        )
        viewModel.init()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filterByStatus(WorkOrderStatus.OPEN)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(WorkOrderStatus.OPEN, fakeWorkOrderRepository.lastListStatusFilter)
        assertEquals(0, fakeWorkOrderRepository.lastListPage)
    }
}
