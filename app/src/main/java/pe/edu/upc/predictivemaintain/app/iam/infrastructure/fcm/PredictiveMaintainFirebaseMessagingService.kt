package pe.edu.upc.predictivemaintain.app.iam.infrastructure.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pe.edu.upc.predictivemaintain.app.MainActivity
import pe.edu.upc.predictivemaintain.app.R
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.local.SessionDataStore
import javax.inject.Inject

@AndroidEntryPoint
class PredictiveMaintainFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var sessionDataStore: SessionDataStore

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            try {
                sessionDataStore.saveFcmToken(token)
            } catch (_: Exception) {
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        showNotification(message)
    }

    private fun showNotification(message: RemoteMessage) {
        val channelId = "predictivemaintain_alerts"
        val channelName = "Predictive Maintain Alerts"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de alertas y mantenimiento predictivo"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = message.notification?.title ?: message.data["title"] ?: "PredictiveMaintain Alerta"
        val body = message.notification?.body ?: message.data["body"] ?: "Nueva notificación recibida"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
