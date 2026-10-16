package cl.duoc.juntazaror.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.duoc.juntazaror.MainActivity
import cl.duoc.juntazaror.R

/**
 * Recurso nativo: NOTIFICACIONES LOCALES.
 * El caso pide avisos de confirmación de reservas y de pagos.
 */
object NotificacionHelper {

    private const val CANAL_ID = "avisos_junta"

    /** Android 8+ exige un canal de notificaciones. Se crea una vez al iniciar la app. */
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                context.getString(R.string.canal_notificaciones),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.canal_notificaciones_desc)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    /** Android 13+ requiere permiso POST_NOTIFICATIONS concedido por el usuario. */
    fun tienePermiso(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

    /** Muestra la notificación solo si hay permiso: si no, no hace nada (no se cae la app). */
    @SuppressLint("MissingPermission") // El permiso se verifica en tienePermiso()
    fun mostrar(context: Context, titulo: String, mensaje: String) {
        if (!tienePermiso(context)) return

        // Al tocar la notificación se abre la app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notificacion)
        } catch (e: SecurityException) {
            // El usuario revocó el permiso justo antes: se ignora de forma segura
        }
    }
}
