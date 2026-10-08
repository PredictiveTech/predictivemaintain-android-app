package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

enum class AlertSeverity {
    WARNING,
    CRITICAL,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): AlertSeverity {
            return when (value?.uppercase()) {
                "WARNING" -> WARNING
                "CRITICAL" -> CRITICAL
                else -> UNKNOWN
            }
        }
    }
}
