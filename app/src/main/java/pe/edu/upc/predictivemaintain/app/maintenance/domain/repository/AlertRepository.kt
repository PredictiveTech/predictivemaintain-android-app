package pe.edu.upc.predictivemaintain.app.maintenance.domain.repository

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Alert

interface AlertRepository {
    suspend fun getAlerts(
        severity: String?,
        status: String?,
        assetId: String?,
        page: Int,
        size: Int
    ): Outcome<PageResult<Alert>>

    suspend fun getAlert(id: String): Outcome<Alert>

    suspend fun reviewAlert(
        id: String,
        status: String,
        reason: String?,
        expectedVersion: Long
    ): Outcome<Alert>
}
