package pe.edu.upc.predictivemaintain.app.telemetry.infrastructure.implementation

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.network.ErrorParser
import pe.edu.upc.predictivemaintain.app.core.network.safeApiCall
import pe.edu.upc.predictivemaintain.app.telemetry.domain.entity.SensorPanelItem
import pe.edu.upc.predictivemaintain.app.telemetry.domain.repository.SensorPanelRepository
import pe.edu.upc.predictivemaintain.app.telemetry.infrastructure.mapper.SensorPanelMapper
import pe.edu.upc.predictivemaintain.app.telemetry.infrastructure.remote.TelemetryApiService
import javax.inject.Inject

class SensorPanelRepositoryImpl @Inject constructor(
    private val telemetryApiService: TelemetryApiService,
    private val errorParser: ErrorParser
) : SensorPanelRepository {

    override suspend fun getSensorPanel(assetId: String): Outcome<List<SensorPanelItem>> {
        return when (val apiResult = safeApiCall(errorParser) { telemetryApiService.getSensors(assetId) }) {
            is Outcome.Success -> SensorPanelMapper.toDomain(apiResult.data)
            is Outcome.Failure -> apiResult
        }
    }
}
