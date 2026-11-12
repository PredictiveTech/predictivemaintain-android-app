package pe.edu.upc.predictivemaintain.app.maintenance.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetChanges
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetDetail
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.AssetRepository
import javax.inject.Inject

class UpdateAssetUseCase @Inject constructor(
    private val assetRepository: AssetRepository
) {
    suspend operator fun invoke(id: String, changes: AssetChanges): Outcome<AssetDetail> {
        return assetRepository.updateAsset(id, changes)
    }
}
