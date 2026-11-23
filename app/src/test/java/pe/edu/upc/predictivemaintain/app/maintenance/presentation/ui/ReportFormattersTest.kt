package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import pe.edu.upc.predictivemaintain.app.core.ui.StatusTone
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportFormattersTest {

    @Test
    fun `duration shows seconds only under a minute`() {
        assertEquals("0 s", formatDuration(0))
        assertEquals("59 s", formatDuration(59))
    }

    @Test
    fun `duration drops zero units`() {
        assertEquals("1 min", formatDuration(60))
        assertEquals("2 h", formatDuration(7_200))
        assertEquals("2 h 5 min", formatDuration(7_500))
        assertEquals("1 d 2 h 3 min", formatDuration(93_780))
        assertEquals("3 d", formatDuration(259_200))
    }

    @Test
    fun `availability tone follows the thresholds`() {
        assertEquals(StatusTone.SUCCESS, availabilityTone(100.0))
        assertEquals(StatusTone.SUCCESS, availabilityTone(99.0))
        assertEquals(StatusTone.WARNING, availabilityTone(98.99))
        assertEquals(StatusTone.WARNING, availabilityTone(95.0))
        assertEquals(StatusTone.DANGER, availabilityTone(94.99))
    }
}
