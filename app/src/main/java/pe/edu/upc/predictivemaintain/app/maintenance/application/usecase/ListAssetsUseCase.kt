package pe.edu.upc.predictivemaintain.app.maintenance.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Asset
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.AssetRepository
import javax.inject.Inject

class ListAssetsUseCase @Inject constructor(
    private val assetRepository: AssetRepository
) {
    suspend operator fun invoke(status: String?, page: Int, size: Int = 20): Outcome<PageResult<Asset>> {
        return assetRepository.getAssets(status, page, size)
    }
}
