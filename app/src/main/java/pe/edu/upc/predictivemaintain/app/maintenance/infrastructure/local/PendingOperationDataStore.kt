package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.PendingOperation
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.PendingOperationQueue
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncOperationType
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

private val Context.pendingDataStore: DataStore<Preferences> by preferencesDataStore(name = "pending_sync_prefs")

@Serializable
internal data class PendingOperationLocalModel(
    val operationId: String,
    val workOrderId: String,
    val userId: String,
    val type: String,
    val performedAt: String,
    val summary: String? = null
)

@Singleton
class PendingOperationDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val json: Json
) : PendingOperationQueue {

    private val serializer = ListSerializer(PendingOperationLocalModel.serializer())

    override fun observe(): Flow<List<PendingOperation>> =
        context.pendingDataStore.data.map { decode(it[KEY_OPERATIONS]) }

    override suspend fun list(): List<PendingOperation> =
        decode(context.pendingDataStore.data.first()[KEY_OPERATIONS])

    override suspend fun enqueue(operation: PendingOperation) {
        context.pendingDataStore.edit { prefs ->
            val current = decodeModels(prefs[KEY_OPERATIONS])
            if (current.any { it.operationId == operation.operationId }) return@edit
            prefs[KEY_OPERATIONS] = json.encodeToString(serializer, current + operation.toLocal())
        }
    }

    override suspend fun remove(operationIds: Set<String>) {
        context.pendingDataStore.edit { prefs ->
            val remaining = decodeModels(prefs[KEY_OPERATIONS]).filterNot { it.operationId in operationIds }
            prefs[KEY_OPERATIONS] = json.encodeToString(serializer, remaining)
        }
    }

    private fun decodeModels(raw: String?): List<PendingOperationLocalModel> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString(serializer, raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun decode(raw: String?): List<PendingOperation> =
        decodeModels(raw).mapNotNull { it.toDomainOrNull() }

    private fun PendingOperation.toLocal() = PendingOperationLocalModel(
        operationId = operationId,
        workOrderId = workOrderId,
        userId = userId,
        type = type.name,
        performedAt = performedAt.toString(),
        summary = summary
    )

    private fun PendingOperationLocalModel.toDomainOrNull(): PendingOperation? = try {
        PendingOperation(
            operationId = operationId,
            workOrderId = workOrderId,
            userId = userId,
            type = SyncOperationType.valueOf(type),
            performedAt = Instant.parse(performedAt),
            summary = summary
        )
    } catch (_: Exception) {
        null
    }

    private companion object {
        val KEY_OPERATIONS = stringPreferencesKey("operations")
    }
}
