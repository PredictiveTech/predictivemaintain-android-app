package pe.edu.upc.predictivemaintain.app.maintenance.domain.entity

import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality

/** Data needed to register an asset (POST /assets). Limits mirror the backend's CreateAssetResource. */
data class NewAsset(
    val code: String,
    val name: String,
    val location: String,
    val assetType: String,
    val criticality: Criticality,
    val productionLine: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    init {
        require(code.isNotBlank() && code.length <= CODE_MAX) { "Invalid code" }
        require(name.isNotBlank() && name.length <= NAME_MAX) { "Invalid name" }
        require(location.isNotBlank() && location.length <= LOCATION_MAX) { "Invalid location" }
        require(assetType.isNotBlank() && assetType.length <= ASSET_TYPE_MAX) { "Invalid asset type" }
        require(criticality != Criticality.UNKNOWN) { "Criticality is required" }
        require(productionLine == null || productionLine.length <= PRODUCTION_LINE_MAX) { "Invalid production line" }
        require((latitude == null) == (longitude == null)) { "Latitude and longitude go together" }
        require(latitude == null || latitude in LATITUDE_RANGE) { "Invalid latitude" }
        require(longitude == null || longitude in LONGITUDE_RANGE) { "Invalid longitude" }
    }

    companion object {
        const val CODE_MAX = 50
        const val NAME_MAX = 120
        const val LOCATION_MAX = 120
        const val ASSET_TYPE_MAX = 80
        const val PRODUCTION_LINE_MAX = 100
        val LATITUDE_RANGE = -90.0..90.0
        val LONGITUDE_RANGE = -180.0..180.0
    }
}
