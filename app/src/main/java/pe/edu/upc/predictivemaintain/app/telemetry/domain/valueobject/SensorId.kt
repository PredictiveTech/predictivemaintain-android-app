package pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject

data class SensorId(val value: String) {
    init {
        require(value.isNotBlank()) { "SensorId cannot be blank" }
    }
}
