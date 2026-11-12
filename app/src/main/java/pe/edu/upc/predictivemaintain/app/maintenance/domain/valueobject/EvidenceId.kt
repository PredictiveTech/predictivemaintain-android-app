package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

data class EvidenceId(val value: String) {
    init {
        require(value.isNotBlank()) { "EvidenceId cannot be blank" }
    }
}
