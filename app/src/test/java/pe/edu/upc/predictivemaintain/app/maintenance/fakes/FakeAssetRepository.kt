package pe.edu.upc.predictivemaintain.app.maintenance.fakes

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Asset
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetDetail
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.AssetRepository

class FakeAssetRepository : AssetRepository {
    var getAssetsResult: Outcome<PageResult<Asset>> = Outcome.Success(PageResult(emptyList(), 0, 0, 0L))
    var getAssetResult: Outcome<AssetDetail> = Outcome.Failure(AppError.Unknown())

    override suspend fun getAssets(status: String?, page: Int, size: Int): Outcome<PageResult<Asset>> {
        return getAssetsResult
    }

    override suspend fun getAsset(id: String): Outcome<AssetDetail> {
        return getAssetResult
    }
}
