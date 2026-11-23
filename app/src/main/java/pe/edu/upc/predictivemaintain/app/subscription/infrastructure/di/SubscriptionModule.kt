package pe.edu.upc.predictivemaintain.app.subscription.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.predictivemaintain.app.subscription.domain.repository.SubscriptionRepository
import pe.edu.upc.predictivemaintain.app.subscription.infrastructure.implementation.SubscriptionRepositoryImpl
import pe.edu.upc.predictivemaintain.app.subscription.infrastructure.remote.SubscriptionApiService
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SubscriptionModule {

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(impl: SubscriptionRepositoryImpl): SubscriptionRepository

    companion object {
        @Provides
        @Singleton
        fun provideSubscriptionApiService(retrofit: Retrofit): SubscriptionApiService =
            retrofit.create(SubscriptionApiService::class.java)
    }
}
