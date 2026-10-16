package cl.duoc.juntazaror.ui.screens.reservas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import cl.duoc.juntazaror.data.model.BloqueHorario
import cl.duoc.juntazaror.data.model.Dependencia
import cl.duoc.juntazaror.ui.components.BotonCarga
import cl.duoc.juntazaror.ui.components.CampoFecha
import cl.duoc.juntazaror.ui.components.CampoTextoValidado
import cl.duoc.juntazaror.ui.components.PantallaConBarra
import cl.duoc.juntazaror.ui.components.SelectorDesplegable
import cl.duoc.juntazaror.utils.Formatos
import cl.duoc.juntazaror.utils.Validaciones
import cl.duoc.juntazaror.viewmodel.MainViewModel
import cl.duoc.juntazaror.viewmodel.ReservaViewModel

/** Formulario para solicitar el arriendo de una dependencia comunitaria. */
@Composable
fun NuevaReservaScreen(reservaViewModel: ReservaViewModel, mainViewModel: MainViewModel) {
    val form by reservaViewModel.form.collectAsState()
    val ocupados by reservaViewModel.bloquesOcupados.collectAsState()
    val foco = LocalFocusManager.current

    PantallaConBarra(
        titulo = "Solicitar arriendo",
        onVolver = { mainViewModel.navigateBack() }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Elige la dependencia, fecha y horario. La directiva revisará tu solicitud.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SelectorDesplegable(
                    etiqueta = "Dependencia",
                    opciones = Dependencia.entries,
                    seleccion = form.dependencia,
                    textoOpcion = { "${it.nombre} (máx. ${it.capacidad} personas)" },
                    onSeleccion = reservaViewModel::onDependenciaChange,
                    icono = Icons.Outlined.MeetingRoom,
                    error = form.errores.dependencia
                )

                CampoFecha(
                    etiqueta = "Fecha del evento",
                    fecha = form.fecha,
                    onFechaSeleccionada = reservaViewModel::onFechaChange,
                    error = form.errores.fecha,
                    textoAyuda = "Hasta ${Validaciones.DIAS_MAXIMOS_ANTICIPACION} días de anticipación"
                )

                // Los bloques ocupados aparecen deshabilitados: se previene la doble reserva
                val disponibles = BloqueHorario.entries.size - ocupados.size
                SelectorDesplegable(
                    etiqueta = "Bloque horario",
                    opciones = BloqueHorario.entries,
                    seleccion = form.bloque,
                    textoOpcion = { it.etiqueta },
                    onSeleccion = reservaViewModel::onBloqueChange,
                    icono = Icons.Filled.Schedule,
                    error = form.errores.bloque,
                    opcionHabilitada = { it !in ocupados },
                    textoAyuda = if (form.dependencia == null || form.fecha == null)
                        "Elige dependencia y fecha para ver la disponibilidad"
                    else "$disponibles de ${BloqueHorario.entries.size} bloques disponibles ese día"
                )

                CampoTextoValidado(
                    valor = form.asistentes,
                    onValorChange = reservaViewModel::onAsistentesChange,
                    etiqueta = "Cantidad de asistentes",
                    icono = Icons.Filled.Groups,
                    error = form.errores.asistentes,
                    tipoTeclado = KeyboardType.Number,
                    textoAyuda = form.dependencia?.let { "Capacidad máxima: ${it.capacidad} personas" }
                )

                CampoTextoValidado(
                    valor = form.motivo,
                    onValorChange = reservaViewModel::onMotivoChange,
                    etiqueta = "Motivo del arriendo",
                    icono = Icons.Filled.Description,
                    error = form.errores.motivo,
                    lineas = 3,
                    accionIme = ImeAction.Default,
                    textoAyuda = "${form.motivo.length}/200 caracteres"
                )

                // Tarifa referencial: aparece con animación al elegir dependencia
                AnimatedVisibility(
                    visible = form.dependencia != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null)
                            Text(
                                "  Tarifa referencial: ${Formatos.pesos(form.dependencia?.tarifaPorBloque ?: 0L)} por bloque. " +
                                    "El pago se registra en tesorería.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                BotonCarga(
                    texto = "Enviar solicitud",
                    cargando = form.guardando,
                    onClick = {
                        foco.clearFocus()
                        reservaViewModel.guardarReserva { mainViewModel.navigateBack() }
                    },
                    icono = Icons.AutoMirrored.Filled.Send,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
