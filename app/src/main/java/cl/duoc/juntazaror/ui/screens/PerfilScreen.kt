package cl.duoc.juntazaror.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import cl.duoc.juntazaror.data.model.PerfilEdicionState
import cl.duoc.juntazaror.data.model.Usuario
import cl.duoc.juntazaror.navigation.Screen
import cl.duoc.juntazaror.ui.components.BotonCarga
import cl.duoc.juntazaror.ui.components.BotonesImagen
import cl.duoc.juntazaror.ui.components.CampoTextoValidado
import cl.duoc.juntazaror.ui.components.ImagenInteligente
import cl.duoc.juntazaror.ui.components.MensajeExito
import cl.duoc.juntazaror.ui.components.PantallaConBarra
import cl.duoc.juntazaror.utils.NotificacionHelper
import cl.duoc.juntazaror.viewmodel.AuthViewModel
import cl.duoc.juntazaror.viewmodel.MainViewModel
import cl.duoc.juntazaror.viewmodel.PerfilViewModel

/** Perfil del usuario: foto (cámara/galería), datos editables, notificaciones y cierre de sesión. */
@Composable
fun PerfilScreen(
    perfilViewModel: PerfilViewModel,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel
) {
    val usuario by perfilViewModel.usuario.collectAsState()
    val notificaciones by perfilViewModel.notificacionesActivas.collectAsState()
    val edicion by perfilViewModel.edicion.collectAsState()
    val mensaje by perfilViewModel.mensaje.collectAsState()
    val context = LocalContext.current

    var confirmarSalida by rememberSaveable { mutableStateOf(false) }

    // Al abrir la pantalla se cargan los datos desde Room y DataStore
    LaunchedEffect(Unit) { perfilViewModel.cargarDatos() }

    // Permiso de notificaciones (Android 13+): si lo niega, la preferencia queda desactivada
    val lanzadorPermisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> perfilViewModel.cambiarNotificaciones(concedido) }

    PantallaConBarra(
        titulo = "Mi perfil",
        insetsContenido = WindowInsets(0),
        acciones = {
            IconButton(onClick = { confirmarSalida = true }) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
            }
        }
    ) { padding ->
        val u = usuario
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (u == null) {
                CircularProgressIndicator(Modifier.padding(32.dp))
            } else {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MensajeExito(mensaje)

                    // ----- Foto de perfil (Guía 13) -----
                    ImagenInteligente(uri = u.fotoUri)
                    Text(u.nombre, style = MaterialTheme.typography.titleLarge)
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            u.rol.etiqueta,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                    BotonesImagen(
                        onCrearUriCamara = perfilViewModel::crearUriCamara,
                        onFotoTomada = perfilViewModel::onFotoTomada,
                        onImagenGaleria = perfilViewModel::onImagenGaleria
                    )

                    // ----- Datos: modo lectura <-> modo edición (transición animada) -----
                    Card(Modifier.fillMaxWidth()) {
                        AnimatedContent(
                            targetState = edicion.editando,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "modo_edicion"
                        ) { editando ->
                            if (editando) {
                                FormularioEdicion(edicion, perfilViewModel)
                            } else {
                                DatosUsuario(u, onEditar = perfilViewModel::iniciarEdicion)
                            }
                        }
                    }

                    // ----- Preferencias guardadas en DataStore -----
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Notificaciones", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Avisos de reservas y pagos registrados",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = notificaciones,
                                onCheckedChange = { activar ->
                                    val faltaPermiso = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        !NotificacionHelper.tienePermiso(context)
                                    if (activar && faltaPermiso) {
                                        lanzadorPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        perfilViewModel.cambiarNotificaciones(activar)
                                    }
                                }
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { confirmarSalida = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Text("  Cerrar sesión")
                    }
                }
            }
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tus datos quedan guardados en el dispositivo.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    authViewModel.cerrarSesion {
                        mainViewModel.navigateTo(Screen.Login, limpiarPila = true)
                    }
                }) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun DatosUsuario(usuario: Usuario, onEditar: () -> Unit) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Mis datos", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = onEditar) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("  Editar")
            }
        }
        FilaPerfil(Icons.Filled.Badge, "RUT", usuario.rut)
        FilaPerfil(Icons.Filled.Email, "Correo", usuario.correo)
        FilaPerfil(Icons.Filled.Phone, "Teléfono", "+56 ${usuario.telefono}")
        FilaPerfil(Icons.Filled.Home, "Dirección", usuario.direccion)
    }
}

@Composable
private fun FilaPerfil(icono: ImageVector, titulo: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun FormularioEdicion(estado: PerfilEdicionState, viewModel: PerfilViewModel) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Editar datos", style = MaterialTheme.typography.titleMedium)
        CampoTextoValidado(
            valor = estado.nombre, onValorChange = viewModel::onNombreChange,
            etiqueta = "Nombre completo", icono = Icons.Filled.Person, error = estado.errores.nombre
        )
        CampoTextoValidado(
            valor = estado.telefono, onValorChange = viewModel::onTelefonoChange,
            etiqueta = "Teléfono móvil", icono = Icons.Filled.Phone, error = estado.errores.telefono,
            tipoTeclado = KeyboardType.Phone, prefijo = "+56 "
        )
        CampoTextoValidado(
            valor = estado.direccion, onValorChange = viewModel::onDireccionChange,
            etiqueta = "Dirección", icono = Icons.Filled.Home, error = estado.errores.direccion
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = viewModel::cancelarEdicion, modifier = Modifier.weight(1f)) { Text("Cancelar") }
            BotonCarga(
                texto = "Guardar",
                cargando = estado.guardando,
                onClick = viewModel::guardarCambios,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
