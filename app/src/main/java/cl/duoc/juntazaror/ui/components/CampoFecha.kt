package cl.duoc.juntazaror.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cl.duoc.juntazaror.utils.Formatos
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Campo de fecha con calendario Material 3 (DatePickerDialog).
 * La validación de la fecha la hace el ViewModel; aquí solo se muestra el error.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    etiqueta: String,
    fecha: LocalDate?,
    onFechaSeleccionada: (LocalDate) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
    textoAyuda: String? = null
) {
    var mostrarCalendario by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = fecha?.let { Formatos.fechaCorta(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            placeholder = { Text("dd-mm-aaaa") },
            leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
            trailingIcon = {
                if (error != null) Icon(Icons.Filled.Error, "Error en $etiqueta", tint = MaterialTheme.colorScheme.error)
            },
            isError = error != null,
            supportingText = if (error != null) {
                { Text(error) }
            } else if (textoAyuda != null) {
                { Text(textoAyuda) }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente que captura el toque y abre el calendario
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { mostrarCalendario = true }
        )
    }

    if (mostrarCalendario) {
        // El DatePicker trabaja en milisegundos UTC
        val estadoCalendario = rememberDatePickerState(
            initialSelectedDateMillis = (fecha ?: LocalDate.now())
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoCalendario.selectedDateMillis?.let { millis ->
                        onFechaSeleccionada(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    mostrarCalendario = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estadoCalendario)
        }
    }
}
