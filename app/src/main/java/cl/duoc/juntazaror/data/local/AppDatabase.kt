package cl.duoc.juntazaror.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cl.duoc.juntazaror.data.local.dao.MovimientoDao
import cl.duoc.juntazaror.data.local.dao.ReservaDao
import cl.duoc.juntazaror.data.local.dao.UsuarioDao
import cl.duoc.juntazaror.data.model.Movimiento
import cl.duoc.juntazaror.data.model.Reserva
import cl.duoc.juntazaror.data.model.Usuario

/**
 * Base de datos local SQLite gestionada por Room.
 * Contiene 3 tablas: usuarios, reservas y movimientos.
 */
@Database(
    entities = [Usuario::class, Reserva::class, Movimiento::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun reservaDao(): ReservaDao
    abstract fun movimientoDao(): MovimientoDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        /** Patrón Singleton: una sola instancia de la BD para toda la app. */
        fun obtener(context: Context): AppDatabase =
            INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "junta_zaror.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCIA = it }
            }
    }
}
