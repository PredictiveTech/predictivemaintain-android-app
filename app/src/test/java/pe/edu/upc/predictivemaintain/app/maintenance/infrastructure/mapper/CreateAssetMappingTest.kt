package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.NewAsset
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality

class CreateAssetMappingTest {

    @Test
    fun `request dto carries every field with the criticality name`() {
        val dto = AssetMapper.toRequestDto(
            NewAsset("P-1", "Pump", "Plant A", "PUMP", Criticality.CRITICAL, "Line 1", -12.0, -77.0)
        )

        assertEquals("P-1", dto.code)
        assertEquals("Pump", dto.name)
        assertEquals("Plant A", dto.location)
        assertEquals("PUMP", dto.assetType)
        assertEquals("CRITICAL", dto.criticality)
        assertEquals("Line 1", dto.productionLine)
        assertEquals(-12.0, dto.latitude!!, 0.0)
        assertEquals(-77.0, dto.longitude!!, 0.0)
    }

    @Test
    fun `optional fields stay null`() {
        val dto = AssetMapper.toRequestDto(NewAsset("P-1", "Pump", "Plant A", "PUMP", Criticality.LOW))

        assertNull(dto.productionLine)
        assertNull(dto.latitude)
        assertNull(dto.longitude)
    }

    @Test
    fun `NewAsset rejects invalid data`() {
        assertThrows(IllegalArgumentException::class.java) { NewAsset(" ", "n", "l", "t", Criticality.LOW) }
        assertThrows(IllegalArgumentException::class.java) { NewAsset("c", "n", "l", "t", Criticality.UNKNOWN) }
        assertThrows(IllegalArgumentException::class.java) { NewAsset("c", "n", "l", "t", Criticality.LOW, latitude = 1.0) }
        assertThrows(IllegalArgumentException::class.java) {
            NewAsset("c", "n", "l", "t", Criticality.LOW, latitude = 91.0, longitude = 0.0)
        }
    }
}
