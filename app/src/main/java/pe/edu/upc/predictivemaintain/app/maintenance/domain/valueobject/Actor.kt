package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

data class Actor(
    val userId: String,
    val isManager: Boolean,
    val isTechnician: Boolean
)
