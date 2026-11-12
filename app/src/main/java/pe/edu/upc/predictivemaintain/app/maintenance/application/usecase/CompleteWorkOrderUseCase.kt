package pe.edu.upc.predictivemaintain.app.maintenance.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.WorkOrderRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import javax.inject.Inject

class CompleteWorkOrderUseCase @Inject constructor(
    private val workOrderRepository: WorkOrderRepository
) {
    suspend operator fun invoke(
        id: WorkOrderId,
        summary: String,
        expectedVersion: Long
    ): Outcome<WorkOrder> {
        val trimmed = summary.trim()
        if (trimmed.isBlank() || trimmed.length > 2000) {
            return Outcome.Failure(AppError.InvalidResponse("summary"))
        }
        return workOrderRepository.completeWorkOrder(id, trimmed, expectedVersion)
    }
}
