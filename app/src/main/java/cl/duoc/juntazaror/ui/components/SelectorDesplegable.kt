package cl.duoc.juntazaror.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Lista desplegable genérica <T> con la misma retroalimentación visual que CampoTextoValidado.
 * opcionHabilitada permite deshabilitar opciones (ej: bloques horarios ya ocupados).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectorDesplegable(
    etiqueta: String,
    opciones: List<T>,
    seleccion: T?,
    textoOpcion: (T) -> String,
    onSeleccion: (T) -> Unit,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    opcionHabilitada: (T) -> Boolean = { true },
    textoAyuda: String? = null
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = seleccion?.let(textoOpcion) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            leadingIcon = { Icon(imageVector = icono, contentDescription = null) },
            trailingIcon = {
                if (error != null) {
                    Icon(Icons.Filled.Error, contentDescription = "Error en $etiqueta", tint = MaterialTheme.colorScheme.error)
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
                }
            },
            isError = error != null,
            supportingText = if (error != null) {
                { Text(error) }
            } else if (textoAyuda != null) {
                { Text(textoAyuda) }
            } else {
                null
            },
            singleLine = true,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                val habilitada = opcionHabilitada(opcion)
                DropdownMenuItem(
                    text = {
                        Text(if (habilitada) textoOpcion(opcion) else "${textoOpcion(opcion)} (ocupado)")
                    },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    },
                    enabled = habilitada
                )
            }
        }
    }
}
