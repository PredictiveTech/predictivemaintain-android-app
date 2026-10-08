package pe.edu.upc.predictivemaintain.app.iam.domain.valueobject

@JvmInline
value class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "Email cannot be blank" }
        require(EMAIL_REGEX.matches(value.trim())) { "Invalid email format: $value" }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        fun isValid(value: String): Boolean {
            val trimmed = value.trim()
            return trimmed.isNotBlank() && EMAIL_REGEX.matches(trimmed)
        }

        fun createOrNull(value: String): Email? {
            val trimmed = value.trim()
            return if (isValid(trimmed)) Email(trimmed) else null
        }
    }
}
