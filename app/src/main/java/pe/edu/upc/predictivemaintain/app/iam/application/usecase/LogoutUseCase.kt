package pe.edu.upc.predictivemaintain.app.iam.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.SessionRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(): Outcome<Unit> {
        sessionRepository.clearSession()
        return Outcome.Success(Unit)
    }
}
