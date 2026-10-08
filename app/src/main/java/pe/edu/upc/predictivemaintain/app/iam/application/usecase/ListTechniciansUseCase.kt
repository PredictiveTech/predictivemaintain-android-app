package pe.edu.upc.predictivemaintain.app.iam.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.Technician
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.UserDirectoryRepository
import javax.inject.Inject

class ListTechniciansUseCase @Inject constructor(
    private val userDirectoryRepository: UserDirectoryRepository
) {
    suspend operator fun invoke(): Outcome<List<Technician>> {
        return userDirectoryRepository.listTechnicians()
    }
}
