package pe.edu.upc.predictivemaintain.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
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
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        setContent {
            PredictiveMaintainTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val session by appViewModel.session.collectAsStateWithLifecycle()
                    NotificationPermissionRequest(isLoggedIn = session != null)
                    AppNavigation(session = session)
                }
            }
        }
    }

    /** Android 13+: asks for POST_NOTIFICATIONS once, right after login. */
    @androidx.compose.runtime.Composable
    private fun NotificationPermissionRequest(isLoggedIn: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        var alreadyAsked by rememberSaveable { mutableStateOf(false) }
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("FCM", "POST_NOTIFICATIONS permission granted=$granted")
        }
        LaunchedEffect(isLoggedIn) {
            if (!isLoggedIn || alreadyAsked) return@LaunchedEffect
            val granted = ContextCompat.checkSelfPermission(
                this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                Log.d("FCM", "POST_NOTIFICATIONS already granted")
            } else {
                alreadyAsked = true
                Log.d("FCM", "Requesting POST_NOTIFICATIONS permission")
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
