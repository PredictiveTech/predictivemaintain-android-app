package pe.edu.upc.predictivemaintain.app.iam.infrastructure.mapper

import pe.edu.upc.predictivemaintain.app.iam.domain.entity.AuthSession
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.UserProfile
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.AccessToken
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.DisplayName
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Email
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.TenantId
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.LoginResponseDto
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.UserDto
import java.time.Instant

object AuthMapper {

    fun toDomain(dto: LoginResponseDto): AuthSession {
        val roles = dto.roles.mapNotNull { roleStr ->
            try { Role.fromString(roleStr) } catch (_: Exception) { null }
        }
        val expiresAtInstant = try {
            Instant.parse(dto.expiresAt)
        } catch (_: Exception) {
            Instant.now().plusSeconds(3600)
        }

        return AuthSession(
            userId = UserId(dto.userId),
            tenantId = TenantId(dto.tenantId),
            roles = roles,
            accessToken = AccessToken(dto.accessToken),
            expiresAt = expiresAtInstant
        )
    }

    fun toDomain(dto: UserDto): UserProfile {
        val roles = dto.roles.mapNotNull { roleStr ->
            try { Role.fromString(roleStr) } catch (_: Exception) { null }
        }

        return UserProfile(
            id = UserId(dto.id),
            tenantId = TenantId(dto.tenantId),
            email = Email(dto.email.trim()),
            displayName = DisplayName(dto.displayName),
            active = dto.active,
            roles = roles
        )
    }
}
