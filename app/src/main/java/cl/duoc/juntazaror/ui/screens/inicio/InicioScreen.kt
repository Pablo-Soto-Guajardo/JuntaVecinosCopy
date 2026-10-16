package cl.duoc.juntazaror.ui.screens.inicio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import cl.duoc.juntazaror.navigation.Screen
import cl.duoc.juntazaror.ui.components.PantallaConBarra
import cl.duoc.juntazaror.ui.utils.obtenerWindowSizeClass
import cl.duoc.juntazaror.viewmodel.FinanzasViewModel
import cl.duoc.juntazaror.viewmodel.MainViewModel
import cl.duoc.juntazaror.viewmodel.ReservaViewModel

/** Acciones de la pantalla de Inicio agrupadas en un solo objeto. */
data class AccionesInicio(
    val onNuevaReserva: () -> Unit = {},
    val onNuevoMovimiento: () -> Unit = {},
    val onVerReservas: () -> Unit = {},
    val onVerFinanzas: () -> Unit = {}
)

/**
 * Pantalla principal. Según el tamaño de la ventana (Guía 9) muestra
 * un diseño COMPACTO (una columna) o EXPANDIDO (dos columnas).
 */
@Composable
fun InicioScreen(
    reservaViewModel: ReservaViewModel,
    finanzasViewModel: FinanzasViewModel,
    mainViewModel: MainViewModel
) {
    val usuario by reservaViewModel.usuario.collectAsState()
    val resumen by finanzasViewModel.resumen.collectAsState()
    val proximas by reservaViewModel.proximasConfirmadas.collectAsState()
    val pendientes by reservaViewModel.cantidadPendientes.collectAsState()
    val windowSizeClass = obtenerWindowSizeClass()

    // Al abrir la pantalla se leen los datos actualizados desde Room
    LaunchedEffect(Unit) {
        reservaViewModel.cargarDatos()
        finanzasViewModel.cargarDatos()
    }

    val acciones = AccionesInicio(
        onNuevaReserva = {
            reservaViewModel.limpiarFormulario()
            mainViewModel.navigateTo(Screen.NuevaReserva)
        },
        onNuevoMovimiento = {
            finanzasViewModel.limpiarFormulario()
            mainViewModel.navigateTo(Screen.NuevoMovimiento)
        },
        onVerReservas = { mainViewModel.navegarPestana(Screen.Reservas) },
        onVerFinanzas = { mainViewModel.navegarPestana(Screen.Finanzas) }
    )

    PantallaConBarra(titulo = "Inicio", insetsContenido = WindowInsets(0)) { padding ->
        val u = usuario
        if (u == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> InicioCompacto(
                nombre = u.nombre, rol = u.rol, resumen = resumen, proximas = proximas,
                pendientes = pendientes, acciones = acciones, modifier = Modifier.padding(padding)
            )
            else -> InicioExpandido(
                nombre = u.nombre, rol = u.rol, resumen = resumen, proximas = proximas,
                pendientes = pendientes, acciones = acciones, modifier = Modifier.padding(padding)
            )
        }
    }
}
