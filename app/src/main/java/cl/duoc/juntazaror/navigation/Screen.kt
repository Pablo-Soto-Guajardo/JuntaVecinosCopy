package cl.duoc.juntazaror.navigation

/**
 * Sealed class con las rutas tipo-seguras de la app (patrón de la Guía 10).
 * Usar objetos en vez de Strings sueltos evita errores de tipeo al navegar.
 */
sealed class Screen(val route: String) {

    /** Ruta real a la que se navega (se sobrescribe en rutas con argumentos). */
    open val destino: String get() = route

    // ----- Rutas simples (sin argumentos) -----
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Registro : Screen("registro")
    data object Inicio : Screen("inicio")
    data object Reservas : Screen("reservas")
    data object NuevaReserva : Screen("nueva_reserva")
    data object Finanzas : Screen("finanzas")
    data object NuevoMovimiento : Screen("nuevo_movimiento")
    data object Perfil : Screen("perfil")

    // ----- Ruta con argumento: detalle de un movimiento según su id -----
    data class DetalleMovimiento(val movimientoId: Long) : Screen("detalle_movimiento/{movimientoId}") {
        override val destino: String get() = route.replace("{$ARGUMENTO}", movimientoId.toString())

        companion object {
            const val ARGUMENTO = "movimientoId"
            const val RUTA = "detalle_movimiento/{$ARGUMENTO}"
        }
    }
}
