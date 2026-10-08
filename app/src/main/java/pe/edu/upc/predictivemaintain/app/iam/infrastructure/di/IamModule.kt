package pe.edu.upc.predictivemaintain.app.iam.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.predictivemaintain.app.core.network.AccessTokenProvider
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.AuthRepository
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.SessionRepository
import pe.edu.upc.predictivemaintain.app.iam.domain.repository.UserDirectoryRepository
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.implementation.AuthRepositoryImpl
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.implementation.SessionRepositoryImpl
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.implementation.UserDirectoryRepositoryImpl
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.local.SessionDataStore
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.AuthApiService
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.UserDirectoryApiService
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class IamModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        impl: SessionRepositoryImpl
    ): SessionRepository

    @Binds
    @Singleton
    abstract fun bindAccessTokenProvider(
        impl: SessionDataStore
    ): AccessTokenProvider

    @Binds
    @Singleton
    abstract fun bindUserDirectoryRepository(
        impl: UserDirectoryRepositoryImpl
    ): UserDirectoryRepository

    companion object {
        @Provides
        @Singleton
        fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
            return retrofit.create(AuthApiService::class.java)
        }

        @Provides
        @Singleton
        fun provideUserDirectoryApiService(retrofit: Retrofit): UserDirectoryApiService {
            return retrofit.create(UserDirectoryApiService::class.java)
        }
    }
}
