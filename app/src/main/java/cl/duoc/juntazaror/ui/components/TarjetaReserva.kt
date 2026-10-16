package cl.duoc.juntazaror.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.juntazaror.data.model.EstadoReserva
import cl.duoc.juntazaror.data.model.Reserva
import cl.duoc.juntazaror.ui.theme.ColorConfirmada
import cl.duoc.juntazaror.ui.theme.ColorPendiente
import cl.duoc.juntazaror.ui.theme.ColorRechazada
import cl.duoc.juntazaror.utils.Formatos

/** Chip de estado: su color cambia con animación cuando la reserva se confirma o rechaza. */
@Composable
fun ChipEstado(estado: EstadoReserva, modifier: Modifier = Modifier) {
    val colorObjetivo = when (estado) {
        EstadoReserva.PENDIENTE -> ColorPendiente
        EstadoReserva.CONFIRMADA -> ColorConfirmada
        EstadoReserva.RECHAZADA -> ColorRechazada
    }
    val color by animateColorAsState(colorObjetivo, animationSpec = tween(600), label = "color_estado")
    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = estado.etiqueta,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

/**
 * Tarjeta de una reserva. Al tocarla se expande (animateContentSize) mostrando
 * el detalle y las acciones que permite el rol del usuario.
 */
@Composable
fun TarjetaReserva(
    reserva: Reserva,
    puedeGestionar: Boolean,      // Directiva: confirmar / rechazar
    puedeCancelar: Boolean,       // Dueño de la solicitud pendiente
    onConfirmar: () -> Unit,
    onRechazar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandida by rememberSaveable(reserva.id) { mutableStateOf(false) }
    val rotacionFlecha by animateFloatAsState(if (expandida) 180f else 0f, label = "flecha")

    Card(
        onClick = { expandida = !expandida },
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.MeetingRoom, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    reserva.dependencia.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                ChipEstado(reserva.estado)
            }
            Text(
                Formatos.fechaLarga(reserva.fechaEpochDay),
                style = MaterialTheme.typography.bodyLarge
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(reserva.bloque.etiqueta, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = if (expandida) "Ocultar detalle" else "Ver detalle",
                    modifier = Modifier.rotate(rotacionFlecha)
                )
            }

            // ----- Detalle visible solo al expandir -----
            if (expandida) {
                HorizontalDivider()
                Text("Solicitante: ${reserva.nombreSolicitante}", style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("${reserva.asistentes} asistentes", style = MaterialTheme.typography.bodyMedium)
                }
                Text("Motivo: ${reserva.motivo}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Monto referencial: ${Formatos.pesos(reserva.monto)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (puedeGestionar && reserva.estado == EstadoReserva.PENDIENTE) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onConfirmar, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null)
                            Text("  Confirmar")
                        }
                        OutlinedButton(
                            onClick = onRechazar,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Cancel, contentDescription = null)
                            Text("  Rechazar")
                        }
                    }
                }
                if (puedeCancelar) {
                    TextButton(
                        onClick = onCancelar,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Cancelar mi solicitud") }
                }
            }
        }
    }
}

/** Versión compacta para la pantalla de Inicio (ocupación de la sede, sin datos personales). */
@Composable
fun FilaReservaResumen(reserva: Reserva, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.MeetingRoom, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(reserva.dependencia.nombre, style = MaterialTheme.typography.titleSmall)
            Text(
                "${Formatos.fechaLarga(reserva.fechaEpochDay)} · ${reserva.bloque.etiqueta}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ChipEstado(reserva.estado)
    }
}
