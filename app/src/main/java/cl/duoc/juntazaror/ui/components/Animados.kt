package cl.duoc.juntazaror.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Botón principal con estado de carga ANIMADO:
 * al guardar, el texto se transforma en un indicador de progreso (AnimatedContent)
 * y el botón se deshabilita para evitar envíos duplicados.
 */
@Composable
fun BotonCarga(
    texto: String,
    cargando: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = !cargando,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        AnimatedContent(
            targetState = cargando,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "boton_carga"
        ) { estaCargando ->
            if (estaCargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icono != null) Icon(icono, contentDescription = null)
                    Text(texto, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/**
 * Banner de éxito que aparece deslizándose desde arriba y se oculta solo
 * (patrón AnimatedVisibility de la Guía 12). El ViewModel controla cuándo se muestra.
 */
@Composable
fun MensajeExito(mensaje: String?, modifier: Modifier = Modifier) {
    // Se recuerda el último texto para que no desaparezca durante la animación de salida
    var ultimoTexto by remember { mutableStateOf("") }
    if (mensaje != null) ultimoTexto = mensaje

    AnimatedVisibility(
        visible = mensaje != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Text(ultimoTexto, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Tarjeta de error general (ej: credenciales incorrectas) que se expande/contrae animada. */
@Composable
fun MensajeError(mensaje: String?, modifier: Modifier = Modifier) {
    var ultimoTexto by remember { mutableStateOf("") }
    if (mensaje != null) ultimoTexto = mensaje

    AnimatedVisibility(
        visible = mensaje != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Filled.ErrorOutline, contentDescription = null)
                Text(ultimoTexto, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
