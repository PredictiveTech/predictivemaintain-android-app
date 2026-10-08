package pe.edu.upc.predictivemaintain.app.core.error

sealed class AppError {
    data class Api(
        val httpStatus: Int,
        val code: String,
        val detail: String,
        val fieldErrors: Map<String, String> = emptyMap()
    ) : AppError()

    data class Network(val cause: Throwable? = null) : AppError()

    data class InvalidResponse(val field: String) : AppError()

    data object SessionExpired : AppError()

    data class Unknown(val cause: Throwable? = null) : AppError()
}
