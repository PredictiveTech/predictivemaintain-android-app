package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

data class AlertId(val value: String) {
    init {
        require(value.isNotBlank()) { "AlertId cannot be blank" }
    }
}
