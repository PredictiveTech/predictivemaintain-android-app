package pe.edu.upc.predictivemaintain.app.iam.infrastructure.mapper

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.domain.entity.Technician
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.DisplayName
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Email
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.UserId
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.UserDto
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.UserPageDto

object TechnicianMapper {

    fun toDomain(dto: UserDto): Outcome<Technician> = mapping {
        Technician(
            id = field("id") {
                require(dto.id.isNotBlank()) { "Id cannot be blank" }
                UserId(dto.id)
            },
            displayName = field("displayName") { DisplayName(dto.displayName) },
            email = field("email") { Email(dto.email.trim()) }
        )
    }

    fun toDomain(dto: UserPageDto): Outcome<List<Technician>> = mapping {
        dto.items.map { itemDto ->
            when (val result = toDomain(itemDto)) {
                is Outcome.Success -> result.data
                is Outcome.Failure -> throw InvalidField(
                    when (val err = result.error) {
                        is AppError.InvalidResponse -> err.field
                        else -> "items"
                    }
                )
            }
        }
    }

    private class InvalidField(val field: String) : RuntimeException("Invalid field: $field")

    private inline fun <T> field(name: String, build: () -> T): T {
        try {
            return build()
        } catch (_: IllegalArgumentException) {
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
