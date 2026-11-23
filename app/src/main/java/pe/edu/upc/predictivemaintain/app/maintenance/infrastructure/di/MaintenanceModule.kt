package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import android.content.Context
import androidx.room.Room
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.PendingOperationQueue
import pe.edu.upc.predictivemaintain.app.maintenance.domain.sync.SyncRepository
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.implementation.SyncRepositoryImpl
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local.MaintenanceDatabase
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local.PendingOperationDao
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local.RoomPendingOperationQueue
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.local.WorkOrderDao
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.SyncApiService
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

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        impl: SyncRepositoryImpl
    ): SyncRepository

    @Binds
    @Singleton
    abstract fun bindPendingOperationQueue(
        impl: RoomPendingOperationQueue
    ): PendingOperationQueue

    companion object {
        @Provides
        @Singleton
        fun provideMaintenanceDatabase(@ApplicationContext context: Context): MaintenanceDatabase =
            Room.databaseBuilder(context, MaintenanceDatabase::class.java, MaintenanceDatabase.NAME).build()

        @Provides
        fun providePendingOperationDao(database: MaintenanceDatabase): PendingOperationDao =
            database.pendingOperationDao()

        @Provides
        fun provideWorkOrderDao(database: MaintenanceDatabase): WorkOrderDao =
            database.workOrderDao()

        @Provides
        @Singleton
        fun provideSyncApiService(retrofit: Retrofit): SyncApiService {
            return retrofit.create(SyncApiService::class.java)
        }

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
