package pe.edu.upc.predictivemaintain.app.iam.domain.entity

import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.DisplayName
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Email
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.TenantId
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId

data class UserProfile(
    val id: UserId,
    val tenantId: TenantId,
    val email: Email,
    val displayName: DisplayName,
    val active: Boolean,
    val roles: List<Role>
)
