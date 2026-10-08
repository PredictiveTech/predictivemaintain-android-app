package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.implementation

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.network.ErrorParser
import pe.edu.upc.predictivemaintain.app.core.network.safeApiCall
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Alert
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.AlertRepository
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.mapper.AlertMapper
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AlertApiService
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.ReviewAlertRequestDto
import javax.inject.Inject

class AlertRepositoryImpl @Inject constructor(
    private val alertApiService: AlertApiService,
    private val errorParser: ErrorParser
) : AlertRepository {

    override suspend fun getAlerts(
        severity: String?,
        status: String?,
        assetId: String?,
        page: Int,
        size: Int
    ): Outcome<PageResult<Alert>> {
        return when (val apiResult = safeApiCall(errorParser) { alertApiService.getAlerts(severity, status, assetId, page, size) }) {
            is Outcome.Success -> AlertMapper.toDomain(apiResult.data)
            is Outcome.Failure -> apiResult
        }
    }

    override suspend fun getAlert(id: String): Outcome<Alert> {
        return when (val apiResult = safeApiCall(errorParser) { alertApiService.getAlert(id) }) {
            is Outcome.Success -> AlertMapper.toDomain(apiResult.data)
            is Outcome.Failure -> apiResult
        }
    }

    override suspend fun reviewAlert(
        id: String,
        status: String,
        reason: String?,
        expectedVersion: Long
    ): Outcome<Alert> {
        val requestDto = ReviewAlertRequestDto(
            status = status,
            reason = reason,
            expectedVersion = expectedVersion
        )
        return when (val apiResult = safeApiCall(errorParser) { alertApiService.reviewAlert(id, requestDto) }) {
            is Outcome.Success -> AlertMapper.toDomain(apiResult.data)
            is Outcome.Failure -> apiResult
        }
    }
}
