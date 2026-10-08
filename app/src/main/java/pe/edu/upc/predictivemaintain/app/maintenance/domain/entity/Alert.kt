package pe.edu.upc.predictivemaintain.app.maintenance.domain.entity

import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertDiagnostic
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertStatus
import java.time.Instant

data class Alert(
    val id: AlertId,
    val assetId: String,
    val assetCode: String,
    val assetName: String,
    val severity: AlertSeverity,
    val status: AlertStatus,
    val raisedAt: Instant,
    val discardReason: String?,
    val version: Long,
    val diagnostic: AlertDiagnostic?
)
