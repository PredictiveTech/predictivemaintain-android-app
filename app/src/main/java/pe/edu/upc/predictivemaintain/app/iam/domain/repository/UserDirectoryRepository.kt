package pe.edu.upc.predictivemaintain.app.iam.domain.repository

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.Technician

interface UserDirectoryRepository {
    suspend fun listTechnicians(): Outcome<List<Technician>>
}
