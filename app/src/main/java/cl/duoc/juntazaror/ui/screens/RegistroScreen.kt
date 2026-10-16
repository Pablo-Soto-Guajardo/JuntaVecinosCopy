package cl.duoc.juntazaror.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import cl.duoc.juntazaror.data.model.RegistroErrores
import cl.duoc.juntazaror.data.model.RegistroUiState
import cl.duoc.juntazaror.navigation.Screen
import cl.duoc.juntazaror.ui.components.BotonCarga
import cl.duoc.juntazaror.ui.components.CampoTextoValidado
import cl.duoc.juntazaror.ui.components.PantallaConBarra
import cl.duoc.juntazaror.ui.theme.JuntaZarorTheme
import cl.duoc.juntazaror.viewmodel.AuthViewModel
import cl.duoc.juntazaror.viewmodel.MainViewModel

/** Pantalla de registro de vecinos/as. */
@Composable
fun RegistroScreen(authViewModel: AuthViewModel, mainViewModel: MainViewModel) {
    val estado by authViewModel.registro.collectAsState()
    val foco = LocalFocusManager.current

    PantallaConBarra(
        titulo = "Crear cuenta",
        subtitulo = "Registro de vecino/a",
        onVolver = {
            authViewModel.limpiarRegistro()
            mainViewModel.navigateBack()
        }
    ) { padding ->
        FormularioRegistro(
            estado = estado,
            onNombreChange = authViewModel::onNombreChange,
            onRutChange = authViewModel::onRutChange,
            onCorreoChange = authViewModel::onCorreoChange,
            onTelefonoChange = authViewModel::onTelefonoChange,
            onDireccionChange = authViewModel::onDireccionChange,
            onClaveChange = authViewModel::onClaveChange,
            onConfirmarClaveChange = authViewModel::onConfirmarClaveChange,
            onTerminosChange = authViewModel::onTerminosChange,
            onRegistrar = {
                foco.clearFocus()
                authViewModel.registrar {
                    mainViewModel.navigateTo(Screen.Inicio, limpiarPila = true)
                }
            },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun FormularioRegistro(
    estado: RegistroUiState,
    onNombreChange: (String) -> Unit,
    onRutChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onDireccionChange: (String) -> Unit,
    onClaveChange: (String) -> Unit,
    onConfirmarClaveChange: (String) -> Unit,
    onTerminosChange: (Boolean) -> Unit,
    onRegistrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Completa tus datos. Todos los campos son obligatorios.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            CampoTextoValidado(
                valor = estado.nombre, onValorChange = onNombreChange,
                etiqueta = "Nombre completo", icono = Icons.Filled.Person, error = estado.errores.nombre
            )
            CampoTextoValidado(
                valor = estado.rut, onValorChange = onRutChange,
                etiqueta = "RUT", icono = Icons.Filled.Badge, error = estado.errores.rut,
                textoAyuda = "Con dígito verificador. Ej: 12.345.678-5"
            )
            CampoTextoValidado(
                valor = estado.correo, onValorChange = onCorreoChange,
                etiqueta = "Correo electrónico", icono = Icons.Filled.Email, error = estado.errores.correo,
                tipoTeclado = KeyboardType.Email
            )
            CampoTextoValidado(
                valor = estado.telefono, onValorChange = onTelefonoChange,
                etiqueta = "Teléfono móvil", icono = Icons.Filled.Phone, error = estado.errores.telefono,
                tipoTeclado = KeyboardType.Phone, prefijo = "+56 ",
                textoAyuda = "9 dígitos, comenzando en 9"
            )
            CampoTextoValidado(
                valor = estado.direccion, onValorChange = onDireccionChange,
                etiqueta = "Dirección", icono = Icons.Filled.Home, error = estado.errores.direccion,
                textoAyuda = "Calle y número. Ej: Los Jazmines 425"
            )
            CampoTextoValidado(
                valor = estado.clave, onValorChange = onClaveChange,
                etiqueta = "Contraseña", icono = Icons.Filled.Lock, error = estado.errores.clave,
                tipoTeclado = KeyboardType.Password, esClave = true,
                textoAyuda = "Mínimo 6 caracteres, con letras y números"
            )
            CampoTextoValidado(
                valor = estado.confirmarClave, onValorChange = onConfirmarClaveChange,
                etiqueta = "Repetir contraseña", icono = Icons.Outlined.Lock, error = estado.errores.confirmarClave,
                tipoTeclado = KeyboardType.Password, esClave = true, accionIme = ImeAction.Done
            )

            // ----- Aceptación de uso de datos (checkbox con error propio) -----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = estado.aceptaTerminos,
                    onCheckedChange = onTerminosChange,
                    colors = CheckboxDefaults.colors(
                        uncheckedColor = if (estado.errores.terminos != null) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    "Acepto que la Junta use mis datos solo para gestionar reservas",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            AnimatedVisibility(visible = estado.errores.terminos != null) {
                Text(
                    estado.errores.terminos ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            BotonCarga(
                texto = "Crear cuenta",
                cargando = estado.cargando,
                onClick = onRegistrar,
                icono = Icons.Filled.PersonAdd,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Preview(name = "Registro con errores", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun PreviewRegistro() {
    JuntaZarorTheme {
        FormularioRegistro(
            estado = RegistroUiState(
                nombre = "Ana", rut = "12.345.678-9",
                errores = RegistroErrores(rut = "El dígito verificador no corresponde", terminos = "Debes aceptar el uso de tus datos para continuar")
            ),
            onNombreChange = {}, onRutChange = {}, onCorreoChange = {}, onTelefonoChange = {},
            onDireccionChange = {}, onClaveChange = {}, onConfirmarClaveChange = {},
            onTerminosChange = {}, onRegistrar = {}
        )
    }
}
