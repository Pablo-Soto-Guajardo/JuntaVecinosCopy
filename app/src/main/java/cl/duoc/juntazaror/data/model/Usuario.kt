package cl.duoc.juntazaror.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Perfiles de usuario definidos en el caso de la Junta de Vecinos.
 * Cada rol habilita distintas acciones dentro de la app.
 */
enum class Rol(val etiqueta: String) {
    VECINO("Vecino/a"),                  // Solicita arriendos y consulta finanzas
    DIRECTIVA("Directiva / Secretaría"), // Verifica disponibilidad y confirma reservas
    TESORERIA("Tesorería")               // Registra ingresos, gastos y comprobantes
}

/**
 * Tabla "usuarios" en Room (SQLite).
 * El correo es único para evitar cuentas duplicadas.
 */
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val rut: String,
    val correo: String,
    val telefono: String,
    val direccion: String,
    val clave: String,
    val rol: Rol = Rol.VECINO,
    val fotoUri: String? = null
)
