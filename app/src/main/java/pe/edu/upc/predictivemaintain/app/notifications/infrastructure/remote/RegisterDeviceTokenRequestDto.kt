package pe.edu.upc.predictivemaintain.app.notifications.infrastructure.remote

import kotlinx.serialization.Serializable

// No default for platform: the app's Json does not encode defaults, so it would be omitted from the body.
@Serializable
data class RegisterDeviceTokenRequestDto(
    val token: String,
    val platform: String
)
