package pe.edu.upc.predictivemaintain.app.maintenance.domain.repository

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Asset
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetDetail

interface AssetRepository {
    suspend fun getAssets(status: String?, page: Int, size: Int): Outcome<PageResult<Asset>>
    suspend fun getAsset(id: String): Outcome<AssetDetail>
}
