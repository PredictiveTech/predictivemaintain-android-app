package pe.edu.upc.predictivemaintain.app.core.network

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import retrofit2.Response
import java.io.IOException

suspend fun <T> safeApiCall(
    errorParser: ErrorParser = ErrorParser(),
    apiCall: suspend () -> Response<T>
): Outcome<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Outcome.Success(body)
            } else if (response.code() == 204) {
                @Suppress("UNCHECKED_CAST")
                Outcome.Success(Unit as T)
            } else {
                Outcome.Failure(
                    AppError.Api(
                        httpStatus = response.code(),
                        code = "EMPTY_BODY",
                        detail = "Response body was unexpectedly null"
                    )
                )
            }
        } else {
            val hadAuthHeader = response.raw().request.header("Authorization") != null
            if (response.code() == 401 && hadAuthHeader) {
                Outcome.Failure(AppError.SessionExpired)
            } else {
                val errorBody = response.errorBody()?.string()
                val apiError = errorParser.parse(errorBody, response.code())
                Outcome.Failure(apiError)
            }
        }
    } catch (e: IOException) {
        Outcome.Failure(AppError.Network(e))
    } catch (e: Exception) {
        Outcome.Failure(AppError.Unknown(e))
    }
}
