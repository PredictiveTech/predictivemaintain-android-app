package pe.edu.upc.predictivemaintain.app.telemetry.domain.entity

import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.CommunicationStatus
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.RangeStatus
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.SensorId
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.SensorMetric
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.SensorReading
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.Threshold

data class SensorPanelItem(
    val id: SensorId,
    val metric: SensorMetric,
    val unit: String,
    val communication: CommunicationStatus,
    val latestReading: SensorReading?,
    val threshold: Threshold?,
    val rangeStatus: RangeStatus
)
