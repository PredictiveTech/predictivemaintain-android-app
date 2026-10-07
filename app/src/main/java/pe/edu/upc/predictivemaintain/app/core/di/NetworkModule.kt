package pe.edu.upc.predictivemaintain.app.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pe.edu.upc.predictivemaintain.app.BuildConfig
import pe.edu.upc.predictivemaintain.app.core.network.AccessTokenProvider
import pe.edu.upc.predictivemaintain.app.core.network.AuthInterceptor
import pe.edu.upc.predictivemaintain.app.core.network.DefaultSessionExpiredSignal
import pe.edu.upc.predictivemaintain.app.core.network.ErrorParser
import pe.edu.upc.predictivemaintain.app.core.network.LanguageInterceptor
import pe.edu.upc.predictivemaintain.app.core.network.SessionExpiredSignal
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideSessionExpiredSignal(): SessionExpiredSignal = DefaultSessionExpiredSignal()

    @Provides
    @Singleton
    fun provideErrorParser(json: Json): ErrorParser = ErrorParser(json)

    @Provides
    @Singleton
    fun provideLanguageInterceptor(): LanguageInterceptor = LanguageInterceptor()

    @Provides
    @Singleton
    fun provideAccessTokenProvider(): AccessTokenProvider {
        return object : AccessTokenProvider {
            override fun getAccessToken(): String? = null
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        accessTokenProvider: AccessTokenProvider,
        sessionExpiredSignal: SessionExpiredSignal
    ): AuthInterceptor = AuthInterceptor(accessTokenProvider, sessionExpiredSignal)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        languageInterceptor: LanguageInterceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(languageInterceptor)
            .addInterceptor(authInterceptor)

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                redactHeader("Authorization")
                level = HttpLoggingInterceptor.Level.HEADERS
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}
