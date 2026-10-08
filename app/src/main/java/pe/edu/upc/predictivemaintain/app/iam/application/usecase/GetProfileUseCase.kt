package pe.edu.upc.predictivemaintain.app.iam.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.UserProfile
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.AuthRepository
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.SessionRepository
import javax.inject.Inject

data class ProfileResult(
    val profile: UserProfile,
    val isOffline: Boolean
)

class GetProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(): Outcome<ProfileResult> {
        return when (val result = authRepository.fetchProfile()) {
            is Outcome.Success -> {
                sessionRepository.saveUserProfile(result.data.displayName, result.data.email)
                Outcome.Success(ProfileResult(result.data, isOffline = false))
            }
            is Outcome.Failure -> {
                when (result.error) {
                    is AppError.Network, is AppError.InvalidResponse -> {
                        val savedProfile = sessionRepository.getSavedUserProfile()
                        val session = sessionRepository.currentSession()
                        if (savedProfile != null && session != null) {
                            Outcome.Success(
                                ProfileResult(
                                    UserProfile(
                                        id = session.userId,
                                        tenantId = session.tenantId,
                                        email = savedProfile.second,
                                        displayName = savedProfile.first,
                                        active = true,
                                        roles = session.roles
                                    ),
                                    isOffline = true
                                )
                            )
                        } else {
                            Outcome.Failure(result.error)
                        }
                    }
                    else -> Outcome.Failure(result.error)
                }
            }
        }
    }
}
