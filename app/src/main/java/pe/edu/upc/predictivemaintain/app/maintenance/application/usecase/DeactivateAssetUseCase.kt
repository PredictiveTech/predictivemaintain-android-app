package pe.edu.upc.predictivemaintain.app.maintenance.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetDetail
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.AssetRepository
import javax.inject.Inject

/**
 * Logical deletion of an asset. Idempotent on the server. If the asset has open work orders the first
 * call fails with 409 ASSET_HAS_OPEN_WORK_ORDERS (a warning); call again with [force] = true to confirm.
 */
class DeactivateAssetUseCase @Inject constructor(
    private val assetRepository: AssetRepository
) {
    suspend operator fun invoke(id: String, force: Boolean = false): Outcome<AssetDetail> {
        return assetRepository.deactivateAsset(id, force)
    }
}
