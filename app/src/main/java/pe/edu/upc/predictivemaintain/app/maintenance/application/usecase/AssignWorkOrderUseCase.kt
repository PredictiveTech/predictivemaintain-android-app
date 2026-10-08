package pe.edu.upc.predictivemaintain.app.maintenance.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.WorkOrder
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.WorkOrderRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.TechnicianId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import javax.inject.Inject

class AssignWorkOrderUseCase @Inject constructor(
    private val workOrderRepository: WorkOrderRepository
) {
    suspend operator fun invoke(
        id: WorkOrderId,
        technicianId: TechnicianId,
        expectedVersion: Long
    ): Outcome<WorkOrder> {
        return workOrderRepository.assignWorkOrder(id, technicianId, expectedVersion)
    }
}
