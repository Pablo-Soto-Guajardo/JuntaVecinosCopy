package cl.duoc.juntazaror.data.model

import java.time.LocalDate

// ---------------------------------------------------------------------------
// Modelos de ESTADO de cada formulario (patrón de la Guía 11).
// Cada formulario tiene un data class con los valores de los campos
// y otro data class con el error de cada campo (null = sin error).
// ---------------------------------------------------------------------------

// ----- Inicio de sesión -----
data class LoginErrores(
    val correo: String? = null,
    val clave: String? = null
)

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val errores: LoginErrores = LoginErrores(),
    val errorGeneral: String? = null,   // Ej: "Correo o contraseña incorrectos"
    val cargando: Boolean = false
)

// ----- Registro de vecino/a -----
data class RegistroErrores(
    val nombre: String? = null,
    val rut: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val clave: String? = null,
    val confirmarClave: String? = null,
    val terminos: String? = null
) {
    fun hayErrores(): Boolean = listOfNotNull(
        nombre, rut, correo, telefono, direccion, clave, confirmarClave, terminos
    ).isNotEmpty()
}

data class RegistroUiState(
    val nombre: String = "",
    val rut: String = "",
    val correo: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val clave: String = "",
    val confirmarClave: String = "",
    val aceptaTerminos: Boolean = false,
    val errores: RegistroErrores = RegistroErrores(),
    val cargando: Boolean = false
)

// ----- Solicitud de reserva -----
data class ReservaErrores(
    val dependencia: String? = null,
    val fecha: String? = null,
    val bloque: String? = null,
    val asistentes: String? = null,
    val motivo: String? = null
) {
    fun hayErrores(): Boolean =
        listOfNotNull(dependencia, fecha, bloque, asistentes, motivo).isNotEmpty()
}

data class ReservaFormState(
    val dependencia: Dependencia? = null,
    val fecha: LocalDate? = null,
    val bloque: BloqueHorario? = null,
    val asistentes: String = "",
    val motivo: String = "",
    val errores: ReservaErrores = ReservaErrores(),
    val guardando: Boolean = false
)

// ----- Registro de movimiento financiero -----
data class MovimientoErrores(
    val categoria: String? = null,
    val monto: String? = null,
    val fecha: String? = null,
    val descripcion: String? = null,
    val comprobante: String? = null
) {
    fun hayErrores(): Boolean =
        listOfNotNull(categoria, monto, fecha, descripcion, comprobante).isNotEmpty()
}

data class MovimientoFormState(
    val tipo: TipoMovimiento = TipoMovimiento.INGRESO,
    val categoria: CategoriaMovimiento? = null,
    val monto: String = "",
    val fecha: LocalDate = LocalDate.now(),
    val descripcion: String = "",
    val comprobanteUri: String? = null,
    val errores: MovimientoErrores = MovimientoErrores(),
    val guardando: Boolean = false,
    val procesandoImagen: Boolean = false
)

// ----- Edición de perfil -----
data class PerfilErrores(
    val nombre: String? = null,
    val telefono: String? = null,
    val direccion: String? = null
) {
    fun hayErrores(): Boolean = listOfNotNull(nombre, telefono, direccion).isNotEmpty()
}

data class PerfilEdicionState(
    val editando: Boolean = false,
    val nombre: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val errores: PerfilErrores = PerfilErrores(),
    val guardando: Boolean = false
)
