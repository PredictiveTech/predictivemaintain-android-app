package pe.edu.upc.predictivemaintain.app.maintenance.application.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.TechnicianId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeWorkOrderRepository
import java.time.Instant

class WorkOrderUseCasesTest {

    private lateinit var fakeRepository: FakeWorkOrderRepository

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
        fakeRepository = FakeWorkOrderRepository()
    }

    @Test
    fun `ListWorkOrdersUseCase invokes repository listWorkOrders`() = runTest {
        val useCase = ListWorkOrdersUseCase(fakeRepository)
        fakeRepository.listWorkOrdersResult = Outcome.Success(
            PageResult(items = listOf(sampleOrder), page = 0, totalPages = 1, totalElements = 1)
        )

        val result = useCase(status = WorkOrderStatus.OPEN, page = 0)

        assertTrue(result is Outcome.Success)
        assertEquals(1, (result as Outcome.Success).data.items.size)
        assertEquals(WorkOrderStatus.OPEN, fakeRepository.lastListStatusFilter)
    }

    @Test
    fun `GetWorkOrderUseCase invokes repository getWorkOrder`() = runTest {
        val useCase = GetWorkOrderUseCase(fakeRepository)
        fakeRepository.getWorkOrderResult = Outcome.Success(sampleOrder)

        val result = useCase(WorkOrderId("wo-1"))

        assertTrue(result is Outcome.Success)
        assertEquals("wo-1", (result as Outcome.Success).data.id.value)
    }

    @Test
    fun `CreateWorkOrderUseCase invokes repository createWorkOrder`() = runTest {
        val useCase = CreateWorkOrderUseCase(fakeRepository)
        fakeRepository.createWorkOrderResult = Outcome.Success(sampleOrder)

        val result = useCase(AlertId("alert-1"))

        assertTrue(result is Outcome.Success)
        assertEquals("wo-1", (result as Outcome.Success).data.id.value)
    }

    @Test
    fun `AssignWorkOrderUseCase invokes repository assignWorkOrder`() = runTest {
        val useCase = AssignWorkOrderUseCase(fakeRepository)
        val assigned = sampleOrder.copy(assignedUserId = TechnicianId("tech-1"), status = WorkOrderStatus.ASSIGNED)
        fakeRepository.assignWorkOrderResult = Outcome.Success(assigned)

        val result = useCase(WorkOrderId("wo-1"), TechnicianId("tech-1"), expectedVersion = 1L)

        assertTrue(result is Outcome.Success)
        assertEquals("tech-1", (result as Outcome.Success).data.assignedUserId?.value)
        assertEquals(WorkOrderStatus.ASSIGNED, result.data.status)
    }
}
