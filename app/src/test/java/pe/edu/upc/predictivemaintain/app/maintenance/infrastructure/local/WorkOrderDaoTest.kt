package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.TechnicianId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderStatus
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WorkOrderDaoTest {

    private lateinit var db: MaintenanceDatabase
    private lateinit var store: WorkOrderLocalStore

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MaintenanceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        store = WorkOrderLocalStore(db.workOrderDao())
    }

    @After
    fun tearDown() = db.close()

    private fun order(id: String = "wo-1", status: WorkOrderStatus = WorkOrderStatus.ASSIGNED, version: Long = 3) =
        WorkOrder(
            id = WorkOrderId(id),
            alertId = AlertId("al-1"),
            assetId = AssetId("as-1"),
            assetCode = "PUMP-01",
            assetName = "Main pump",
            severity = AlertSeverity.CRITICAL,
            assignedUserId = TechnicianId("u1"),
            status = status,
            summary = null,
            openedAt = Instant.parse("2026-01-01T10:00:00.123456789Z"),
            completedAt = null,
            version = version
        )

    @Test
    fun put_thenGet_roundTripsEveryField() = runBlocking {
        val completed = order().copy(
            status = WorkOrderStatus.COMPLETED,
            summary = "replaced bearing",
            completedAt = Instant.parse("2026-01-02T08:00:00Z"),
            assignedUserId = null
        )
        store.put(completed)

        assertEquals(completed, store.get("wo-1"))
    }

    @Test
    fun get_returnsNullWhenMissing() = runBlocking {
        assertNull(store.get("nope"))
    }

    @Test
    fun put_replacesExistingRow() = runBlocking {
        store.put(order(status = WorkOrderStatus.ASSIGNED, version = 3))
        store.put(order(status = WorkOrderStatus.IN_PROGRESS, version = 4))

        val read = store.get("wo-1")!!
        assertEquals(WorkOrderStatus.IN_PROGRESS, read.status)
        assertEquals(4L, read.version)
    }

    @Test
    fun putAll_storesEveryOrder() = runBlocking {
        store.putAll(listOf(order("a"), order("b"), order("c")))

        assertEquals(listOf("a", "b", "c"), listOf("a", "b", "c").map { store.get(it)!!.id.value })
    }

    @Test
    fun putAll_withEmptyListDoesNothing() = runBlocking {
        store.putAll(emptyList())
        assertNull(store.get("a"))
    }

    @Test
    fun applyServerState_updatesStatusAndVersion() = runBlocking {
        store.put(order(status = WorkOrderStatus.IN_PROGRESS, version = 3))

        store.applyServerState("wo-1", "completed", 7)

        val read = store.get("wo-1")!!
        assertEquals(WorkOrderStatus.COMPLETED, read.status)
        assertEquals(7L, read.version)
    }

    @Test
    fun applyServerState_withNullsKeepsCurrentValues() = runBlocking {
        store.put(order(status = WorkOrderStatus.IN_PROGRESS, version = 3))

        store.applyServerState("wo-1", null, null)

        val read = store.get("wo-1")!!
        assertEquals(WorkOrderStatus.IN_PROGRESS, read.status)
        assertEquals(3L, read.version)
    }

    @Test
    fun applyServerState_onUnknownOrderDoesNotCreateIt() = runBlocking {
        store.applyServerState("ghost", "COMPLETED", 9)
        assertNull(store.get("ghost"))
    }

    @Test
    fun get_treatsCorruptRowAsMiss() = runBlocking {
        db.workOrderDao().upsert(order().toEntity(0).copy(openedAt = "garbage"))
        assertNull(store.get("wo-1"))
    }
}
