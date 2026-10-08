package pe.edu.upc.predictivemaintain.app.maintenance.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.WorkOrderRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import javax.inject.Inject

class CreateWorkOrderUseCase @Inject constructor(
    private val workOrderRepository: WorkOrderRepository
) {
    suspend operator fun invoke(alertId: AlertId): Outcome<WorkOrder> {
        return workOrderRepository.createWorkOrder(alertId)
    }
}
