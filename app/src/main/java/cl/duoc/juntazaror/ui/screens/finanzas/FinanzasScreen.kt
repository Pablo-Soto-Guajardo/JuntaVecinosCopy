package cl.duoc.juntazaror.ui.screens.finanzas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import cl.duoc.juntazaror.data.model.Movimiento
import cl.duoc.juntazaror.data.model.ResumenFinanciero
import cl.duoc.juntazaror.data.model.TipoMovimiento
import cl.duoc.juntazaror.navigation.Screen
import cl.duoc.juntazaror.ui.components.EstadoVacio
import cl.duoc.juntazaror.ui.components.MensajeExito
import cl.duoc.juntazaror.ui.components.PantallaConBarra
import cl.duoc.juntazaror.ui.components.TarjetaMovimiento
import cl.duoc.juntazaror.ui.components.TarjetaResumenFinanciero
import cl.duoc.juntazaror.viewmodel.FinanzasViewModel
import cl.duoc.juntazaror.viewmodel.MainViewModel

/**
 * Transparencia financiera: todos los perfiles ven el resumen y los movimientos.
 * Solo Tesorería y Directiva ven el botón para registrar.
 * Diseño adaptable con BoxWithConstraints: en pantallas anchas usa dos paneles.
 */
@Composable
fun FinanzasScreen(finanzasViewModel: FinanzasViewModel, mainViewModel: MainViewModel) {
    val resumen by finanzasViewModel.resumen.collectAsState()
    val movimientos by finanzasViewModel.movimientosFiltrados.collectAsState()
    val filtro by finanzasViewModel.filtroTipo.collectAsState()
    val puedeRegistrar by finanzasViewModel.puedeRegistrar.collectAsState()
    val mensaje by finanzasViewModel.mensaje.collectAsState()

    // Al abrir la pantalla se leen los movimientos desde Room
    LaunchedEffect(Unit) { finanzasViewModel.cargarDatos() }

    val abrirDetalle: (Movimiento) -> Unit = { mainViewModel.navigateTo(Screen.DetalleMovimiento(it.id)) }

    PantallaConBarra(
        titulo = "Finanzas",
        subtitulo = if (puedeRegistrar) "Registro y rendición de cuentas" else "Vista de transparencia",
        insetsContenido = WindowInsets(0),
        botonFlotante = {
            if (puedeRegistrar) {
                ExtendedFloatingActionButton(
                    onClick = {
                        finanzasViewModel.limpiarFormulario()
                        mainViewModel.navigateTo(Screen.NuevoMovimiento)
                    },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Registrar") }
                )
            }
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (maxWidth >= 700.dp) {
                // ----- Pantalla ancha: panel de resumen | lista -----
                Row(Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MensajeExito(mensaje)
                        PanelResumen(resumen, puedeRegistrar, filtro, finanzasViewModel::cambiarFiltro)
                    }
                    ListaMovimientos(
                        movimientos = movimientos,
                        onClick = abrirDetalle,
                        modifier = Modifier.weight(0.58f)
                    )
                }
            } else {
                // ----- Teléfono: todo en una sola lista -----
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item { MensajeExito(mensaje) }
                    item { PanelResumen(resumen, puedeRegistrar, filtro, finanzasViewModel::cambiarFiltro) }
                    if (movimientos.isEmpty()) {
                        item { SinMovimientos() }
                    } else {
                        items(movimientos, key = { it.id }) { movimiento ->
                            TarjetaMovimiento(
                                movimiento = movimiento,
                                onClick = { abrirDetalle(movimiento) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelResumen(
    resumen: ResumenFinanciero,
    puedeRegistrar: Boolean,
    filtro: TipoMovimiento?,
    onFiltro: (TipoMovimiento?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TarjetaResumenFinanciero(resumen)
        if (!puedeRegistrar) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text(
                    "  Información pública para todos los vecinos (montos ficticios)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChipTipo("Todos", filtro == null) { onFiltro(null) }
            ChipTipo("Ingresos", filtro == TipoMovimiento.INGRESO) { onFiltro(TipoMovimiento.INGRESO) }
            ChipTipo("Gastos", filtro == TipoMovimiento.GASTO) { onFiltro(TipoMovimiento.GASTO) }
        }
    }
}

@Composable
private fun ChipTipo(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
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
private fun ListaMovimientos(
    movimientos: List<Movimiento>,
    onClick: (Movimiento) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 8.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxHeight()
    ) {
        if (movimientos.isEmpty()) {
            item { SinMovimientos() }
        } else {
            items(movimientos, key = { it.id }) { movimiento ->
                TarjetaMovimiento(
                    movimiento = movimiento,
                    onClick = { onClick(movimiento) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                )
            }
        }
    }
}

@Composable
private fun SinMovimientos() {
    EstadoVacio(
        icono = Icons.Outlined.AccountBalanceWallet,
        titulo = "Sin movimientos",
        descripcion = "Aún no hay ingresos ni gastos registrados con este filtro"
    )
}
