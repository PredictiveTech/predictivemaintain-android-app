package pe.edu.upc.predictivemaintain.app.iam.infrastructure.implementation

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.network.ErrorParser
import pe.edu.upc.predictivemaintain.app.core.network.safeApiCall
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.Technician
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.UserDirectoryRepository
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.mapper.TechnicianMapper
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.UserDirectoryApiService
import javax.inject.Inject

class UserDirectoryRepositoryImpl @Inject constructor(
    private val apiService: UserDirectoryApiService,
    private val errorParser: ErrorParser
) : UserDirectoryRepository {

    override suspend fun listTechnicians(): Outcome<List<Technician>> {
        return when (val apiResult = safeApiCall(errorParser) { apiService.getUsers() }) {
            is Outcome.Success -> TechnicianMapper.toDomain(apiResult.data)
            is Outcome.Failure -> apiResult
        }
    }
}
