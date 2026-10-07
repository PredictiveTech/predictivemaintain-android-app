package pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
) {
    override fun toString(): String {
        return "LoginRequestDto(email=$email, password=***)"
    }
}
