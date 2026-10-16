package cl.duoc.juntazaror.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.juntazaror.ui.screens.LoginScreen
import cl.duoc.juntazaror.ui.screens.PerfilScreen
import cl.duoc.juntazaror.ui.screens.RegistroScreen
import cl.duoc.juntazaror.ui.screens.SplashScreen
import cl.duoc.juntazaror.ui.screens.finanzas.DetalleMovimientoScreen
import cl.duoc.juntazaror.ui.screens.finanzas.FinanzasScreen
import cl.duoc.juntazaror.ui.screens.finanzas.NuevoMovimientoScreen
import cl.duoc.juntazaror.ui.screens.inicio.InicioScreen
import cl.duoc.juntazaror.ui.screens.reservas.NuevaReservaScreen
import cl.duoc.juntazaror.ui.screens.reservas.ReservasScreen
import cl.duoc.juntazaror.ui.utils.esPantallaAncha
import cl.duoc.juntazaror.utils.NotificacionHelper
import cl.duoc.juntazaror.viewmodel.AuthViewModel
import cl.duoc.juntazaror.viewmodel.FinanzasViewModel
import cl.duoc.juntazaror.viewmodel.MainViewModel
import cl.duoc.juntazaror.viewmodel.PerfilViewModel
import cl.duoc.juntazaror.viewmodel.ReservaViewModel

/** Pestañas principales de la barra de navegación. */
private data class Pestana(
    val screen: Screen,
    val titulo: String,
    val iconoSeleccionado: ImageVector,
    val icono: ImageVector
)

private val pestanas = listOf(
    Pestana(Screen.Inicio, "Inicio", Icons.Filled.Home, Icons.Outlined.Home),
    Pestana(Screen.Reservas, "Reservas", Icons.Filled.Event, Icons.Outlined.Event),
    Pestana(Screen.Finanzas, "Finanzas", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    Pestana(Screen.Perfil, "Perfil", Icons.Filled.Person, Icons.Outlined.Person)
)

private const val DURACION_TRANSICION = 350

/**
 * Grafo de navegación de la app (NavHost + NavController).
 * - Los ViewModels se crean aquí UNA vez y se COMPARTEN entre pantallas (Guía 11).
 * - Escucha los NavigationEvent emitidos por MainViewModel (Guía 10).
 * - Adaptabilidad (Guía 9): barra inferior en teléfonos, NavigationRail en pantallas anchas.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val mainViewModel: MainViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val reservaViewModel: ReservaViewModel = viewModel()
    val finanzasViewModel: FinanzasViewModel = viewModel()
    val perfilViewModel: PerfilViewModel = viewModel()

    // ----- Ejecuta los eventos de navegación emitidos por el ViewModel -----
    LaunchedEffect(Unit) {
        mainViewModel.navigationEvents.collect { evento ->
            when (evento) {
                is NavigationEvent.NavigateTo -> navController.navigate(evento.route.destino) {
                    when {
                        evento.limpiarPila -> popUpTo(navController.graph.id) { inclusive = true }
                        evento.popUpToRoute != null -> popUpTo(evento.popUpToRoute.route) {
                            inclusive = evento.inclusive
                        }
                    }
                    launchSingleTop = evento.singleTop
                }
                NavigationEvent.PopBackStack -> navController.popBackStack()
                NavigationEvent.NavigateUp -> navController.navigateUp()
            }
        }
    }

    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val enPestana = pestanas.any { it.screen.route == rutaActual }
    val usarRail = esPantallaAncha()

    // ----- Solicita permiso de notificaciones la primera vez que se entra a la app (Android 13+) -----
    val context = LocalContext.current
    var permisoSolicitado by rememberSaveable { mutableStateOf(false) }
    val lanzadorNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> perfilViewModel.cambiarNotificaciones(concedido) }
    LaunchedEffect(enPestana) {
        if (enPestana && !permisoSolicitado &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificacionHelper.tienePermiso(context)
        ) {
            permisoSolicitado = true
            lanzadorNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Row(Modifier.fillMaxSize()) {
        // Pantallas anchas: menú lateral (NavigationRail)
        if (enPestana && usarRail) {
            NavigationRail {
                pestanas.forEach { pestana ->
                    val seleccionada = rutaActual == pestana.screen.route
                    NavigationRailItem(
                        selected = seleccionada,
                        onClick = { mainViewModel.navegarPestana(pestana.screen) },
                        icon = {
                            Icon(if (seleccionada) pestana.iconoSeleccionado else pestana.icono, contentDescription = null)
                        },
                        label = { Text(pestana.titulo) }
                    )
                }
            }
        }

        Scaffold(
            modifier = Modifier.weight(1f),
            // La barra inferior reserva su propio espacio; las pantallas manejan la barra de estado
            contentWindowInsets = if (enPestana && usarRail) WindowInsets.navigationBars else WindowInsets(0),
            bottomBar = {
                // Teléfonos: barra de navegación inferior
                if (enPestana && !usarRail) {
                    NavigationBar {
                        pestanas.forEach { pestana ->
                            val seleccionada = rutaActual == pestana.screen.route
                            NavigationBarItem(
                                selected = seleccionada,
                                onClick = { mainViewModel.navegarPestana(pestana.screen) },
                                icon = {
                                    Icon(if (seleccionada) pestana.iconoSeleccionado else pestana.icono, contentDescription = null)
                                },
                                label = { Text(pestana.titulo) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.padding(padding),
                // Transiciones animadas entre pantallas
                enterTransition = {
                    fadeIn(tween(DURACION_TRANSICION)) + slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start, tween(DURACION_TRANSICION)
                    )
                },
                exitTransition = { fadeOut(tween(DURACION_TRANSICION)) },
                popEnterTransition = { fadeIn(tween(DURACION_TRANSICION)) },
                popExitTransition = {
                    fadeOut(tween(DURACION_TRANSICION)) + slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End, tween(DURACION_TRANSICION)
                    )
                }
            ) {
                composable(Screen.Splash.route) { SplashScreen(authViewModel, mainViewModel) }
                composable(Screen.Login.route) { LoginScreen(authViewModel, mainViewModel) }
                composable(Screen.Registro.route) { RegistroScreen(authViewModel, mainViewModel) }

                composable(Screen.Inicio.route) {
                    InicioScreen(reservaViewModel, finanzasViewModel, mainViewModel)
                }
                composable(Screen.Reservas.route) { ReservasScreen(reservaViewModel, mainViewModel) }
                composable(Screen.NuevaReserva.route) { NuevaReservaScreen(reservaViewModel, mainViewModel) }
                composable(Screen.Finanzas.route) { FinanzasScreen(finanzasViewModel, mainViewModel) }
                composable(Screen.NuevoMovimiento.route) { NuevoMovimientoScreen(finanzasViewModel, mainViewModel) }
                composable(
                    route = Screen.DetalleMovimiento.RUTA,
                    arguments = listOf(navArgument(Screen.DetalleMovimiento.ARGUMENTO) { type = NavType.LongType })
                ) { entrada ->
                    val id = entrada.arguments?.getLong(Screen.DetalleMovimiento.ARGUMENTO) ?: 0L
                    DetalleMovimientoScreen(id, finanzasViewModel, mainViewModel)
                }
                composable(Screen.Perfil.route) { PerfilScreen(perfilViewModel, authViewModel, mainViewModel) }
            }
        }
    }
}
