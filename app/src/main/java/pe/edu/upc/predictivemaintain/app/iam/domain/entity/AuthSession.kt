package pe.edu.upc.predictivemaintain.app.iam.domain.entity

import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.AccessToken
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.TenantId
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId
import java.time.Instant

data class AuthSession(
    val userId: UserId,
    val tenantId: TenantId,
    val roles: List<Role>,
    val accessToken: AccessToken,
    val expiresAt: Instant
) {
    fun isExpired(now: Instant = Instant.now()): Boolean {
        return !now.isBefore(expiresAt)
    }
}
