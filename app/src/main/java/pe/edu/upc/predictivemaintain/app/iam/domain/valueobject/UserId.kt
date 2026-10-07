package pe.edu.upc.predictivemaintain.app.iam.domain.valueobject

@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "UserId cannot be blank" }
    }
}
