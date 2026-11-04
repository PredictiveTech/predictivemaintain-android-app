package pe.edu.upc.predictivemaintain.app.maintenance.domain.sync

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import java.time.Instant

interface SyncRepository {
    suspend fun synchronize(
        deviceSentAt: Instant,
        operations: List<PendingOperation>
    ): Outcome<List<SyncItemResult>>
}
