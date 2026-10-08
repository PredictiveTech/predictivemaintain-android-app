package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

data class TechnicianId(val value: String) {
    init {
        require(value.isNotBlank()) { "TechnicianId cannot be blank" }
    }
}
