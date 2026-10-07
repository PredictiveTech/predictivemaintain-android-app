package pe.edu.upc.predictivemaintain.app.iam.application.usecase

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.SessionRepository
import javax.inject.Inject

class ObserveSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): Flow<AuthSession?> {
        return sessionRepository.observeSession()
    }
}
