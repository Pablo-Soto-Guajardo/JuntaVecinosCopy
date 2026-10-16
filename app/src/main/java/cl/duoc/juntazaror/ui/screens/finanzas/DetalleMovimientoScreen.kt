package cl.duoc.juntazaror.ui.screens.finanzas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import cl.duoc.juntazaror.data.model.Movimiento
import cl.duoc.juntazaror.data.model.TipoMovimiento
import cl.duoc.juntazaror.ui.components.PantallaConBarra
import cl.duoc.juntazaror.ui.components.TituloSeccion
import cl.duoc.juntazaror.ui.components.VistaComprobante
import cl.duoc.juntazaror.ui.theme.ColorGasto
import cl.duoc.juntazaror.ui.theme.ColorIngreso
import cl.duoc.juntazaror.utils.Formatos
import cl.duoc.juntazaror.viewmodel.FinanzasViewModel
import cl.duoc.juntazaror.viewmodel.MainViewModel

/** Detalle de un movimiento (ruta con argumento: detalle_movimiento/{movimientoId}). */
@Composable
fun DetalleMovimientoScreen(
    movimientoId: Long,
    finanzasViewModel: FinanzasViewModel,
    mainViewModel: MainViewModel
) {
    // Se busca el movimiento en la lista ya cargada por el ViewModel
    val movimientos by finanzasViewModel.movimientos.collectAsState()
    val movimiento = movimientos.find { it.id == movimientoId }

    PantallaConBarra(
        titulo = "Detalle del movimiento",
        onVolver = { mainViewModel.navigateBack() }
    ) { padding ->
        val m = movimiento
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (m == null) {
                CircularProgressIndicator(Modifier.padding(32.dp))
            } else {
                ContenidoDetalle(m)
            }
        }
    }
}

@Composable
private fun ContenidoDetalle(movimiento: Movimiento) {
    val esIngreso = movimiento.tipo == TipoMovimiento.INGRESO
    Column(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(movimiento.tipo.etiqueta.uppercase(), style = MaterialTheme.typography.labelLarge,
                    color = if (esIngreso) ColorIngreso else ColorGasto)
                Text(
                    (if (esIngreso) "+" else "-") + Formatos.pesos(movimiento.monto),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (esIngreso) ColorIngreso else ColorGasto
                )
                HorizontalDivider()
                FilaDato("Categoría", movimiento.categoria.etiqueta)
                FilaDato("Fecha", Formatos.fechaLarga(movimiento.fechaEpochDay))
                FilaDato("Descripción", movimiento.descripcion)
                FilaDato("Registrado por", movimiento.registradoPor)
            }
        }
        TituloSeccion("Comprobante")
        // ContentScale.Fit: se ve la boleta completa, sin recortes
        VistaComprobante(uri = movimiento.comprobanteUri, alto = 360.dp, escala = ContentScale.Fit)
    }
}

@Composable
private fun FilaDato(titulo: String, valor: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            titulo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(valor, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.6f))
    }
}
