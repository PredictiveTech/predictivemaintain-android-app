package pe.edu.upc.predictivemaintain.app.maintenance.application.policy

import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Alert
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertStatus
import javax.inject.Inject

class AlertActionPolicy @Inject constructor() {
    fun canReview(roles: List<Role>, alert: Alert): Boolean {
        val isManager = roles.contains(Role.MAINTENANCE_MANAGER)
        val isInReview = alert.status == AlertStatus.IN_REVIEW
        return isManager && isInReview
    }
}
