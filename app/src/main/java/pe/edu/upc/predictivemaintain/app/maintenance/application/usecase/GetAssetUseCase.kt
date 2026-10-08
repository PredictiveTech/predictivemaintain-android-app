package pe.edu.upc.predictivemaintain.app.maintenance.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetDetail
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.AssetRepository
import javax.inject.Inject

class GetAssetUseCase @Inject constructor(
    private val assetRepository: AssetRepository
) {
    suspend operator fun invoke(id: String): Outcome<AssetDetail> {
        return assetRepository.getAsset(id)
    }
}
