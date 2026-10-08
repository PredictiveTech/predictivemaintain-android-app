package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.AlertRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.AssetRepository
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.implementation.AlertRepositoryImpl
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.implementation.AssetRepositoryImpl
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AlertApiService
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AssetApiService
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MaintenanceModule {

    @Binds
    @Singleton
    abstract fun bindAssetRepository(
        impl: AssetRepositoryImpl
    ): AssetRepository

    @Binds
    @Singleton
    abstract fun bindAlertRepository(
        impl: AlertRepositoryImpl
    ): AlertRepository

    companion object {
        @Provides
        @Singleton
        fun provideAssetApiService(retrofit: Retrofit): AssetApiService {
            return retrofit.create(AssetApiService::class.java)
        }

        @Provides
        @Singleton
        fun provideAlertApiService(retrofit: Retrofit): AlertApiService {
            return retrofit.create(AlertApiService::class.java)
        }
    }
}
