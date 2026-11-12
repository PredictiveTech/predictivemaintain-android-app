package pe.edu.upc.predictivemaintain.app.maintenance.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import org.junit.Assert.assertSame
import org.junit.Test

class AssetTypeIconTest {

    @Test
    fun `known types resolve to their icon`() {
        assertSame(Icons.Filled.Settings, assetTypeIcon("COMPRESSOR"))
        assertSame(Icons.Filled.Build, assetTypeIcon("EXTRUDER"))
        assertSame(Icons.Filled.Refresh, assetTypeIcon("MOTOR"))
        assertSame(Icons.Filled.Refresh, assetTypeIcon("PUMP"))
        assertSame(Icons.Filled.ArrowForward, assetTypeIcon("CONVEYOR"))
        assertSame(Icons.Filled.ArrowForward, assetTypeIcon("FAJA"))
    }

    @Test
    fun `matching ignores case, spaces and variants`() {
        assertSame(Icons.Filled.Refresh, assetTypeIcon(" centrifugal_pump "))
        assertSame(Icons.Filled.ArrowForward, assetTypeIcon("Faja transportadora"))
    }

    @Test
    fun `unknown or blank types use the generic icon`() {
        assertSame(Icons.Filled.Info, assetTypeIcon("BOILER"))
        assertSame(Icons.Filled.Info, assetTypeIcon(""))
    }
}
