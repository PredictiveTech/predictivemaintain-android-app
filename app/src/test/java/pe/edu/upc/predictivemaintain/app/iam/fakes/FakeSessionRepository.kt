package pe.edu.upc.predictivemaintain.app.iam.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.SessionRepository
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.DisplayName
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Email

class FakeSessionRepository : SessionRepository {

    private val sessionState = MutableStateFlow<AuthSession?>(null)
    private var savedProfile: Pair<DisplayName, Email>? = null

    override fun observeSession(): Flow<AuthSession?> = sessionState.asStateFlow()

    override suspend fun currentSession(): AuthSession? = sessionState.value

    override suspend fun saveSession(session: AuthSession) {
        sessionState.value = session
    }

    override suspend fun clearSession() {
        sessionState.value = null
        savedProfile = null
    }

    override suspend fun saveUserProfile(displayName: DisplayName, email: Email) {
        savedProfile = Pair(displayName, email)
    }

    override suspend fun getSavedUserProfile(): Pair<DisplayName, Email>? {
        return savedProfile
    }

    fun setSessionSync(session: AuthSession?) {
        sessionState.value = session
    }
}
