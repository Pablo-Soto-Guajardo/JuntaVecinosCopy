package cl.duoc.juntazaror.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.juntazaror.data.model.BloqueHorario
import cl.duoc.juntazaror.data.model.Dependencia
import cl.duoc.juntazaror.data.model.Reserva

/** DAO: consultas SQL sobre la tabla "reservas". */
@Dao
interface ReservaDao {

    @Insert
    suspend fun insertar(reserva: Reserva): Long

    @Update
    suspend fun actualizar(reserva: Reserva)

    @Delete
    suspend fun eliminar(reserva: Reserva)

    @Query("SELECT * FROM reservas ORDER BY fechaEpochDay ASC, creadaEn ASC")
    suspend fun obtenerTodas(): List<Reserva>

    /**
     * Regla de negocio: evitar duplicidad de reservas.
     * Cuenta cuántas reservas activas (no rechazadas) existen para la misma
     * dependencia, fecha y bloque horario. Si es mayor a 0, el bloque está ocupado.
     */
    @Query(
        """
        SELECT COUNT(*) FROM reservas
        WHERE dependencia = :dependencia
          AND fechaEpochDay = :fechaEpochDay
          AND bloque = :bloque
          AND estado != 'RECHAZADA'
        """
    )
    suspend fun contarOcupadas(
        dependencia: Dependencia,
        fechaEpochDay: Long,
        bloque: BloqueHorario
    ): Int
}
