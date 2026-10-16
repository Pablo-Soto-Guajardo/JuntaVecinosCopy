package cl.duoc.juntazaror.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.juntazaror.data.model.Movimiento
import cl.duoc.juntazaror.data.model.ResumenFinanciero
import cl.duoc.juntazaror.data.model.TipoMovimiento
import cl.duoc.juntazaror.ui.theme.ColorGasto
import cl.duoc.juntazaror.ui.theme.ColorIngreso
import cl.duoc.juntazaror.utils.Formatos

/**
 * Tarjeta de resumen financiero (transparencia para los vecinos).
 * - El saldo se anima como un contador cuando cambian los montos.
 * - La barra muestra qué porcentaje de los ingresos se ha gastado.
 */
@Composable
fun TarjetaResumenFinanciero(resumen: ResumenFinanciero, modifier: Modifier = Modifier) {
    val saldoAnimado by animateFloatAsState(
        targetValue = resumen.saldo.toFloat(),
        animationSpec = tween(durationMillis = 900),
        label = "saldo"
    )
    val proporcionGasto = if (resumen.totalIngresos > 0) {
        (resumen.totalGastos.toFloat() / resumen.totalIngresos).coerceIn(0f, 1f)
    } else 0f
    val progresoAnimado by animateFloatAsState(proporcionGasto, tween(900), label = "progreso")

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Saldo disponible", style = MaterialTheme.typography.titleMedium)
            Text(
                Formatos.pesos(saldoAnimado.toLong()),
                style = MaterialTheme.typography.headlineMedium
            )
            LinearProgressIndicator(
                progress = { progresoAnimado },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                strokeCap = StrokeCap.Round
            )
            Text(
                "Se ha gastado el ${(proporcionGasto * 100).toInt()}% de los ingresos",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DatoResumen("Ingresos", resumen.totalIngresos, Icons.Filled.ArrowUpward, Modifier.weight(1f))
                DatoResumen("Gastos", resumen.totalGastos, Icons.Filled.ArrowDownward, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DatoResumen(
    titulo: String,
    monto: Long,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(titulo, style = MaterialTheme.typography.labelMedium)
            Text(Formatos.pesos(monto), style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** Fila de un movimiento: verde con flecha arriba = ingreso, rojo con flecha abajo = gasto. */
@Composable
fun TarjetaMovimiento(
    movimiento: Movimiento,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esIngreso = movimiento.tipo == TipoMovimiento.INGRESO
    val color = if (esIngreso) ColorIngreso else ColorGasto

    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (esIngreso) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    contentDescription = movimiento.tipo.etiqueta,
                    tint = color
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(movimiento.categoria.etiqueta, style = MaterialTheme.typography.titleSmall)
                Text(
                    movimiento.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        Formatos.fechaCorta(movimiento.fechaEpochDay),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (movimiento.comprobanteUri != null) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.AttachFile,
                            contentDescription = "Tiene comprobante",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Text(
                (if (esIngreso) "+" else "-") + Formatos.pesos(movimiento.monto),
                color = color,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
