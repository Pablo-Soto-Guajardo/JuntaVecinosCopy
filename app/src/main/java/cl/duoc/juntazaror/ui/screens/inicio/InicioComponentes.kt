package cl.duoc.juntazaror.ui.screens.inicio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.juntazaror.data.model.Reserva
import cl.duoc.juntazaror.data.model.Rol
import cl.duoc.juntazaror.ui.components.FilaReservaResumen

/** Saludo + rol del usuario + aviso de solicitudes pendientes (solo Directiva). */
@Composable
fun TarjetaBienvenida(nombre: String, rol: Rol, pendientes: Int, onVerReservas: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "Hola, ${nombre.substringBefore(" ")}",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            "Perfil: ${rol.etiqueta}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AnimatedVisibility(visible = rol == Rol.DIRECTIVA && pendientes > 0) {
            Card(
                onClick = onVerReservas,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.NotificationsActive, contentDescription = null)
                    Text(
                        "  Tienes $pendientes solicitud(es) de arriendo por revisar",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/** Botones de acceso rápido. Solo se muestran las acciones permitidas para el rol. */
@Composable
fun AccesosRapidos(rol: Rol, acciones: AccionesInicio) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        AccesoRapido("Solicitar arriendo", Icons.Filled.EventAvailable, acciones.onNuevaReserva, Modifier.weight(1f))
        if (rol == Rol.VECINO) {
            AccesoRapido("Ver finanzas", Icons.Filled.PieChart, acciones.onVerFinanzas, Modifier.weight(1f))
        } else {
            AccesoRapido("Registrar movimiento", Icons.Filled.AttachMoney, acciones.onNuevoMovimiento, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AccesoRapido(texto: String, icono: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(onClick = onClick, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(texto, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}

/** Próximas reservas confirmadas de la sede: los vecinos ven cuándo está ocupada. */
@Composable
fun TarjetaProximasReservas(proximas: List<Reserva>, onVerTodas: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Próximos arriendos confirmados",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onVerTodas) { Text("Ver todo") }
            }
            if (proximas.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                    Icon(Icons.Filled.EventBusy, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    Text(
                        "  No hay arriendos confirmados próximamente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                proximas.forEach { FilaReservaResumen(it) }
            }
        }
    }
}
