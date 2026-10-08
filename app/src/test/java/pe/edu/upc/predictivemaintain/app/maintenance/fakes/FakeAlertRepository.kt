package pe.edu.upc.predictivemaintain.app.maintenance.fakes

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Alert
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.AlertRepository

class FakeAlertRepository : AlertRepository {
    var getAlertsResult: Outcome<PageResult<Alert>> = Outcome.Success(PageResult(emptyList(), 0, 0, 0L))
    var getAlertResult: Outcome<Alert> = Outcome.Failure(AppError.Unknown())
    var reviewAlertResult: Outcome<Alert> = Outcome.Failure(AppError.Unknown())

    override suspend fun getAlerts(
        severity: String?,
        status: String?,
        assetId: String?,
        page: Int,
        size: Int
    ): Outcome<PageResult<Alert>> {
        return getAlertsResult
    }

    override suspend fun getAlert(id: String): Outcome<Alert> {
        return getAlertResult
    }

    override suspend fun reviewAlert(
        id: String,
        status: String,
        reason: String?,
        expectedVersion: Long
    ): Outcome<Alert> {
        return reviewAlertResult
    }
}
