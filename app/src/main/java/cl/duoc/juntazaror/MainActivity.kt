package cl.duoc.juntazaror

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import cl.duoc.juntazaror.navigation.AppNavigation
import cl.duoc.juntazaror.ui.theme.JuntaZarorTheme
import cl.duoc.juntazaror.utils.NotificacionHelper

/**
 * Punto de entrada de la aplicación.
 * Solo aplica el tema y delega todo a AppNavigation (la Activity no tiene lógica).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Canal de notificaciones (obligatorio desde Android 8)
        NotificacionHelper.crearCanal(this)
        setContent {
            JuntaZarorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
