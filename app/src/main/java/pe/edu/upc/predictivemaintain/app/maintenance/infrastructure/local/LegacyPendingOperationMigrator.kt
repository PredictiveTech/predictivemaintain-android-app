package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
internal data class LegacyPendingOperation(
    val operationId: String,
    val workOrderId: String,
    val userId: String,
    val type: String,
    val performedAt: String,
    val summary: String? = null
)

/**
 * One-time move of the operations queued by the old Preferences DataStore implementation into Room.
 * The DataStore file is deleted only after the rows are committed, and inserts ignore duplicates, so a crash
 * half way simply repeats the move on the next run without duplicating or losing operations.
 */
@Singleton
class LegacyPendingOperationMigrator @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val json: Json,
    private val dao: PendingOperationDao
) {
    private val mutex = Mutex()
    @Volatile private var done = false

    suspend fun migrateIfNeeded() {
        if (done) return
        mutex.withLock {
            if (done) return
            withContext(Dispatchers.IO) { migrate() }
            done = true
        }
    }

    private suspend fun migrate() {
        val file = context.preferencesDataStoreFile(LEGACY_NAME)
        if (!file.exists()) return

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val store = PreferenceDataStoreFactory.create(scope = scope) { file }
            val raw = try {
                store.data.first()[KEY_OPERATIONS]
            } catch (_: Exception) {
                null // unreadable file: nothing recoverable, same as the old implementation
            }
            val operations = decode(raw)
            if (operations.isNotEmpty()) dao.insertAll(operations)
        } finally {
            scope.cancel()
        }
        file.delete()
    }

    private fun decode(raw: String?): List<PendingOperationEntity> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString(ListSerializer(LegacyPendingOperation.serializer()), raw).map {
                PendingOperationEntity(
                    operationId = it.operationId,
                    workOrderId = it.workOrderId,
                    userId = it.userId,
                    type = it.type,
                    performedAt = it.performedAt,
                    summary = it.summary
                )
            }
        } catch (_: Exception) {
            emptyList() // the old implementation also treated an unreadable queue as empty
        }
    }

    private companion object {
        const val LEGACY_NAME = "pending_sync_prefs"
        val KEY_OPERATIONS = stringPreferencesKey("operations")
    }
}
