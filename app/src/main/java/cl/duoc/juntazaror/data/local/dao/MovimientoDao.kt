package cl.duoc.juntazaror.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.juntazaror.data.model.Movimiento

/** DAO: consultas SQL sobre la tabla "movimientos". */
@Dao
interface MovimientoDao {

    @Insert
    suspend fun insertar(movimiento: Movimiento): Long

    // Los más recientes primero
    @Query("SELECT * FROM movimientos ORDER BY fechaEpochDay DESC, creadoEn DESC")
    suspend fun obtenerTodos(): List<Movimiento>
}
