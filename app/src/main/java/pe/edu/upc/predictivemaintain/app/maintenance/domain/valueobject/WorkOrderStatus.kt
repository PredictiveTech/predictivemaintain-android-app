package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

enum class WorkOrderStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): WorkOrderStatus {
            return when (value?.uppercase()) {
                "OPEN" -> OPEN
                "ASSIGNED" -> ASSIGNED
                "IN_PROGRESS" -> IN_PROGRESS
                "COMPLETED" -> COMPLETED
                "CANCELLED" -> CANCELLED
                else -> UNKNOWN
            }
        }
    }
}
