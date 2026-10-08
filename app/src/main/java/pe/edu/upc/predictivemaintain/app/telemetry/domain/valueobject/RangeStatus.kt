package pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject

enum class RangeStatus {
    NORMAL,
    OUT_OF_RANGE,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): RangeStatus {
            return when (value?.uppercase()) {
                "NORMAL" -> NORMAL
                "OUT_OF_RANGE" -> OUT_OF_RANGE
                else -> UNKNOWN
            }
        }
    }
}
