package cl.duoc.juntazaror.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import cl.duoc.juntazaror.R
import cl.duoc.juntazaror.data.model.LoginErrores
import cl.duoc.juntazaror.data.model.LoginUiState
import cl.duoc.juntazaror.navigation.Screen
import cl.duoc.juntazaror.ui.components.BotonCarga
import cl.duoc.juntazaror.ui.components.CampoTextoValidado
import cl.duoc.juntazaror.ui.components.MensajeError
import cl.duoc.juntazaror.ui.theme.JuntaZarorTheme
import cl.duoc.juntazaror.viewmodel.AuthViewModel
import cl.duoc.juntazaror.viewmodel.MainViewModel

/** Pantalla de inicio de sesión: conecta el ViewModel con el formulario (sin lógica aquí). */
@Composable
fun LoginScreen(authViewModel: AuthViewModel, mainViewModel: MainViewModel) {
    val estado by authViewModel.login.collectAsState()
    val foco = LocalFocusManager.current

    FormularioLogin(
        estado = estado,
        onCorreoChange = authViewModel::onLoginCorreoChange,
        onClaveChange = authViewModel::onLoginClaveChange,
        onIngresar = {
            foco.clearFocus()
            authViewModel.iniciarSesion {
                mainViewModel.navigateTo(Screen.Inicio, limpiarPila = true)
            }
        },
        onRegistrarse = { mainViewModel.navigateTo(Screen.Registro) }
    )
}

/** Composable "sin estado": recibe los datos y avisa los eventos (fácil de previsualizar). */
@Composable
fun FormularioLogin(
    estado: LoginUiState,
    onCorreoChange: (String) -> Unit,
    onClaveChange: (String) -> Unit,
    onIngresar: () -> Unit,
    onRegistrarse: () -> Unit
) {
    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp) // En tablets el formulario no se estira de más
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ----- Encabezado (jerarquía visual: logo > título > subtítulo) -----
                Image(
                    painter = painterResource(R.drawable.logo_junta),
                    contentDescription = "Logo de la Junta de Vecinos",
                    modifier = Modifier.size(96.dp)
                )
                Text(
                    "Junta de Vecinos Zaror",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Ingresa para gestionar arriendos y revisar las finanzas de tu comunidad",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // ----- Formulario -----
                CampoTextoValidado(
                    valor = estado.correo,
                    onValorChange = onCorreoChange,
                    etiqueta = "Correo electrónico",
                    icono = Icons.Filled.Email,
                    error = estado.errores.correo,
                    tipoTeclado = KeyboardType.Email
                )
                CampoTextoValidado(
                    valor = estado.clave,
                    onValorChange = onClaveChange,
                    etiqueta = "Contraseña",
                    icono = Icons.Filled.Lock,
                    error = estado.errores.clave,
                    tipoTeclado = KeyboardType.Password,
                    accionIme = ImeAction.Done,
                    esClave = true
                )

                MensajeError(estado.errorGeneral)

                BotonCarga(
                    texto = "Ingresar",
                    cargando = estado.cargando,
                    onClick = onIngresar,
                    icono = Icons.AutoMirrored.Filled.Login
                )
                TextButton(onClick = onRegistrarse) {
                    Text("¿No tienes cuenta? Regístrate como vecino/a")
                }
            }
        }
    }
}

@Preview(name = "Login", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewLogin() {
    JuntaZarorTheme {
        FormularioLogin(
            estado = LoginUiState(
                correo = "vecino@",
                errores = LoginErrores(correo = "Formato de correo inválido (ej: nombre@correo.cl)")
            ),
            onCorreoChange = {}, onClaveChange = {}, onIngresar = {}, onRegistrarse = {}
        )
    }
}
