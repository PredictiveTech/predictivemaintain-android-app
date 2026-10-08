package pe.edu.upc.predictivemaintain.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upc.predictivemaintain.app.core.network.SessionExpiredSignal
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.LogoutUseCase
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.ObserveSessionUseCase
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
    sessionExpiredSignal: SessionExpiredSignal,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    val session: StateFlow<AuthSession?> = observeSessionUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            sessionExpiredSignal.events.collect {
                logoutUseCase()
            }
        }
    }
}
