package pe.edu.upc.predictivemaintain.app.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale

class LanguageInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val language = Locale.getDefault().language.ifBlank { "en" }
        val request = chain.request().newBuilder()
            .header("Accept-Language", language)
            .build()
        return chain.proceed(request)
    }
}
