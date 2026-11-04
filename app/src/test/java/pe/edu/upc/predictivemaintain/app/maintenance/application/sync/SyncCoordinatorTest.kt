package pe.edu.upc.predictivemaintain.app.maintenance.application.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.AccessToken
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.TenantId
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId
import pe.edu.upc.predictivemaintain.app.iam.fakes.FakeSessionRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.PendingOperation
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncItemResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncOperationType
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncOutcome
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeConnectivityObserver
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakePendingOperationQueue
import pe.edu.upc.predictivemaintain.app.maintenance.fakes.FakeSyncRepository
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SyncCoordinatorTest {

    private val connectivity = FakeConnectivityObserver(initial = false)
    private val queue = FakePendingOperationQueue()
    private val syncRepo = FakeSyncRepository()
    private val sessions = FakeSessionRepository().apply {
        setSessionSync(
            AuthSession(UserId("tech-1"), TenantId("t"), listOf(Role.TECHNICIAN), AccessToken("x"), Instant.now().plusSeconds(3600))
        )
    }

    private fun op(id: String, user: String = "tech-1") = PendingOperation(
        operationId = id, workOrderId = "wo-$id", userId = user,
        type = SyncOperationType.START, performedAt = Instant.now(), summary = null
    )

    private fun TestScopeHolder.coordinator() = SyncCoordinator(
        connectivity, queue, SyncPendingOperationsUseCase(queue, syncRepo, sessions), CoroutineScope(dispatcher)
    )

    private class TestScopeHolder(val dispatcher: kotlinx.coroutines.test.TestDispatcher)

    @Test
    fun `offline does not sync and reflects state`() = runTest {
        val holder = TestScopeHolder(StandardTestDispatcher(testScheduler))
        queue.enqueue(op("1"))
        val coordinator = holder.coordinator()
        coordinator.start()
        advanceUntilIdle()

        assertFalse(coordinator.state.value.isOnline)
        assertEquals(1, coordinator.state.value.pendingCount)
        assertEquals(0, syncRepo.calls)
    }

    @Test
    fun `reconnecting syncs pending operations and reports the result`() = runTest {
        val holder = TestScopeHolder(StandardTestDispatcher(testScheduler))
        queue.enqueue(op("1"))
        queue.enqueue(op("2"))
        val coordinator = holder.coordinator()
        val events = mutableListOf<SyncEvent>()
        coordinator.start()
        advanceUntilIdle()

        val collector = backgroundScope.let { scope ->
            scope.launch(UnconfinedTestDispatcher(testScheduler)) { coordinator.events.collect { events += it } }
        }
        advanceUntilIdle()
        connectivity.online.value = true
        advanceUntilIdle()

        assertEquals(1, syncRepo.calls)
        assertEquals(0, queue.list().size)
        assertTrue(coordinator.state.value.isOnline)
        val completed = events.single() as SyncEvent.Completed
        assertEquals(2, completed.report.appliedCount)
        collector.cancel()
    }

    @Test
    fun `superseded and rejected are reported and removed from the queue`() = runTest {
        val holder = TestScopeHolder(StandardTestDispatcher(testScheduler))
        queue.enqueue(op("1"))
        syncRepo.result = Outcome.Success(
            listOf(SyncItemResult("1", "wo-1", SyncOutcome.SUPERSEDED, "Server wins", "COMPLETED", 5L))
        )
        connectivity.online.value = true
        val coordinator = holder.coordinator()
        val events = mutableListOf<SyncEvent>()
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { coordinator.events.collect { events += it } }
        advanceUntilIdle()
        coordinator.start()
        advanceUntilIdle()

        val report = (events.single() as SyncEvent.Completed).report
        assertEquals(0, report.appliedCount)
        assertEquals("Server wins", report.issues.single().message)
        assertEquals(0, queue.list().size)
        collector.cancel()
    }

    @Test
    fun `network failure keeps operations queued and emits nothing`() = runTest {
        val holder = TestScopeHolder(StandardTestDispatcher(testScheduler))
        queue.enqueue(op("1"))
        syncRepo.result = Outcome.Failure(AppError.Network())
        connectivity.online.value = true
        val coordinator = holder.coordinator()
        val events = mutableListOf<SyncEvent>()
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { coordinator.events.collect { events += it } }
        advanceUntilIdle()
        coordinator.start()
        advanceUntilIdle()

        assertEquals(1, queue.list().size)
        assertTrue(events.isEmpty())
        assertFalse(coordinator.state.value.isSyncing)
        collector.cancel()
    }

    @Test
    fun `only the current user operations are sent`() = runTest {
        val holder = TestScopeHolder(StandardTestDispatcher(testScheduler))
        queue.enqueue(op("1", user = "other"))
        queue.enqueue(op("2"))
        connectivity.online.value = true
        val coordinator = holder.coordinator()
        coordinator.start()
        advanceUntilIdle()

        assertEquals(listOf("2"), syncRepo.lastSent.map { it.operationId })
        assertEquals(listOf("1"), queue.list().map { it.operationId })
    }
}
