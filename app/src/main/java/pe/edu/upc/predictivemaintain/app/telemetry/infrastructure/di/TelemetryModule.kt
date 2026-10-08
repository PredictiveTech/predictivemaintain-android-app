package pe.edu.upc.predictivemaintain.app.telemetry.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.predictivemaintain.app.telemetry.domain.repository.SensorPanelRepository
import pe.edu.upc.predictivemaintain.app.telemetry.infrastructure.implementation.SensorPanelRepositoryImpl
import pe.edu.upc.predictivemaintain.app.telemetry.infrastructure.remote.TelemetryApiService
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TelemetryModule {

    @Binds
    @Singleton
    abstract fun bindSensorPanelRepository(
        impl: SensorPanelRepositoryImpl
    ): SensorPanelRepository

    companion object {
        @Provides
        @Singleton
        fun provideTelemetryApiService(retrofit: Retrofit): TelemetryApiService {
            return retrofit.create(TelemetryApiService::class.java)
        }
    }
}
