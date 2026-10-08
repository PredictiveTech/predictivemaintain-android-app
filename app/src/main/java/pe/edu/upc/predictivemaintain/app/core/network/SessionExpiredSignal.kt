package pe.edu.upc.predictivemaintain.app.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface SessionExpiredSignal {
    val events: SharedFlow<Unit>
    fun emitSessionExpired()
}

class DefaultSessionExpiredSignal : SessionExpiredSignal {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val events: SharedFlow<Unit> = _events.asSharedFlow()

    override fun emitSessionExpired() {
        _events.tryEmit(Unit)
    }
}
