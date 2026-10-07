package pe.edu.upc.predictivemaintain.app.iam.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.AuthRepository
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.SessionRepository
import java.time.Instant
import javax.inject.Inject

class RestoreSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(now: Instant = Instant.now()): Outcome<AuthSession> {
        val storedSession = sessionRepository.currentSession()
            ?: return Outcome.Failure(AppError.SessionExpired)

        if (storedSession.isExpired(now)) {
            sessionRepository.clearSession()
            return Outcome.Failure(AppError.SessionExpired)
        }

        return when (val profileResult = authRepository.fetchProfile()) {
            is Outcome.Success -> {
                Outcome.Success(storedSession)
            }
            is Outcome.Failure -> {
                when (profileResult.error) {
                    is AppError.SessionExpired -> {
                        sessionRepository.clearSession()
                        Outcome.Failure(AppError.SessionExpired)
                    }
                    is AppError.Api -> {
                        if (profileResult.error.httpStatus == 401) {
                            sessionRepository.clearSession()
                            Outcome.Failure(AppError.SessionExpired)
                        } else {
                            Outcome.Failure(profileResult.error)
                        }
                    }
                    // The server's answer was unreadable: that does not prove the token is invalid,
                    // so the stored session is kept, exactly as when there is no network.
                    is AppError.Network, is AppError.InvalidResponse -> {
                        Outcome.Success(storedSession)
                    }
                    is AppError.Unknown -> {
                        Outcome.Failure(profileResult.error)
                    }
                }
            }
        }
    }
}