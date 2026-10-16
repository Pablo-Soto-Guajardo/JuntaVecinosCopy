package cl.duoc.juntazaror.ui.screens.finanzas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import cl.duoc.juntazaror.data.model.CategoriaMovimiento
import cl.duoc.juntazaror.data.model.TipoMovimiento
import cl.duoc.juntazaror.ui.components.BotonCarga
import cl.duoc.juntazaror.ui.components.BotonesImagen
import cl.duoc.juntazaror.ui.components.CampoFecha
import cl.duoc.juntazaror.ui.components.CampoTextoValidado
import cl.duoc.juntazaror.ui.components.PantallaConBarra
import cl.duoc.juntazaror.ui.components.SelectorDesplegable
import cl.duoc.juntazaror.ui.components.TituloSeccion
import cl.duoc.juntazaror.ui.components.VistaComprobante
import cl.duoc.juntazaror.viewmodel.FinanzasViewModel
import cl.duoc.juntazaror.viewmodel.MainViewModel

/**
 * Formulario de registro de ingresos y gastos (Tesorería / Directiva).
 * Integra los recursos nativos CÁMARA y GALERÍA para adjuntar el comprobante.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoMovimientoScreen(finanzasViewModel: FinanzasViewModel, mainViewModel: MainViewModel) {
    val form by finanzasViewModel.form.collectAsState()
    val foco = LocalFocusManager.current

    PantallaConBarra(
        titulo = "Registrar movimiento",
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
                // ----- Tipo: Ingreso / Gasto (botones segmentados Material 3) -----
                TituloSeccion("Tipo de movimiento")
                val tipos = TipoMovimiento.entries
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    tipos.forEachIndexed { indice, tipo ->
                        SegmentedButton(
                            selected = form.tipo == tipo,
                            onClick = { finanzasViewModel.onTipoChange(tipo) },
                            shape = SegmentedButtonDefaults.itemShape(index = indice, count = tipos.size)
                        ) {
                            Text(tipo.etiqueta)
                        }
                    }
                }

                TituloSeccion("Detalle")
                SelectorDesplegable(
                    etiqueta = "Categoría",
                    opciones = CategoriaMovimiento.porTipo(form.tipo),
                    seleccion = form.categoria,
                    textoOpcion = { it.etiqueta },
                    onSeleccion = finanzasViewModel::onCategoriaChange,
                    icono = Icons.Filled.Category,
                    error = form.errores.categoria
                )
                CampoTextoValidado(
                    valor = form.monto,
                    onValorChange = finanzasViewModel::onMontoChange,
                    etiqueta = "Monto",
                    icono = Icons.Filled.Payments,
                    error = form.errores.monto,
                    tipoTeclado = KeyboardType.Number,
                    prefijo = "$ ",
                    textoAyuda = "Solo números, sin puntos. Montos ficticios"
                )
                CampoFecha(
                    etiqueta = "Fecha del movimiento",
                    fecha = form.fecha,
                    onFechaSeleccionada = finanzasViewModel::onFechaChange,
                    error = form.errores.fecha,
                    textoAyuda = "No puede ser una fecha futura"
                )
                CampoTextoValidado(
                    valor = form.descripcion,
                    onValorChange = finanzasViewModel::onDescripcionChange,
                    etiqueta = "Descripción",
                    icono = Icons.Filled.Description,
                    error = form.errores.descripcion,
                    lineas = 2,
                    accionIme = ImeAction.Default,
                    textoAyuda = "${form.descripcion.length}/150 caracteres"
                )

                // ----- Comprobante: cámara o galería -----
                TituloSeccion(
                    if (form.tipo == TipoMovimiento.GASTO) "Comprobante (obligatorio para gastos)"
                    else "Comprobante (opcional)"
                )
                VistaComprobante(uri = form.comprobanteUri, cargando = form.procesandoImagen)
                BotonesImagen(
                    onCrearUriCamara = finanzasViewModel::crearUriCamara,
                    onFotoTomada = finanzasViewModel::onFotoTomada,
                    onImagenGaleria = finanzasViewModel::onImagenGaleria,
                    habilitado = !form.procesandoImagen,
                    modifier = Modifier.padding(top = 8.dp)
                )
                AnimatedVisibility(visible = form.comprobanteUri != null) {
                    TextButton(
                        onClick = finanzasViewModel::quitarComprobante,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Text("  Quitar imagen")
                    }
                }
                AnimatedVisibility(visible = form.errores.comprobante != null) {
                    Text(
                        form.errores.comprobante ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                BotonCarga(
                    texto = "Guardar movimiento",
                    cargando = form.guardando,
                    onClick = {
                        foco.clearFocus()
                        finanzasViewModel.guardarMovimiento { mainViewModel.navigateBack() }
                    },
                    icono = Icons.Filled.Save,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
