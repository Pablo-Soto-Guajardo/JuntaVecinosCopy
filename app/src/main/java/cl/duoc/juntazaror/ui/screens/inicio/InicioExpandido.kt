package cl.duoc.juntazaror.ui.screens.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.juntazaror.data.model.Reserva
import cl.duoc.juntazaror.data.model.ResumenFinanciero
import cl.duoc.juntazaror.data.model.Rol
import cl.duoc.juntazaror.ui.components.TarjetaResumenFinanciero
import cl.duoc.juntazaror.ui.theme.JuntaZarorTheme

/**
 * Diseño para pantallas anchas (teléfono horizontal / tablet):
 * DOS columnas para aprovechar el espacio horizontal.
 *  - Izquierda: saludo, finanzas y accesos rápidos.
 *  - Derecha: próximos arriendos.
 */
@Composable
fun InicioExpandido(
    nombre: String,
    rol: Rol,
    resumen: ResumenFinanciero,
    proximas: List<Reserva>,
    pendientes: Int,
    acciones: AccionesInicio,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TarjetaBienvenida(nombre, rol, pendientes, acciones.onVerReservas)
            TarjetaResumenFinanciero(resumen)
            AccesosRapidos(rol, acciones)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            TarjetaProximasReservas(proximas, acciones.onVerReservas)
        }
    }
}

@Preview(name = "Expanded", widthDp = 1000, heightDp = 640, showBackground = true)
@Composable
private fun PreviewInicioExpandido() {
    JuntaZarorTheme {
        InicioExpandido(
            nombre = "Jorge Muñoz", rol = Rol.DIRECTIVA,
            resumen = ResumenFinanciero(155000, 61400), proximas = reservasDemo,
            pendientes = 2, acciones = AccionesInicio()
        )
    }
}
