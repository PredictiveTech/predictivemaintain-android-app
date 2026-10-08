package pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject

enum class CommunicationStatus {
    ONLINE,
    NO_COMMUNICATION,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): CommunicationStatus {
            return when (value?.uppercase()) {
                "ONLINE" -> ONLINE
                "NO_COMMUNICATION" -> NO_COMMUNICATION
                else -> UNKNOWN
            }
        }
    }
}
