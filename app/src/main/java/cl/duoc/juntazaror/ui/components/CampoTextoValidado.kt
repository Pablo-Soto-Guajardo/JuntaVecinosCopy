package cl.duoc.juntazaror.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Campo de texto REUTILIZABLE con retroalimentación visual estandarizada:
 *  - Ícono descriptivo a la izquierda (leadingIcon).
 *  - Si hay error: borde rojo (isError), ícono de error a la derecha y mensaje debajo (supportingText).
 *  - Si es contraseña: botón para mostrar/ocultar.
 * El componente NO valida: solo muestra el error que le entrega el ViewModel.
 */
@Composable
fun CampoTextoValidado(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    textoAyuda: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    accionIme: ImeAction = ImeAction.Next,
    esClave: Boolean = false,
    lineas: Int = 1,
    prefijo: String? = null,
    habilitado: Boolean = true
) {
    // rememberSaveable: el estado de "mostrar clave" sobrevive a la rotación de pantalla
    var claveVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(imageVector = icono, contentDescription = null) },
        trailingIcon = {
            when {
                esClave -> IconButton(onClick = { claveVisible = !claveVisible }) {
                    Icon(
                        imageVector = if (claveVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (claveVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                error != null -> Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Error en $etiqueta",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        prefix = if (prefijo != null) {
            { Text(prefijo) }
        } else {
            null
        },
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else if (textoAyuda != null) {
            { Text(textoAyuda) }
        } else {
            null
        },
        visualTransformation = if (esClave && !claveVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado, imeAction = accionIme),
        singleLine = lineas == 1,
        minLines = lineas,
        maxLines = if (lineas == 1) 1 else lineas + 2,
        enabled = habilitado,
        modifier = modifier.fillMaxWidth()
    )
}
