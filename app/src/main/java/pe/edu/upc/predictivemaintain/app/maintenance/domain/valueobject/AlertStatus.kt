package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

enum class AlertStatus {
    IN_REVIEW,
    CONFIRMED,
    DISCARDED,
    RESOLVED,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): AlertStatus {
            return when (value?.uppercase()) {
                "IN_REVIEW" -> IN_REVIEW
                "CONFIRMED" -> CONFIRMED
                "DISCARDED" -> DISCARDED
                "RESOLVED" -> RESOLVED
                else -> UNKNOWN
            }
        }
    }
}
