package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.PendingOperation
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncOperationType
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PendingOperationDaoTest {

    private lateinit var db: MaintenanceDatabase
    private lateinit var dao: PendingOperationDao
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MaintenanceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.pendingOperationDao()
    }

    @After
    fun tearDown() = db.close()

    private fun entity(id: String, order: String = "wo-1", user: String = "u1", summary: String? = null) =
        PendingOperationEntity(id, order, user, "START", "2026-01-01T10:00:00Z", summary)

    @Test
    fun insert_ignoresDuplicateOperationId() = runBlocking {
        dao.insert(entity("op-1", summary = "first"))
        dao.insert(entity("op-1", summary = "second"))

        val all = dao.getAll()
        assertEquals(1, all.size)
        assertEquals("first", all.single().summary)
    }

    @Test
    fun getAll_returnsOperationsInInsertionOrder() = runBlocking {
        listOf("c", "a", "b").forEach { dao.insert(entity(it)) }

        assertEquals(listOf("c", "a", "b"), dao.getAll().map { it.operationId })
    }

    @Test
    fun deleteByIds_removesOnlyTheGivenOperations() = runBlocking {
        listOf("op-1", "op-2", "op-3").forEach { dao.insert(entity(it)) }

        dao.deleteByIds(listOf("op-1", "op-3", "missing"))

        assertEquals(listOf("op-2"), dao.getAll().map { it.operationId })
    }

    @Test
    fun observeAll_emitsCurrentContent() = runBlocking {
        assertEquals(emptyList<PendingOperationEntity>(), dao.observeAll().first())

        dao.insert(entity("op-1"))

        assertEquals(listOf("op-1"), dao.observeAll().first().map { it.operationId })
    }

    @Test
    fun entityMapping_roundTripsDomainOperation() {
        val op = PendingOperation(
            operationId = "op-1",
            workOrderId = "wo-9",
            userId = "u7",
            type = SyncOperationType.COMPLETE,
            performedAt = Instant.parse("2026-03-04T05:06:07.123456789Z"),
            summary = "done"
        )

        assertEquals(op, op.toEntity().toDomainOrNull())
    }

    @Test
    fun toDomainOrNull_dropsUnreadableRows() {
        assertNull(entity("op-1").copy(type = "NOT_A_TYPE").toDomainOrNull())
        assertNull(entity("op-1").copy(performedAt = "garbage").toDomainOrNull())
    }

    @Test
    fun queue_removesInChunksAndKeepsTheRest() = runBlocking {
        val queue = RoomPendingOperationQueue(db, dao, LegacyPendingOperationMigrator(context, Json, dao))
        val ids = (1..1200).map { "op-$it" }
        ids.forEach { queue.enqueue(op(it)) }

        queue.remove(ids.take(1100).toSet())

        assertEquals(ids.drop(1100), queue.list().map { it.operationId })
    }

    @Test
    fun queue_enqueueDeduplicatesAndListKeepsOrder() = runBlocking {
        val queue = RoomPendingOperationQueue(db, dao, LegacyPendingOperationMigrator(context, Json, dao))
        queue.enqueue(op("b"))
        queue.enqueue(op("a"))
        queue.enqueue(op("b"))

        assertEquals(listOf("b", "a"), queue.list().map { it.operationId })
        assertEquals(listOf("b", "a"), queue.observe().first().map { it.operationId })
    }

    private fun op(id: String) = PendingOperation(
        operationId = id,
        workOrderId = "wo-1",
        userId = "u1",
        type = SyncOperationType.START,
        performedAt = Instant.parse("2026-01-01T10:00:00Z"),
        summary = null
    )
}
