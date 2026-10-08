package pe.edu.upc.predictivemaintain.app.iam.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.GetProfileUseCase
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.LogoutUseCase
import pe.edu.upc.predictivemaintain.app.iam.presentation.state.HomeUiState
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isOffline = false, hasNoConnection = false) }
            when (val result = getProfileUseCase()) {
                is Outcome.Success -> {
                    val profileResult = result.data
                    val profile = profileResult.profile
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            displayName = profile.displayName.value,
                            email = profile.email.value,
                            roles = profile.roles,
                            isOffline = profileResult.isOffline,
                            hasNoConnection = false
                        )
                    }
                }
                is Outcome.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasNoConnection = true,
                            errorMessage = "No internet connection and no saved data available."
                        )
                    }
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onLoggedOut()
        }
    }
}
