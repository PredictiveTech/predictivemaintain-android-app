package pe.edu.upc.predictivemaintain.app.iam.domain.valueobject

enum class Role {
    MAINTENANCE_MANAGER,
    TECHNICIAN,
    OPERATOR;

    companion object {
        fun fromString(value: String): Role {
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown role: $value")
        }
    }
}
