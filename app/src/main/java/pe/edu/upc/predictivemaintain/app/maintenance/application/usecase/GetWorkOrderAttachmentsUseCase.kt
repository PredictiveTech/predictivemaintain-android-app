package pe.edu.upc.predictivemaintain.app.maintenance.application.usecase

import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Evidence
import pe.edu.upc.predictivemaintain.app.maintenance.domain.repository.WorkOrderRepository
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import javax.inject.Inject

class GetWorkOrderAttachmentsUseCase @Inject constructor(
    private val workOrderRepository: WorkOrderRepository
) {
    suspend operator fun invoke(id: WorkOrderId): Outcome<List<Evidence>> {
        return workOrderRepository.listAttachments(id)
    }
}
