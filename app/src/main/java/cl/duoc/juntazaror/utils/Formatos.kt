package cl.duoc.juntazaror.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Funciones de formato para mostrar datos al usuario (montos y fechas en formato chileno). */
object Formatos {

    private val LOCALE_CL: Locale = Locale.forLanguageTag("es-CL")
    private val FECHA_CORTA = DateTimeFormatter.ofPattern("dd-MM-yyyy", LOCALE_CL)
    private val FECHA_LARGA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", LOCALE_CL)

    /** 35000 -> "$35.000" */
    fun pesos(monto: Long): String {
        val signo = if (monto < 0) "-" else ""
        val conSeparador = String.format(Locale.US, "%,d", kotlin.math.abs(monto)).replace(',', '.')
        return "$signo\$$conSeparador"
    }

    fun fechaCorta(fecha: LocalDate): String = fecha.format(FECHA_CORTA)

    fun fechaCorta(epochDay: Long): String = fechaCorta(LocalDate.ofEpochDay(epochDay))

    /** "sábado 14 de junio" */
    fun fechaLarga(epochDay: Long): String =
        LocalDate.ofEpochDay(epochDay).format(FECHA_LARGA).replaceFirstChar { it.uppercase() }

    /** "12345678k" -> "12.345.678-K" */
    fun rut(rut: String): String {
        val limpio = Validaciones.limpiarRut(rut)
        if (limpio.length < 2) return rut
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        val cuerpoConPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        return "$cuerpoConPuntos-$dv"
    }
}
