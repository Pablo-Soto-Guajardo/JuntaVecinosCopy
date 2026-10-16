package cl.duoc.juntazaror.ui.screens.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.juntazaror.data.model.BloqueHorario
import cl.duoc.juntazaror.data.model.Dependencia
import cl.duoc.juntazaror.data.model.EstadoReserva
import cl.duoc.juntazaror.data.model.Reserva
import cl.duoc.juntazaror.data.model.ResumenFinanciero
import cl.duoc.juntazaror.data.model.Rol
import cl.duoc.juntazaror.ui.components.TarjetaResumenFinanciero
import cl.duoc.juntazaror.ui.theme.JuntaZarorTheme
import java.time.LocalDate

/** Diseño para teléfonos en vertical: todo en UNA columna con scroll. */
@Composable
fun InicioCompacto(
    nombre: String,
    rol: Rol,
    resumen: ResumenFinanciero,
    proximas: List<Reserva>,
    pendientes: Int,
    acciones: AccionesInicio,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TarjetaBienvenida(nombre, rol, pendientes, acciones.onVerReservas)
        TarjetaResumenFinanciero(resumen)
        AccesosRapidos(rol, acciones)
        TarjetaProximasReservas(proximas, acciones.onVerReservas)
    }
}

/** Datos ficticios solo para las vistas previas. */
internal val reservasDemo = listOf(
    Reserva(
        id = 1, usuarioId = 1, nombreSolicitante = "Camila Rojas",
        dependencia = Dependencia.SALON_PRINCIPAL, fechaEpochDay = LocalDate.now().plusDays(3).toEpochDay(),
        bloque = BloqueHorario.TARDE, motivo = "Cumpleaños", asistentes = 30, monto = 35000,
        estado = EstadoReserva.CONFIRMADA
    )
)

@Preview(name = "Compact", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PreviewInicioCompacto() {
    JuntaZarorTheme {
        InicioCompacto(
            nombre = "Camila Rojas", rol = Rol.VECINO,
            resumen = ResumenFinanciero(155000, 61400), proximas = reservasDemo,
            pendientes = 0, acciones = AccionesInicio()
        )
    }
}
