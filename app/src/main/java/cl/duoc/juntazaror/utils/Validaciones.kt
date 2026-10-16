package cl.duoc.juntazaror.utils

import cl.duoc.juntazaror.data.model.TipoMovimiento
import java.time.LocalDate

/**
 * LÓGICA DE VALIDACIÓN CENTRALIZADA Y DESACOPLADA DE LA UI.
 *
 * - Son funciones puras de Kotlin: no dependen de Compose ni de Android.
 * - Cada función retorna un String con el mensaje de error, o null si el valor es válido.
 * - Los ViewModels las usan para actualizar el estado; las pantallas solo muestran el resultado.
 * - Al no depender de Android se pueden probar con pruebas unitarias (ver ValidacionesTest).
 */
object Validaciones {

    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    const val MONTO_MAXIMO = 10_000_000L
    const val DIAS_MAXIMOS_ANTICIPACION = 90L

    // ------------------------- Datos personales -------------------------

    fun validarNombre(nombre: String): String? = when {
        nombre.isBlank() -> "El nombre es obligatorio"
        nombre.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        !nombre.all { it.isLetter() || it.isWhitespace() } -> "Solo se permiten letras y espacios"
        else -> null
    }

    fun validarCorreo(correo: String): String? = when {
        correo.isBlank() -> "El correo es obligatorio"
        !REGEX_CORREO.matches(correo.trim()) -> "Formato de correo inválido (ej: nombre@correo.cl)"
        else -> null
    }

    fun validarClaveLogin(clave: String): String? =
        if (clave.isBlank()) "La contraseña es obligatoria" else null

    fun validarClave(clave: String): String? = when {
        clave.isBlank() -> "La contraseña es obligatoria"
        clave.length < 6 -> "Debe tener al menos 6 caracteres"
        !clave.any { it.isLetter() } -> "Debe incluir al menos una letra"
        !clave.any { it.isDigit() } -> "Debe incluir al menos un número"
        else -> null
    }

    fun validarConfirmacionClave(clave: String, confirmacion: String): String? = when {
        confirmacion.isBlank() -> "Debes repetir la contraseña"
        clave != confirmacion -> "Las contraseñas no coinciden"
        else -> null
    }

    /** Valida un RUT chileno con su dígito verificador (algoritmo módulo 11). */
    fun validarRut(rut: String): String? {
        if (rut.isBlank()) return "El RUT es obligatorio"
        val limpio = limpiarRut(rut)
        if (limpio.length !in 8..9) return "Formato inválido (ej: 12.345.678-5)"
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        if (!cuerpo.all { it.isDigit() }) return "Formato inválido (ej: 12.345.678-5)"
        if (!(dv.isDigit() || dv == 'K')) return "Dígito verificador inválido"
        if (calcularDigitoVerificador(cuerpo) != dv) return "El dígito verificador no corresponde"
        return null
    }

    fun limpiarRut(rut: String): String =
        rut.replace(".", "").replace("-", "").replace(" ", "").uppercase()

    fun calcularDigitoVerificador(cuerpo: String): Char {
        var suma = 0
        var multiplicador = 2
        for (caracter in cuerpo.reversed()) {
            suma += caracter.digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        return when (val resultado = 11 - (suma % 11)) {
            11 -> '0'
            10 -> 'K'
            else -> resultado.digitToChar()
        }
    }

    /** Teléfono móvil chileno: 9 dígitos comenzando en 9 (se acepta +56 al inicio). */
    fun validarTelefono(telefono: String): String? {
        if (telefono.isBlank()) return "El teléfono es obligatorio"
        val digitos = telefono.replace(" ", "").removePrefix("+56")
        return when {
            !digitos.all { it.isDigit() } -> "Solo se permiten números"
            digitos.length != 9 -> "Debe tener 9 dígitos (ej: 912345678)"
            !digitos.startsWith("9") -> "Debe comenzar con 9"
            else -> null
        }
    }

    fun validarDireccion(direccion: String): String? = when {
        direccion.isBlank() -> "La dirección es obligatoria"
        direccion.trim().length < 5 -> "Ingresa una dirección más completa"
        !direccion.any { it.isDigit() } -> "Incluye la numeración (ej: Los Jazmines 425)"
        else -> null
    }

    fun validarTerminos(acepta: Boolean): String? =
        if (!acepta) "Debes aceptar el uso de tus datos para continuar" else null

    // ------------------------- Reservas -------------------------

    fun validarSeleccion(valor: Any?, campo: String): String? =
        if (valor == null) "Selecciona $campo" else null

    /** La fecha no puede ser pasada ni superar los 90 días de anticipación. */
    fun validarFechaReserva(fecha: LocalDate?, hoy: LocalDate = LocalDate.now()): String? = when {
        fecha == null -> "Selecciona una fecha"
        fecha.isBefore(hoy) -> "No puedes reservar en una fecha pasada"
        fecha.isAfter(hoy.plusDays(DIAS_MAXIMOS_ANTICIPACION)) ->
            "Solo se puede reservar con hasta $DIAS_MAXIMOS_ANTICIPACION días de anticipación"
        else -> null
    }

    fun validarAsistentes(texto: String, capacidad: Int?): String? {
        if (texto.isBlank()) return "Indica la cantidad de asistentes"
        val cantidad = texto.toIntOrNull() ?: return "Ingresa solo números"
        return when {
            cantidad <= 0 -> "Debe haber al menos 1 asistente"
            capacidad != null && cantidad > capacidad -> "Supera la capacidad máxima ($capacidad personas)"
            else -> null
        }
    }

    fun validarMotivo(motivo: String): String? = when {
        motivo.isBlank() -> "Describe el motivo del arriendo"
        motivo.trim().length < 10 -> "Describe el motivo con al menos 10 caracteres"
        motivo.length > 200 -> "Máximo 200 caracteres"
        else -> null
    }

    // ------------------------- Movimientos financieros -------------------------

    fun validarMonto(texto: String): String? {
        if (texto.isBlank()) return "El monto es obligatorio"
        val monto = texto.toLongOrNull() ?: return "Ingresa solo números, sin puntos"
        return when {
            monto <= 0 -> "El monto debe ser mayor a 0"
            monto > MONTO_MAXIMO -> "El monto no puede superar ${Formatos.pesos(MONTO_MAXIMO)}"
            else -> null
        }
    }

    /** Un movimiento no puede tener fecha futura ni ser de hace más de un año. */
    fun validarFechaMovimiento(fecha: LocalDate, hoy: LocalDate = LocalDate.now()): String? = when {
        fecha.isAfter(hoy) -> "La fecha no puede ser futura"
        fecha.isBefore(hoy.minusYears(1)) -> "No se aceptan movimientos de hace más de un año"
        else -> null
    }

    fun validarDescripcion(descripcion: String): String? = when {
        descripcion.isBlank() -> "La descripción es obligatoria"
        descripcion.trim().length < 5 -> "Debe tener al menos 5 caracteres"
        descripcion.length > 150 -> "Máximo 150 caracteres"
        else -> null
    }

    /** Regla de negocio: todo GASTO debe tener comprobante (respaldo para la rendición). */
    fun validarComprobante(tipo: TipoMovimiento, comprobanteUri: String?): String? =
        if (tipo == TipoMovimiento.GASTO && comprobanteUri == null)
            "Los gastos requieren foto del comprobante o boleta"
        else null
}
