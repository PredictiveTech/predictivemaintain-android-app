package pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val tenantId: String,
    val email: String,
    val displayName: String,
    val active: Boolean,
    val roles: List<String>
)
