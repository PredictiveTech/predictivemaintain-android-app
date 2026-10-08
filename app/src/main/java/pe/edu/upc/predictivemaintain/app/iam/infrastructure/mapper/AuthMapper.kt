package pe.edu.upc.predictivemaintain.app.iam.infrastructure.mapper

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
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
import java.time.format.DateTimeParseException

/**
 * Anti-corruption layer between the server's DTOs and the domain.
 *
 * The mapper never guesses: a value the domain cannot accept (an unknown role, an empty role list,
 * an unreadable date, a blank id...) makes the whole mapping fail with AppError.InvalidResponse(field).
 * Nothing is dropped, defaulted or invented, and nothing is thrown outside this file.
 */
object AuthMapper {

    fun toDomain(dto: LoginResponseDto): Outcome<AuthSession> = mapping {
        AuthSession(
            userId = field("userId") { UserId(dto.userId) },
            tenantId = field("tenantId") { TenantId(dto.tenantId) },
            roles = field("roles") { parseRoles(dto.roles) },
            accessToken = field("accessToken") {
                require(dto.accessToken.isNotBlank()) { "The access token cannot be blank" }
                AccessToken(dto.accessToken)
            },
            expiresAt = field("expiresAt") { Instant.parse(dto.expiresAt) }
        )
    }

    fun toDomain(dto: UserDto): Outcome<UserProfile> = mapping {
        UserProfile(
            id = field("id") { UserId(dto.id) },
            tenantId = field("tenantId") { TenantId(dto.tenantId) },
            email = field("email") { Email(dto.email.trim()) },
            displayName = field("displayName") { DisplayName(dto.displayName) },
            active = dto.active,
            roles = field("roles") { parseRoles(dto.roles) }
        )
    }

    /**
     * Converts the server's role names. An unknown name or an empty list throws IllegalArgumentException,
     * so callers inside [field] turn it into InvalidResponse("roles"). The local session storage should
     * use the same rule when it reads roles back from disk.
     */
    fun parseRoles(raw: List<String>): List<Role> {
        require(raw.isNotEmpty()) { "A user must have at least one role" }
        return raw.map { Role.fromString(it) }
    }

    /** Only used inside this file: carries the name of the field that was invalid. */
    private class InvalidField(val field: String) : RuntimeException("Invalid field: $field")

    /** Runs [build]; if the domain rejects the value or the date cannot be parsed, reports which field it was. */
    private inline fun <T> field(name: String, build: () -> T): T {
        try {
            return build()
        } catch (_: IllegalArgumentException) {
            throw InvalidField(name)
        } catch (_: DateTimeParseException) {
            throw InvalidField(name)
        }
    }

    private inline fun <T> mapping(build: () -> T): Outcome<T> {
        return try {
            Outcome.Success(build())
        } catch (e: InvalidField) {
            Outcome.Failure(AppError.InvalidResponse(e.field))
        }
    }
}