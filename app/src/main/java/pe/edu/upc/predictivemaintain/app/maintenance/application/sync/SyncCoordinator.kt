package pe.edu.upc.predictivemaintain.app.maintenance.application.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import pe.edu.upc.predictivemaintain.app.core.connectivity.ConnectivityObserver
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.PendingOperationQueue
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncReport
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

data class SyncState(
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val pendingCount: Int = 0,
    val pendingOrderIds: Set<String> = emptySet()
)

sealed interface SyncEvent {
    data class Completed(val report: SyncReport) : SyncEvent
    data class Failed(val error: AppError) : SyncEvent
}

/**
 * US-28. Watches the connectivity of the device and, when it comes back (or the user asks for it), sends the
 * pending offline operations. One instance for the whole app, so only one synchronization runs at a time.
 */
@Singleton
class SyncCoordinator(
    private val connectivityObserver: ConnectivityObserver,
    private val queue: PendingOperationQueue,
    private val syncPendingOperations: SyncPendingOperationsUseCase,
    private val scope: CoroutineScope
) {
    @Inject
    constructor(
        connectivityObserver: ConnectivityObserver,
        queue: PendingOperationQueue,
        syncPendingOperations: SyncPendingOperationsUseCase
    ) : this(
        connectivityObserver,
        queue,
        syncPendingOperations,
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    )

    private val _state = MutableStateFlow(SyncState())
    val state: StateFlow<SyncState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SyncEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<SyncEvent> = _events.asSharedFlow()

    private val started = AtomicBoolean(false)
    private val mutex = Mutex()

    /** Safe to call many times; only the first call starts the observers. */
    fun start() {
        if (!started.compareAndSet(false, true)) return

        scope.launch {
            queue.observe().collect { operations ->
                _state.update {
                    it.copy(
                        pendingCount = operations.size,
                        pendingOrderIds = operations.map { op -> op.workOrderId }.toSet()
                    )
                }
            }
        }
        scope.launch {
            connectivityObserver.observe().distinctUntilChanged().collect { online ->
                _state.update { it.copy(isOnline = online) }
                if (online) runSync()
            }
        }
    }

    /** Reads the queue itself, so it is accurate right after an operation was enqueued. */
    suspend fun hasPending(workOrderId: String): Boolean =
        queue.list().any { it.workOrderId == workOrderId }

    /** Tries to send the pending operations now (e.g. on pull to refresh). Does nothing while offline. */
    fun syncNow() {
        if (!_state.value.isOnline) return
        scope.launch { runSync() }
    }

    private suspend fun runSync() {
        if (!mutex.tryLock()) return
        try {
            if (queue.list().isEmpty()) return
            _state.update { it.copy(isSyncing = true) }
            when (val result = syncPendingOperations()) {
                is Outcome.Success -> if (result.data.items.isNotEmpty()) {
                    _events.emit(SyncEvent.Completed(result.data))
                }
                is Outcome.Failure -> {
                    // Without connection we simply wait for the next reconnection; the queue is untouched.
                    if (result.error !is AppError.Network) _events.emit(SyncEvent.Failed(result.error))
                }
            }
        } finally {
            _state.update { it.copy(isSyncing = false) }
            mutex.unlock()
        }
    }
}
