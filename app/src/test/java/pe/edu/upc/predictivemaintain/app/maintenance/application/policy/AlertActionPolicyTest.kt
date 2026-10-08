package pe.edu.upc.predictivemaintain.app.maintenance.application.policy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.iam.domain.valueobject.Role
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Alert
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertSeverity
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AlertStatus
import java.time.Instant

class AlertActionPolicyTest {

    private val policy = AlertActionPolicy()

    private val alertInReview = Alert(
        id = AlertId("a-1"),
        assetId = "ast-1",
        assetCode = "C-1",
        assetName = "Compressor",
        severity = AlertSeverity.CRITICAL,
        status = AlertStatus.IN_REVIEW,
        raisedAt = Instant.now(),
        discardReason = null,
        version = 1L,
        diagnostic = null
    )

    private val alertConfirmed = alertInReview.copy(status = AlertStatus.CONFIRMED)

    @Test
    fun `manager with IN_REVIEW alert can review`() {
        val roles = listOf(Role.MAINTENANCE_MANAGER)
        assertTrue(policy.canReview(roles, alertInReview))
    }

    @Test
    fun `technician or operator cannot review`() {
        val techRoles = listOf(Role.TECHNICIAN)
        val opRoles = listOf(Role.OPERATOR)
        assertFalse(policy.canReview(techRoles, alertInReview))
        assertFalse(policy.canReview(opRoles, alertInReview))
    }

    @Test
    fun `manager with non IN_REVIEW alert cannot review`() {
        val roles = listOf(Role.MAINTENANCE_MANAGER)
        assertFalse(policy.canReview(roles, alertConfirmed))
    }
}
