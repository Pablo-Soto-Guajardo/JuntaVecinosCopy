package cl.duoc.juntazaror.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import cl.duoc.juntazaror.R
import cl.duoc.juntazaror.navigation.Screen
import cl.duoc.juntazaror.ui.theme.JuntaZarorTheme
import cl.duoc.juntazaror.viewmodel.AuthViewModel
import cl.duoc.juntazaror.viewmodel.EstadoSesion
import cl.duoc.juntazaror.viewmodel.MainViewModel

/**
 * Pantalla de carga inicial (Guía 12, simulación de carga).
 * Mientras se lee la sesión guardada en DataStore se muestra el logo animado
 * y un CircularProgressIndicator. Luego decide si ir a Inicio o a Login.
 */
@Composable
fun SplashScreen(authViewModel: AuthViewModel, mainViewModel: MainViewModel) {
    val estado by authViewModel.estadoSesion.collectAsState()

    LaunchedEffect(estado) {
        // El ViewModel ya esperó la carga: aquí solo se decide el destino
        if (estado is EstadoSesion.Cargando) return@LaunchedEffect
        val destino = if (estado is EstadoSesion.Activa) Screen.Inicio else Screen.Login
        mainViewModel.navigateTo(destino, limpiarPila = true)
    }

    ContenidoSplash()
}

@Composable
private fun ContenidoSplash() {
    // El logo "rebota" al aparecer (spring) y se desvanece hacia visible (tween)
    var visible by remember { mutableStateOf(false) }
    val escala by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "escala_logo"
    )
    val opacidad by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700),
        label = "opacidad_logo"
    )
    LaunchedEffect(Unit) { visible = true }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_junta),
                contentDescription = "Logo Junta de Vecinos Zaror",
                modifier = Modifier
                    .size(140.dp)
                    .scale(escala)
                    .alpha(opacidad)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Junta de Vecinos Zaror",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(opacidad)
            )
            Text(
                "Arriendos y transparencia financiera",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(opacidad)
            )
            Spacer(Modifier.height(40.dp))
            CircularProgressIndicator()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSplash() {
    JuntaZarorTheme { ContenidoSplash() }
}
