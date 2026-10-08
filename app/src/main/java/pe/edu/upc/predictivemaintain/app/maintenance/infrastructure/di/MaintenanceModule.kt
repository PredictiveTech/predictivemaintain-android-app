package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.AlertRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.AssetRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.WorkOrderRepository
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.implementation.AlertRepositoryImpl
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.implementation.AssetRepositoryImpl
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.implementation.WorkOrderRepositoryImpl
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AlertApiService
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AssetApiService
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.WorkOrderApiService
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

    @Binds
    @Singleton
    abstract fun bindWorkOrderRepository(
        impl: WorkOrderRepositoryImpl
    ): WorkOrderRepository

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

        @Provides
        @Singleton
        fun provideWorkOrderApiService(retrofit: Retrofit): WorkOrderApiService {
            return retrofit.create(WorkOrderApiService::class.java)
        }
    }
}
