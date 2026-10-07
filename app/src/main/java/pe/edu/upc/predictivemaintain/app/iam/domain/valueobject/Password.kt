package pe.edu.upc.predictivemaintain.app.iam.domain.valueobject

@JvmInline
value class Password(val value: String) {
    init {
        require(value.isNotBlank()) { "Password cannot be blank" }
    }

    override fun toString(): String = "Password(***)"

    companion object {
        fun isValid(value: String): Boolean = value.isNotBlank()
    }
}
