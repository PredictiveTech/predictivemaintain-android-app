package pe.edu.upc.predictivemaintain.app.iam.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.UserProfile
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.AuthRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Outcome<UserProfile> {
        return authRepository.fetchProfile()
    }
}
