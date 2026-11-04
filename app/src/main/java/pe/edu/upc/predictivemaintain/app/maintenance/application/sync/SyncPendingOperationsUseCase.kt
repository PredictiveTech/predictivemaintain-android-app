package pe.edu.upc.predictivemaintain.app.maintenance.application.sync

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.SessionRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.PendingOperationQueue
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncItemResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncReport
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncRepository
import java.time.Instant
import javax.inject.Inject

class SyncPendingOperationsUseCase @Inject constructor(
    private val queue: PendingOperationQueue,
    private val syncRepository: SyncRepository,
    private val sessionRepository: SessionRepository
) {
    /**
     * Sends the current user's pending operations (oldest first, in batches of the server's maximum).
     * Every operation the server answered is removed from the queue, whatever the outcome: the server
     * has the final word. If a batch fails nothing is removed and the failure is returned.
     */
    suspend operator fun invoke(): Outcome<SyncReport> {
        val userId = sessionRepository.currentSession()?.userId?.value
            ?: return Outcome.Success(SyncReport(emptyList()))

        val results = mutableListOf<SyncItemResult>()
        while (true) {
            val batch = queue.list().filter { it.userId == userId }.take(MAX_BATCH)
            if (batch.isEmpty()) break

            when (val outcome = syncRepository.synchronize(Instant.now(), batch)) {
                is Outcome.Failure -> {
                    // Keep what was already synced in earlier batches visible to the caller only on success.
                    return if (results.isEmpty()) outcome else Outcome.Success(SyncReport(results))
                }
                is Outcome.Success -> {
                    val answered = outcome.data.map { it.operationId }.toSet()
                    if (answered.isEmpty()) break // no progress: avoid looping forever
                    queue.remove(answered)
                    results += outcome.data
                }
            }
        }
        return Outcome.Success(SyncReport(results))
    }

    private companion object {
        const val MAX_BATCH = 100
    }
}
