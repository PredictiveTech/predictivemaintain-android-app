package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

enum class Criticality {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): Criticality {
            return when (value?.uppercase()) {
                "LOW" -> LOW
                "MEDIUM" -> MEDIUM
                "HIGH" -> HIGH
                "CRITICAL" -> CRITICAL
                else -> UNKNOWN
            }
        }
    }
}
