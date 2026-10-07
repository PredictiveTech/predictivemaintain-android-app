package pe.edu.upc.predictivemaintain.app.iam.domain.valueobject

@JvmInline
value class DisplayName(val value: String) {
    init {
        require(value.isNotBlank()) { "DisplayName cannot be blank" }
    }
}
