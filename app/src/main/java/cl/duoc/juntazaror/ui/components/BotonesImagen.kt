package cl.duoc.juntazaror.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * RECURSOS NATIVOS: CÁMARA + GALERÍA (Guía 13, API ActivityResult).
 *
 * Flujo de la cámara:
 *  1. Se verifica si el permiso CAMERA está concedido.
 *  2. Si no, se solicita en tiempo de ejecución (RequestPermission).
 *  3. Con permiso, el ViewModel crea un Uri seguro (FileProvider) y se abre TakePicture().
 *  4. El resultado (true/false) vuelve al ViewModel. Si el usuario cancela, no pasa nada.
 *
 * Galería: GetContent("image/…") no requiere permiso en tiempo de ejecución.
 */
@Composable
fun BotonesImagen(
    onCrearUriCamara: () -> Uri,
    onFotoTomada: (Boolean) -> Unit,
    onImagenGaleria: (Uri?) -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    val context = LocalContext.current
    var permisoDenegado by rememberSaveable { mutableStateOf(false) }

    // Lanzador de la cámara: guarda la foto en el Uri entregado
    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito -> onFotoTomada(exito) }

    // Lanzador del permiso de cámara
    val lanzadorPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoDenegado = !concedido
        if (concedido) lanzadorCamara.launch(onCrearUriCamara())
    }

    // Lanzador de la galería
    val lanzadorGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> onImagenGaleria(uri) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    val tienePermiso = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (tienePermiso) {
                        lanzadorCamara.launch(onCrearUriCamara())
                    } else {
                        lanzadorPermiso.launch(Manifest.permission.CAMERA)
                    }
                },
                enabled = habilitado,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Text("  Cámara")
            }
            OutlinedButton(
                onClick = { lanzadorGaleria.launch("image/*") },
                enabled = habilitado,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                Text("  Galería")
            }
        }
        // Aviso visible si el usuario rechazó el permiso
        AnimatedVisibility(visible = permisoDenegado) {
            Text(
                text = "Sin permiso de cámara no se pueden tomar fotos. Puedes usar la galería o habilitarlo en Ajustes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
