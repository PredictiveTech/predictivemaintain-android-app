package pe.edu.upc.predictivemaintain.app.iam.fakes

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.Technician
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.UserDirectoryRepository

class FakeUserDirectoryRepository : UserDirectoryRepository {

    var listTechniciansResult: Outcome<List<Technician>> = Outcome.Success(emptyList())

    override suspend fun listTechnicians(): Outcome<List<Technician>> {
        return listTechniciansResult
    }
}
