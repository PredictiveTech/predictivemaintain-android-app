package pe.edu.upc.predictivemaintain.app.telemetry.infrastructure.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.CommunicationStatus
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.RangeStatus
import pe.edu.upc.predictivemaintain.app.telemetry.domain.valueobject.SensorMetric
import pe.edu.upc.predictivemaintain.app.telemetry.infrastructure.remote.SensorItemDto
import pe.edu.upc.predictivemaintain.app.telemetry.infrastructure.remote.SensorReadingDto

class SensorPanelMapperTest {

    @Test
    fun `toDomain with valid sensor dto succeeds and maps unknown metric status to UNKNOWN`() {
        val dtoList = listOf(
            SensorItemDto(
                id = "sensor-1",
                metric = "UNKNOWN_METRIC",
                unit = "Hz",
                communication = "INVALID_COMM",
                latestReading = null,
                threshold = null,
                rangeStatus = "INVALID_RANGE"
            )
        )

        val result = SensorPanelMapper.toDomain(dtoList)

        assertTrue(result is Outcome.Success)
        val items = (result as Outcome.Success).data
        assertEquals(1, items.size)
        val sensor = items[0]
        assertEquals("sensor-1", sensor.id.value)
        assertEquals(SensorMetric.UNKNOWN, sensor.metric)
        assertEquals(CommunicationStatus.UNKNOWN, sensor.communication)
        assertEquals(RangeStatus.UNKNOWN, sensor.rangeStatus)
        assertNull(sensor.latestReading)
        assertNull(sensor.threshold)
    }

    @Test
    fun `toDomain with invalid date time fails with InvalidResponse`() {
        val dtoList = listOf(
            SensorItemDto(
                id = "sensor-1",
                metric = "VIBRATION",
                unit = "Hz",
                latestReading = SensorReadingDto(
                    value = 12.5,
                    measuredAt = "invalid-date"
                )
            )
        )

        val result = SensorPanelMapper.toDomain(dtoList)

        assertTrue(result is Outcome.Failure)
        val error = (result as Outcome.Failure).error
        assertTrue(error is AppError.InvalidResponse)
        assertEquals("measuredAt", (error as AppError.InvalidResponse).field)
    }
}
