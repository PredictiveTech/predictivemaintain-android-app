package pe.edu.upc.predictivemaintain.app.maintenance.domain.entity

import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.EvidenceId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.WorkOrderId
import java.time.Instant

/** Photographic evidence attached to a work order (TK-19). The bytes are fetched separately, with authentication. */
data class Evidence(
    val id: EvidenceId,
    val workOrderId: WorkOrderId,
    val mimeType: String,
    val uploadedAt: Instant
)
