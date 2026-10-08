package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

enum class AssetStatus {
    OPERATIONAL,
    IN_ALERT,
    NO_COMMUNICATION,
    INACTIVE,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): AssetStatus {
            return when (value?.uppercase()) {
                "OPERATIONAL" -> OPERATIONAL
                "IN_ALERT" -> IN_ALERT
                "NO_COMMUNICATION" -> NO_COMMUNICATION
                "INACTIVE" -> INACTIVE
                else -> UNKNOWN
            }
        }
    }
}
