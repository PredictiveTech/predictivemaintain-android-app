package pe.edu.upc.predictivemaintain.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import android.util.Log
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upc.predictivemaintain.app.core.network.SessionExpiredSignal
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.LogoutUseCase
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.ObserveSessionUseCase
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.notifications.application.usecase.RegisterDeviceTokenUseCase
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val observeSessionUseCase: ObserveSessionUseCase,
    sessionExpiredSignal: SessionExpiredSignal,
    private val logoutUseCase: LogoutUseCase,
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase
) : ViewModel() {

    val session: StateFlow<AuthSession?> = observeSessionUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        // After a successful login (and when a stored session is restored): fetch the FCM token and send it.
        viewModelScope.launch {
            observeSessionUseCase()
                .filterNotNull()
                .map { it.userId }
                .distinctUntilChanged()
                .collect {
                    Log.d(TAG, "Session active: registering device for push notifications")
                    when (val result = registerDeviceTokenUseCase()) {
                        is Outcome.Success -> Log.d(TAG, "Device registered for push notifications")
                        is Outcome.Failure -> Log.d(TAG, "Device registration failed: ${result.error}")
                    }
                }
        }
        viewModelScope.launch {
            sessionExpiredSignal.events.collect {
                logoutUseCase()
            }
        }
    }

    private companion object {
        const val TAG = "FCM"
    }
}
