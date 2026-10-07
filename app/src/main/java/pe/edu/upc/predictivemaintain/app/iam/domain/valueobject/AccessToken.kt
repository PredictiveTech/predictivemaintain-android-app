package pe.edu.upc.predictivemaintain.app.iam.domain.valueobject

@JvmInline
value class AccessToken(val value: String) {
    init {
        require(value.isNotBlank()) { "AccessToken cannot be blank" }
    }

    override fun toString(): String = "AccessToken(***)"
}
