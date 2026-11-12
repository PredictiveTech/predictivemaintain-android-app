package pe.edu.upc.predictivemaintain.app.notifications.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.predictivemaintain.app.notifications.domain.repository.DeviceTokenRepository
import pe.edu.upc.predictivemaintain.app.notifications.domain.service.PushTokenProvider
import pe.edu.upc.predictivemaintain.app.notifications.infrastructure.fcm.FirebasePushTokenProvider
import pe.edu.upc.predictivemaintain.app.notifications.infrastructure.implementation.DeviceTokenRepositoryImpl
import pe.edu.upc.predictivemaintain.app.notifications.infrastructure.remote.NotificationsApiService
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    @Binds
    @Singleton
    abstract fun bindDeviceTokenRepository(impl: DeviceTokenRepositoryImpl): DeviceTokenRepository

    @Binds
    @Singleton
    abstract fun bindPushTokenProvider(impl: FirebasePushTokenProvider): PushTokenProvider

    companion object {
        @Provides
        @Singleton
        fun provideNotificationsApiService(retrofit: Retrofit): NotificationsApiService {
            return retrofit.create(NotificationsApiService::class.java)
        }
    }
}
