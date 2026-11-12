package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.WaterDrop
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

class AssetTypeIconTest {

    @Test
    fun `known types resolve to their icon`() {
        assertSame(Icons.Filled.Air, assetTypeIcon("COMPRESSOR"))
        assertSame(Icons.Filled.PrecisionManufacturing, assetTypeIcon("EXTRUDER"))
        assertSame(Icons.Filled.Bolt, assetTypeIcon("MOTOR"))
        assertSame(Icons.Filled.Bolt, assetTypeIcon("ROTATING"))
        assertSame(Icons.Filled.WaterDrop, assetTypeIcon("PUMP"))
        assertSame(Icons.Filled.Inventory2, assetTypeIcon("CONVEYOR"))
        assertSame(Icons.Filled.Inventory2, assetTypeIcon("FAJA"))
    }

    @Test
    fun `every known type has its own distinct icon`() {
        val icons = listOf("COMPRESSOR", "EXTRUDER", "MOTOR", "PUMP", "CONVEYOR").map { assetTypeIcon(it) }
        assertSame(icons.size, icons.toSet().size)
        icons.forEach { assertNotSame(Icons.Filled.Info, it) }
    }

    @Test
    fun `matching ignores case, spaces and variants`() {
        assertSame(Icons.Filled.WaterDrop, assetTypeIcon(" centrifugal_pump "))
        assertSame(Icons.Filled.Inventory2, assetTypeIcon("Faja transportadora"))
        assertSame(Icons.Filled.Air, assetTypeIcon("Compresor"))
    }

    @Test
    fun `unknown or blank types use the generic icon`() {
        assertSame(Icons.Filled.Info, assetTypeIcon("BOILER"))
        assertSame(Icons.Filled.Info, assetTypeIcon(""))
    }
}
