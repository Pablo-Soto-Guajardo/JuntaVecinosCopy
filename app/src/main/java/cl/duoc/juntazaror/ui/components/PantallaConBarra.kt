package cl.duoc.juntazaror.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Estructura base de todas las pantallas internas (Scaffold + TopAppBar).
 * Mantiene la coherencia visual: misma barra superior, mismo color, mismo botón volver.
 *
 * @param onVolver si no es null, muestra la flecha para volver atrás.
 * @param insetsContenido en las pestañas principales se pasa WindowInsets(0) porque
 *        la barra inferior de navegación ya reserva el espacio del sistema.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConBarra(
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    onVolver: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {},
    botonFlotante: @Composable () -> Unit = {},
    insetsContenido: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    contenido: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(titulo, style = MaterialTheme.typography.titleLarge)
                        if (subtitulo != null) {
                            Text(subtitulo, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                },
                navigationIcon = {
                    if (onVolver != null) {
                        IconButton(onClick = onVolver) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = acciones,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = botonFlotante,
        contentWindowInsets = insetsContenido,
        content = contenido
    )
}
