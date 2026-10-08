package pe.edu.upc.predictivemaintain.app.iam.domain.valueobject

@JvmInline
value class TenantId(val value: String) {
    init {
        require(value.isNotBlank()) { "TenantId cannot be blank" }
    }
}
