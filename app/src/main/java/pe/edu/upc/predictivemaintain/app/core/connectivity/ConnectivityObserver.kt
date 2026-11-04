package pe.edu.upc.predictivemaintain.app.core.connectivity

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    /** Emits the current state first, then every change. true = the device has validated internet access. */
    fun observe(): Flow<Boolean>
}
