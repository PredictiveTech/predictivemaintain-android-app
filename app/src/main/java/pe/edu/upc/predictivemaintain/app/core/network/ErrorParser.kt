package pe.edu.upc.predictivemaintain.app.core.network

import kotlinx.serialization.json.Json
import pe.edu.upc.predictivemaintain.app.core.error.AppError

class ErrorParser(
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    fun parse(rawJson: String?, httpStatusCode: Int): AppError.Api {
        if (rawJson.isNullOrBlank()) {
            return AppError.Api(
                httpStatus = httpStatusCode,
                code = "HTTP_$httpStatusCode",
                detail = "HTTP Error $httpStatusCode",
                fieldErrors = emptyMap()
            )
        }

        return try {
            val dto = json.decodeFromString<Rfc7807ErrorDto>(rawJson)
            AppError.Api(
                httpStatus = dto.status ?: httpStatusCode,
                code = dto.code ?: "HTTP_$httpStatusCode",
                detail = dto.detail ?: dto.title ?: "HTTP Error $httpStatusCode",
                fieldErrors = dto.errors ?: emptyMap()
            )
        } catch (_: Exception) {
            AppError.Api(
                httpStatus = httpStatusCode,
                code = "UNPARSABLE_ERROR",
                detail = rawJson,
                fieldErrors = emptyMap()
            )
        }
    }
}
