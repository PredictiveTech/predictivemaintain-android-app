package pe.edu.upc.predictivemaintain.app.iam.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession

interface SessionRepository {
    fun observeSession(): Flow<AuthSession?>
    suspend fun currentSession(): AuthSession?
    suspend fun saveSession(session: AuthSession)
    suspend fun clearSession()
}
