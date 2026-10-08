package pe.edu.upc.predictivemaintain.app.iam.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.DisplayName
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Email

interface SessionRepository {
    fun observeSession(): Flow<AuthSession?>
    suspend fun currentSession(): AuthSession?
    suspend fun saveSession(session: AuthSession)
    suspend fun clearSession()
    suspend fun saveUserProfile(displayName: DisplayName, email: Email)
    suspend fun getSavedUserProfile(): Pair<DisplayName, Email>?
}
