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
import pe.edu.upc.predictivemaintain.app.iam.application.usecase.UpdateMyProfileUseCase
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.DisplayName
import pe.edu.upc.predictivemaintain.app.iam.presentation.state.ProfileUiState
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateMyProfileUseCase: UpdateMyProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null, saveError = null, saveSucceeded = false) }
            when (val result = getProfileUseCase()) {
                is Outcome.Success -> {
                    val profile = result.data.profile
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOffline = result.data.isOffline,
                            email = profile.email.value,
                            roles = profile.roles,
                            displayName = profile.displayName.value,
                            savedDisplayName = profile.displayName.value
                        )
                    }
                }
                is Outcome.Failure -> _uiState.update { it.copy(isLoading = false, loadError = result.error) }
            }
        }
    }

    fun onDisplayNameChange(value: String) {
        _uiState.update { it.copy(displayName = value, saveSucceeded = false, saveError = null) }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        val newName = DisplayName(state.displayName.trim())
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSucceeded = false) }
            when (val result = updateMyProfileUseCase(newName)) {
                is Outcome.Success -> {
                    val profile = result.data
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSucceeded = true,
                            email = profile.email.value,
                            roles = profile.roles,
                            displayName = profile.displayName.value,
                            savedDisplayName = profile.displayName.value
                        )
                    }
                }
                is Outcome.Failure -> _uiState.update { it.copy(isSaving = false, saveError = result.error) }
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
