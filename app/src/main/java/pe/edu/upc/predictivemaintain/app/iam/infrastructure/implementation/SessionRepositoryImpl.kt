package pe.edu.upc.predictivemaintain.app.iam.infrastructure.implementation

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.SessionRepository
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.DisplayName
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Email
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.local.SessionDataStore
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionDataStore: SessionDataStore
) : SessionRepository {

    override fun observeSession(): Flow<AuthSession?> {
        return sessionDataStore.observeSession()
    }

    override suspend fun currentSession(): AuthSession? {
        return sessionDataStore.currentSession()
    }

    override suspend fun saveSession(session: AuthSession) {
        sessionDataStore.saveSession(session)
    }

    override suspend fun clearSession() {
        sessionDataStore.clearSession()
    }

    override suspend fun saveUserProfile(displayName: DisplayName, email: Email) {
        sessionDataStore.saveUserProfile(displayName, email)
    }

    override suspend fun getSavedUserProfile(): Pair<DisplayName, Email>? {
        return sessionDataStore.getSavedUserProfile()
    }
}
