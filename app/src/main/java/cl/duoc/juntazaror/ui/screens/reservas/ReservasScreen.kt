package cl.duoc.juntazaror.ui.screens.reservas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import cl.duoc.juntazaror.data.model.EstadoReserva
import cl.duoc.juntazaror.data.model.Reserva
import cl.duoc.juntazaror.data.model.Rol
import cl.duoc.juntazaror.navigation.Screen
import cl.duoc.juntazaror.ui.components.EstadoVacio
import cl.duoc.juntazaror.ui.components.MensajeExito
import cl.duoc.juntazaror.ui.components.PantallaConBarra
import cl.duoc.juntazaror.ui.components.TarjetaReserva
import cl.duoc.juntazaror.utils.Formatos
import cl.duoc.juntazaror.viewmodel.MainViewModel
import cl.duoc.juntazaror.viewmodel.ReservaViewModel

/**
 * Listado de reservas.
 * - Vecino/a: "Mis reservas" (solo las propias) y puede cancelar las pendientes.
 * - Directiva: todas las reservas y puede confirmarlas o rechazarlas.
 */
@Composable
fun ReservasScreen(reservaViewModel: ReservaViewModel, mainViewModel: MainViewModel) {
    val reservas by reservaViewModel.reservasVisibles.collectAsState()
    val filtro by reservaViewModel.filtro.collectAsState()
    val usuario by reservaViewModel.usuario.collectAsState()
    val mensaje by reservaViewModel.mensaje.collectAsState()

    val esVecino = usuario?.rol == Rol.VECINO
    val esDirectiva = usuario?.rol == Rol.DIRECTIVA

    // Reserva sobre la que se pide confirmación antes de una acción destructiva
    var reservaARechazar by remember { mutableStateOf<Reserva?>(null) }
    var reservaACancelar by remember { mutableStateOf<Reserva?>(null) }

    // Al abrir la pantalla se leen las reservas desde Room
    LaunchedEffect(Unit) { reservaViewModel.cargarDatos() }

    PantallaConBarra(
        titulo = if (esVecino) "Mis reservas" else "Reservas de la sede",
        subtitulo = if (esDirectiva) "Revisa y confirma las solicitudes" else null,
        insetsContenido = WindowInsets(0),
        botonFlotante = {
            ExtendedFloatingActionButton(
                onClick = {
                    reservaViewModel.limpiarFormulario()
                    mainViewModel.navigateTo(Screen.NuevaReserva)
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Solicitar arriendo") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            MensajeExito(mensaje)

            // ----- Filtros por estado -----
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroChip("Todas", filtro == null) { reservaViewModel.cambiarFiltro(null) }
                EstadoReserva.entries.forEach { estado ->
                    FiltroChip(estado.etiqueta, filtro == estado) { reservaViewModel.cambiarFiltro(estado) }
                }
            }

            if (reservas.isEmpty()) {
                EstadoVacio(
                    icono = Icons.Filled.EventBusy,
                    titulo = "No hay reservas para mostrar",
                    descripcion = if (esVecino) "Toca \"Solicitar arriendo\" para pedir una dependencia"
                    else "Cuando los vecinos soliciten arriendos aparecerán aquí"
                )
            } else {
                // Grilla adaptable: 1 columna en teléfono, 2 o más en pantallas anchas
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 320.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(reservas, key = { it.id }) { reserva ->
                        TarjetaReserva(
                            reserva = reserva,
                            puedeGestionar = esDirectiva,
                            puedeCancelar = reserva.usuarioId == usuario?.id && reserva.estado == EstadoReserva.PENDIENTE,
                            onConfirmar = { reservaViewModel.cambiarEstado(reserva, EstadoReserva.CONFIRMADA) },
                            onRechazar = { reservaARechazar = reserva },
                            onCancelar = { reservaACancelar = reserva },
                            // Animación al agregar, quitar o reordenar elementos de la lista
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    // ----- Diálogos de confirmación -----
    reservaARechazar?.let { reserva ->
        DialogoConfirmacion(
            titulo = "¿Rechazar solicitud?",
            texto = "${reserva.dependencia.nombre} el ${Formatos.fechaCorta(reserva.fechaEpochDay)} solicitada por ${reserva.nombreSolicitante}. El bloque quedará libre.",
            textoConfirmar = "Rechazar",
            onConfirmar = {
                reservaViewModel.cambiarEstado(reserva, EstadoReserva.RECHAZADA)
                reservaARechazar = null
            },
            onCancelar = { reservaARechazar = null }
        )
    }
    reservaACancelar?.let { reserva ->
        DialogoConfirmacion(
            titulo = "¿Cancelar tu solicitud?",
            texto = "Se eliminará la solicitud de ${reserva.dependencia.nombre} del ${Formatos.fechaCorta(reserva.fechaEpochDay)}.",
            textoConfirmar = "Sí, cancelar",
            onConfirmar = {
                reservaViewModel.cancelar(reserva)
                reservaACancelar = null
            },
            onCancelar = { reservaACancelar = null }
        )
    }
}

@Composable
private fun FiltroChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(texto) },
        leadingIcon = if (seleccionado) {
            { Icon(Icons.Filled.Check, contentDescription = null) }
        } else {
            null
        }
    )
}

@Composable
fun DialogoConfirmacion(
    titulo: String,
    texto: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Text(texto) },
        confirmButton = { TextButton(onClick = onConfirmar) { Text(textoConfirmar) } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Volver") } }
    )
}
