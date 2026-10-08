package pe.edu.upc.predictivemaintain.app.telemetry.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.telemetry.domain.entity.SensorPanelItem
import pe.edu.upc.predictivemaintain.app.telemetry.domain.repository.SensorPanelRepository
import javax.inject.Inject

class GetSensorPanelUseCase @Inject constructor(
    private val sensorPanelRepository: SensorPanelRepository
) {
    suspend operator fun invoke(assetId: String): Outcome<List<SensorPanelItem>> {
        return sensorPanelRepository.getSensorPanel(assetId)
    }
}
