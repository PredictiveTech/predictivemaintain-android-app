package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

data class AssetCode(val value: String) {
    init {
        require(value.isNotBlank()) { "AssetCode cannot be blank" }
    }
}
