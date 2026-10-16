package cl.duoc.juntazaror.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Manejo de archivos de imagen (recurso nativo cámara / galería).
 * Todas las imágenes se guardan en la carpeta PRIVADA de la app (filesDir/imagenes),
 * así ninguna otra app puede leerlas y siguen disponibles al reiniciar la app.
 */
object ImagenUtils {

    private fun carpetaImagenes(context: Context): File =
        File(context.filesDir, "imagenes").apply { if (!exists()) mkdirs() }

    private fun autoridad(context: Context) = "${context.packageName}.fileprovider"

    /**
     * Crea un Uri content:// (vía FileProvider) donde la app de cámara guardará la foto.
     * FileProvider evita exponer rutas file:// del sistema de archivos (medida de seguridad).
     */
    fun crearUriParaCamara(context: Context, prefijo: String): Uri {
        val archivo = File(carpetaImagenes(context), "${prefijo}_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, autoridad(context), archivo)
    }

    /**
     * La galería entrega un Uri con permiso TEMPORAL. Para que la imagen siga
     * visible después de cerrar la app, se copia a la carpeta privada.
     * Retorna null si no se pudo leer la imagen (la app no se cae).
     */
    fun copiarAAlmacenamientoInterno(context: Context, origen: Uri, prefijo: String): Uri? =
        try {
            val destino = File(carpetaImagenes(context), "${prefijo}_${System.currentTimeMillis()}.jpg")
            val copiado = context.contentResolver.openInputStream(origen)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
                true
            } ?: false
            if (copiado) FileProvider.getUriForFile(context, autoridad(context), destino) else null
        } catch (e: Exception) {
            null
        }
}
