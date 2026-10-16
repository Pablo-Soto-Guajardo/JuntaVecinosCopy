package cl.duoc.juntazaror.data.local

import cl.duoc.juntazaror.data.model.BloqueHorario
import cl.duoc.juntazaror.data.model.CategoriaMovimiento
import cl.duoc.juntazaror.data.model.Dependencia
import cl.duoc.juntazaror.data.model.EstadoReserva
import cl.duoc.juntazaror.data.model.Movimiento
import cl.duoc.juntazaror.data.model.Reserva
import cl.duoc.juntazaror.data.model.Rol
import cl.duoc.juntazaror.data.model.Usuario
import java.time.LocalDate

/**
 * Carga datos de demostración la PRIMERA vez que se abre la app.
 * Todos los nombres, RUT y montos son FICTICIOS (restricción del caso).
 *
 * Cuentas de prueba (contraseña de todas: junta123):
 *  - vecino@zaror.cl     -> Vecino/a
 *  - directiva@zaror.cl  -> Directiva / Secretaría
 *  - tesoreria@zaror.cl  -> Tesorería
 */
object DatosDemo {

    suspend fun cargarSiEstaVacia(db: AppDatabase) {
        val usuarioDao = db.usuarioDao()
        if (usuarioDao.contar() > 0) return

        val clave = "junta123"
        val idVecino = usuarioDao.insertar(
            Usuario(nombre = "Camila Rojas", rut = "11.111.111-1", correo = "vecino@zaror.cl",
                telefono = "911111111", direccion = "Los Aromos 120", clave = clave, rol = Rol.VECINO)
        )
        usuarioDao.insertar(
            Usuario(nombre = "Jorge Muñoz", rut = "12.345.678-5", correo = "directiva@zaror.cl",
                telefono = "922222222", direccion = "Los Jazmines 425", clave = clave, rol = Rol.DIRECTIVA)
        )
        usuarioDao.insertar(
            Usuario(nombre = "Patricia Soto", rut = "13.131.313-6", correo = "tesoreria@zaror.cl",
                telefono = "933333333", direccion = "Las Violetas 88", clave = clave, rol = Rol.TESORERIA)
        )

        val hoy = LocalDate.now()
        val reservaDao = db.reservaDao()
        reservaDao.insertar(
            Reserva(usuarioId = idVecino, nombreSolicitante = "Camila Rojas",
                dependencia = Dependencia.SALON_PRINCIPAL, fechaEpochDay = hoy.plusDays(5).toEpochDay(),
                bloque = BloqueHorario.TARDE, motivo = "Cumpleaños familiar con 40 invitados",
                asistentes = 40, monto = Dependencia.SALON_PRINCIPAL.tarifaPorBloque,
                estado = EstadoReserva.CONFIRMADA)
        )
        reservaDao.insertar(
            Reserva(usuarioId = idVecino, nombreSolicitante = "Camila Rojas",
                dependencia = Dependencia.SALA_REUNIONES, fechaEpochDay = hoy.plusDays(9).toEpochDay(),
                bloque = BloqueHorario.NOCHE, motivo = "Reunión del club de adulto mayor",
                asistentes = 15, monto = Dependencia.SALA_REUNIONES.tarifaPorBloque)
        )

        val movimientoDao = db.movimientoDao()
        val registradoPor = "Patricia Soto"
        movimientoDao.insertar(
            Movimiento(tipo = CategoriaMovimiento.CUOTA_SOCIAL.tipo, categoria = CategoriaMovimiento.CUOTA_SOCIAL,
                monto = 120000, fechaEpochDay = hoy.minusDays(20).toEpochDay(),
                descripcion = "Cuotas sociales del mes (ficticio)", registradoPor = registradoPor)
        )
        movimientoDao.insertar(
            Movimiento(tipo = CategoriaMovimiento.ARRIENDO.tipo, categoria = CategoriaMovimiento.ARRIENDO,
                monto = 35000, fechaEpochDay = hoy.minusDays(12).toEpochDay(),
                descripcion = "Arriendo salón principal - evento vecinal", registradoPor = registradoPor)
        )
        movimientoDao.insertar(
            Movimiento(tipo = CategoriaMovimiento.LUZ.tipo, categoria = CategoriaMovimiento.LUZ,
                monto = 42500, fechaEpochDay = hoy.minusDays(8).toEpochDay(),
                descripcion = "Boleta de luz de la sede", registradoPor = registradoPor)
        )
        movimientoDao.insertar(
            Movimiento(tipo = CategoriaMovimiento.AGUA.tipo, categoria = CategoriaMovimiento.AGUA,
                monto = 18900, fechaEpochDay = hoy.minusDays(3).toEpochDay(),
                descripcion = "Boleta de agua de la sede", registradoPor = registradoPor)
        )
    }
}
