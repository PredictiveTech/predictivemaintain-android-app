package pe.edu.upc.predictivemaintain.app.maintenance.domain.entity

import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality

/**
 * New technical data of an existing asset (PUT /assets/{id}). The code never changes.
 * [plantId] is not edited by the app, but the backend overwrites it with whatever the request carries,
 * so the current value must be sent back unchanged.
 */
data class AssetChanges(
    val name: String,
    val location: String,
    val assetType: String,
    val criticality: Criticality,
    val plantId: String? = null,
    val productionLine: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    init {
        require(name.isNotBlank() && name.length <= NewAsset.NAME_MAX) { "Invalid name" }
        require(location.isNotBlank() && location.length <= NewAsset.LOCATION_MAX) { "Invalid location" }
        require(assetType.isNotBlank() && assetType.length <= NewAsset.ASSET_TYPE_MAX) { "Invalid asset type" }
        require(criticality != Criticality.UNKNOWN) { "Criticality is required" }
        require(productionLine == null || productionLine.length <= NewAsset.PRODUCTION_LINE_MAX) { "Invalid production line" }
        require((latitude == null) == (longitude == null)) { "Latitude and longitude go together" }
        require(latitude == null || latitude in NewAsset.LATITUDE_RANGE) { "Invalid latitude" }
        require(longitude == null || longitude in NewAsset.LONGITUDE_RANGE) { "Invalid longitude" }
    }
}
