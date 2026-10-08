package pe.edu.upc.predictivemaintain.app.core.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val accessTokenProvider: AccessTokenProvider,
    private val sessionExpiredSignal: SessionExpiredSignal
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = accessTokenProvider.getAccessToken()

        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        val response = chain.proceed(request)

        val hadAuthHeader = !request.header("Authorization").isNullOrBlank()
        if (response.code == 401 && hadAuthHeader) {
            sessionExpiredSignal.emitSessionExpired()
        }

        return response
    }
}
