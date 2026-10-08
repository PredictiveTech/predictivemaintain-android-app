package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetStatus
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AssetDto

class AssetMapperTest {

    @Test
    fun `toDomain with valid dto succeeds and maps display-only unknown status to UNKNOWN`() {
        val dto = AssetDto(
            id = "asset-1",
            code = "AST-001",
            name = "Compressor 1",
            assetType = "ROTATING",
            criticality = "UNKNOWN_CRITICALITY",
            active = true,
            status = "INVALID_STATUS",
            sensorTypes = listOf("VIBRATION")
        )

        val result = AssetMapper.toDomain(dto)

        assertTrue(result is Outcome.Success)
        val asset = (result as Outcome.Success).data
        assertEquals("asset-1", asset.id.value)
        assertEquals("AST-001", asset.code.value)
        assertEquals("Compressor 1", asset.name)
        assertEquals(Criticality.UNKNOWN, asset.criticality)
        assertEquals(AssetStatus.UNKNOWN, asset.status)
    }

    @Test
    fun `toDomain with blank id fails with InvalidResponse`() {
        val dto = AssetDto(
            id = "",
            code = "AST-001",
            name = "Compressor 1",
            assetType = "ROTATING",
            active = true
        )

        val result = AssetMapper.toDomain(dto)

        assertTrue(result is Outcome.Failure)
        val error = (result as Outcome.Failure).error
        assertTrue(error is AppError.InvalidResponse)
        assertEquals("id", (error as AppError.InvalidResponse).field)
    }
}
