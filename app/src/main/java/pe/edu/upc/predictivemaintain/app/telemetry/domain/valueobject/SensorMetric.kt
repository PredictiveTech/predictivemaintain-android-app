package pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject

enum class SensorMetric {
    VIBRATION,
    TEMPERATURE,
    PRESSURE,
    CURRENT,
    NOISE,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): SensorMetric {
            return when (value?.uppercase()) {
                "VIBRATION" -> VIBRATION
                "TEMPERATURE" -> TEMPERATURE
                "PRESSURE" -> PRESSURE
                "CURRENT" -> CURRENT
                "NOISE" -> NOISE
                else -> UNKNOWN
            }
        }
    }
}
