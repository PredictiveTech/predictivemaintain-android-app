package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

data class WorkOrderId(val value: String) {
    init {
        require(value.isNotBlank()) { "WorkOrderId cannot be blank" }
    }
}
