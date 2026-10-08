package pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class UserPageDto(
    val items: List<UserDto>,
    val totalElements: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int
)
