package cl.duoc.juntazaror.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Dependencias comunitarias que se pueden arrendar.
 * Capacidad y tarifa son valores FICTICIOS (restricción del caso).
 */
enum class Dependencia(val nombre: String, val capacidad: Int, val tarifaPorBloque: Long) {
    SALON_PRINCIPAL("Salón principal", 80, 35000),
    SALA_REUNIONES("Sala de reuniones", 20, 12000),
    MULTICANCHA("Multicancha", 40, 18000)
}

/** Bloques horarios fijos: simplifican la verificación de disponibilidad y evitan choques. */
enum class BloqueHorario(val etiqueta: String) {
    MANANA("Mañana · 09:00 - 13:00"),
    TARDE("Tarde · 14:00 - 18:00"),
    NOCHE("Noche · 19:00 - 23:00")
}

/** Ciclo de vida de una solicitud de arriendo. */
enum class EstadoReserva(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    RECHAZADA("Rechazada")
}

/**
 * Tabla "reservas" en Room.
 * La fecha se guarda como epochDay (días desde 1970-01-01) para poder
 * comparar y ordenar fácilmente en SQL.
 */
@Entity(tableName = "reservas")
data class Reserva(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val nombreSolicitante: String,
    val dependencia: Dependencia,
    val fechaEpochDay: Long,
    val bloque: BloqueHorario,
    val motivo: String,
    val asistentes: Int,
    val monto: Long,
    val estado: EstadoReserva = EstadoReserva.PENDIENTE,
    val creadaEn: Long = System.currentTimeMillis()
)
