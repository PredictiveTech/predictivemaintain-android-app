package pe.edu.upc.predictivemaintain.app.telemetry.domain.repository

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.telemetry.domain.entity.SensorPanelItem

interface SensorPanelRepository {
    suspend fun getSensorPanel(assetId: String): Outcome<List<SensorPanelItem>>
}
