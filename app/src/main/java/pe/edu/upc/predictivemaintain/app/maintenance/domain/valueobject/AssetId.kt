package pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject

data class AssetId(val value: String) {
    init {
        require(value.isNotBlank()) { "AssetId cannot be blank" }
    }
}
