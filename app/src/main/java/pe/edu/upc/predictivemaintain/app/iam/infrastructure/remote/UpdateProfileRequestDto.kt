package pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequestDto(
    val displayName: String
)
