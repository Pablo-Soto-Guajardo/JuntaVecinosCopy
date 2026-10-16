package cl.duoc.juntazaror.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Esquema de colores Material Design 3 (modo claro)
private val EsquemaClaro = lightColorScheme(
    primary = Verde40,
    onPrimary = Neutro99,
    primaryContainer = Verde90,
    onPrimaryContainer = Verde10,
    secondary = Azul40,
    secondaryContainer = Azul80,
    tertiary = Ambar40,
    tertiaryContainer = Ambar90,
    background = Neutro99,
    surface = Neutro99,
    surfaceVariant = Neutro95,
    onBackground = Neutro10,
    onSurface = Neutro10
)

// Esquema de colores Material Design 3 (modo oscuro)
private val EsquemaOscuro = darkColorScheme(
    primary = Verde80,
    onPrimary = Verde10,
    primaryContainer = Verde30,
    onPrimaryContainer = Verde90,
    secondary = Azul80,
    tertiary = Ambar80
)

/**
 * Tema de la app. No usa colores dinámicos (Material You) para mantener
 * una identidad visual constante en todos los dispositivos.
 */
@Composable
fun JuntaZarorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}
