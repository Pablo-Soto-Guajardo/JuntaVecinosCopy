package cl.duoc.juntazaror.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Componente reutilizable de la Guía 13.
 * Muestra la foto de perfil en forma CIRCULAR; si no hay imagen, muestra un ícono por defecto.
 * Crossfade anima el cambio entre el ícono y la foto.
 */
@Composable
fun ImagenInteligente(
    uri: String?,
    modifier: Modifier = Modifier,
    tamano: Dp = 128.dp,
    cargando: Boolean = false
) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = uri, label = "foto_perfil") { uriActual ->
            if (uriActual != null) {
                AsyncImage(
                    model = uriActual,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop, // Recorta para llenar el círculo sin deformar
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Sin foto de perfil",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(tamano * 0.6f)
                    )
                }
            }
        }
        if (cargando) CircularProgressIndicator()
    }
}

/**
 * Vista previa del comprobante de pago: bordes redondeados y altura fija.
 * ContentScale.Crop ajusta la foto al recuadro sin deformarla.
 */
@Composable
fun VistaComprobante(
    uri: String?,
    modifier: Modifier = Modifier,
    alto: Dp = 180.dp,
    cargando: Boolean = false,
    escala: ContentScale = ContentScale.Crop
) {
    val forma = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alto)
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = uri, label = "comprobante") { uriActual ->
            if (uriActual != null) {
                AsyncImage(
                    model = uriActual,
                    contentDescription = "Foto del comprobante",
                    contentScale = escala,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "Sin comprobante adjunto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (cargando) CircularProgressIndicator()
    }
}
