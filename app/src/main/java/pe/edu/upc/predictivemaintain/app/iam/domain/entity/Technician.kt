package pe.edu.upc.predictivemaintain.app.iam.domain.entity

import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.DisplayName
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Email
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId

data class Technician(
    val id: UserId,
    val displayName: DisplayName,
    val email: Email
)
