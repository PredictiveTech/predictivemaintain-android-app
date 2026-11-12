package pe.edu.upc.predictivemaintain.app.maintenance.domain.repository

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Asset
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetChanges
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetDetail
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.NewAsset

interface AssetRepository {
    suspend fun getAssets(status: String?, page: Int, size: Int): Outcome<PageResult<Asset>>
    suspend fun getAsset(id: String): Outcome<AssetDetail>
    suspend fun createAsset(newAsset: NewAsset): Outcome<Asset>
    suspend fun deactivateAsset(id: String, force: Boolean): Outcome<AssetDetail>
    suspend fun updateAsset(id: String, changes: AssetChanges): Outcome<AssetDetail>
}
