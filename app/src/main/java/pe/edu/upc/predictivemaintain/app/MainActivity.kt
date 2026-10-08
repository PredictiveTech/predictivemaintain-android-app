package pe.edu.upc.predictivemaintain.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import pe.edu.upc.predictivemaintain.app.navigation.AppNavigation
import pe.edu.upc.predictivemaintain.app.ui.theme.PredictiveMaintainTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PredictiveMaintainTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val session by appViewModel.session.collectAsStateWithLifecycle()
                    AppNavigation(session = session)
                }
            }
        }
    }
}
