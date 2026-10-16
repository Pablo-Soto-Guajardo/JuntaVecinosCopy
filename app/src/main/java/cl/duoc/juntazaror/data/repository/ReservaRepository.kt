package cl.duoc.juntazaror.data.repository

import cl.duoc.juntazaror.data.local.dao.ReservaDao
import cl.duoc.juntazaror.data.model.BloqueHorario
import cl.duoc.juntazaror.data.model.Dependencia
import cl.duoc.juntazaror.data.model.EstadoReserva
import cl.duoc.juntazaror.data.model.Reserva
import java.time.LocalDate

/** Repository de reservas: encapsula el acceso a Room. */
class ReservaRepository(private val reservaDao: ReservaDao) {

    suspend fun obtenerTodas(): List<Reserva> = reservaDao.obtenerTodas()

    /** true si el bloque NO tiene otra reserva activa (regla anti-duplicidad). */
    suspend fun estaDisponible(dependencia: Dependencia, fecha: LocalDate, bloque: BloqueHorario): Boolean =
        reservaDao.contarOcupadas(dependencia, fecha.toEpochDay(), bloque) == 0

    suspend fun crear(reserva: Reserva): Long = reservaDao.insertar(reserva)

    suspend fun cambiarEstado(reserva: Reserva, nuevoEstado: EstadoReserva) =
        reservaDao.actualizar(reserva.copy(estado = nuevoEstado))

    suspend fun eliminar(reserva: Reserva) = reservaDao.eliminar(reserva)
}
