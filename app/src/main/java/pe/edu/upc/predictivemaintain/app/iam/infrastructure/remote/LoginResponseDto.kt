package pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    val accessToken: String,
    val tokenType: String,
    val expiresAt: String,
    val userId: String,
    val tenantId: String,
    val roles: List<String>
) {
    override fun toString(): String {
        return "LoginResponseDto(accessToken=***, tokenType=$tokenType, expiresAt=$expiresAt, userId=$userId, tenantId=$tenantId, roles=$roles)"
    }
}
