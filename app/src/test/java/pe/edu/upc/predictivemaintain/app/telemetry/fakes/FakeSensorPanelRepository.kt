package pe.edu.upc.predictivemaintain.app.telemetry.fakes

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.telemetry.domain.entity.SensorPanelItem
import pe.edu.upc.predictivemaintain.app.telemetry.domain.repository.SensorPanelRepository

class FakeSensorPanelRepository : SensorPanelRepository {
    var getSensorPanelResult: Outcome<List<SensorPanelItem>> = Outcome.Success(emptyList())

    override suspend fun getSensorPanel(assetId: String): Outcome<List<SensorPanelItem>> {
        return getSensorPanelResult
    }
}
