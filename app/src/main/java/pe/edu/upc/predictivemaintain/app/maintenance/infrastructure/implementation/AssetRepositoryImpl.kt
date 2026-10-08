package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.implementation

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.network.ErrorParser
import pe.edu.upc.predictivemaintain.app.core.network.safeApiCall
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Asset
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetDetail
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.AssetRepository
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.mapper.AssetMapper
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AssetApiService
import javax.inject.Inject

class AssetRepositoryImpl @Inject constructor(
    private val assetApiService: AssetApiService,
    private val errorParser: ErrorParser
) : AssetRepository {

    override suspend fun getAssets(status: String?, page: Int, size: Int): Outcome<PageResult<Asset>> {
        return when (val apiResult = safeApiCall(errorParser) { assetApiService.getAssets(status, page, size) }) {
            is Outcome.Success -> AssetMapper.toDomain(apiResult.data)
            is Outcome.Failure -> apiResult
        }
    }

    override suspend fun getAsset(id: String): Outcome<AssetDetail> {
        return when (val apiResult = safeApiCall(errorParser) { assetApiService.getAsset(id) }) {
            is Outcome.Success -> AssetMapper.toDomain(apiResult.data)
            is Outcome.Failure -> apiResult
        }
    }
}
