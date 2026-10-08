package pe.edu.upc.predictivemaintain.app.maintenance.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Alert
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.AlertRepository
import javax.inject.Inject

class ListAlertsUseCase @Inject constructor(
    private val alertRepository: AlertRepository
) {
    suspend operator fun invoke(
        severity: String?,
        status: String?,
        assetId: String?,
        page: Int,
        size: Int = 20
    ): Outcome<PageResult<Alert>> {
        return alertRepository.getAlerts(severity, status, assetId, page, size)
    }
}
