package pe.edu.upc.predictivemaintain.app.core.network

import kotlinx.serialization.Serializable

@Serializable
data class Rfc7807ErrorDto(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val instance: String? = null,
    val code: String? = null,
    val errors: Map<String, String>? = null
)
