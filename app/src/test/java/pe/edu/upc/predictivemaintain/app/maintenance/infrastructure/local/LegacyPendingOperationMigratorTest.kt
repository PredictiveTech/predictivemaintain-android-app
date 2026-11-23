package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LegacyPendingOperationMigratorTest {

    private lateinit var context: Context
    private lateinit var db: MaintenanceDatabase
    private lateinit var queue: RoomPendingOperationQueue

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, MaintenanceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.pendingOperationDao()
        queue = RoomPendingOperationQueue(db, dao, LegacyPendingOperationMigrator(context, Json, dao))
    }

    @After
    fun tearDown() {
        db.close()
        context.preferencesDataStoreFile("pending_sync_prefs").delete()
    }

    private fun writeLegacyFile(json: String) = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            PreferenceDataStoreFactory.create(scope = scope) { context.preferencesDataStoreFile("pending_sync_prefs") }
                .edit { it[stringPreferencesKey("operations")] = json }
        } finally {
            scope.cancel()
        }
    }

    private val legacyJson = """[
        {"operationId":"op-1","workOrderId":"wo-1","userId":"u1","type":"START","performedAt":"2026-01-01T10:00:00Z"},
        {"operationId":"op-2","workOrderId":"wo-2","userId":"u1","type":"COMPLETE","performedAt":"2026-01-01T11:00:00Z","summary":"done"}
    ]"""

    @Test
    fun movesQueuedOperationsInOrderAndDeletesTheOldFile() = runBlocking {
        writeLegacyFile(legacyJson)

        val ops = queue.list()

        assertEquals(listOf("op-1", "op-2"), ops.map { it.operationId })
        assertEquals("done", ops[1].summary)
        assertFalse(context.preferencesDataStoreFile("pending_sync_prefs").exists())
    }

    @Test
    fun doesNotDuplicateOperationsAlreadyInRoom() = runBlocking {
        writeLegacyFile(legacyJson)
        db.pendingOperationDao().insert(
            PendingOperationEntity("op-1", "wo-1", "u1", "START", "2026-01-01T10:00:00Z", null)
        )

        assertEquals(listOf("op-1", "op-2"), queue.list().map { it.operationId })
    }

    @Test
    fun withoutLegacyFileDoesNothing() = runBlocking {
        assertEquals(emptyList<Any>(), queue.list())
    }

    @Test
    fun unreadableLegacyDataIsTreatedAsEmpty() = runBlocking {
        writeLegacyFile("not json")

        assertEquals(emptyList<Any>(), queue.list())
        assertFalse(context.preferencesDataStoreFile("pending_sync_prefs").exists())
    }
}
