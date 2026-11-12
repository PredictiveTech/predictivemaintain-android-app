package pe.edu.upc.predictivemaintain.app.iam.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.UserProfile
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.AuthRepository
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.SessionRepository
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.DisplayName
import javax.inject.Inject

/**
 * Updates the signed-in user's display name (PATCH users/me) and refreshes the locally saved
 * profile so the offline fallback of [GetProfileUseCase] stays in sync.
 */
class UpdateMyProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(displayName: DisplayName): Outcome<UserProfile> {
        return authRepository.updateProfile(displayName).onSuccess { profile ->
            sessionRepository.saveUserProfile(profile.displayName, profile.email)
        }
    }
}
