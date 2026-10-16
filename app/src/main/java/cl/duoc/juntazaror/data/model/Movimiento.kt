package cl.duoc.juntazaror.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Un movimiento financiero puede ser un ingreso o un gasto. */
enum class TipoMovimiento(val etiqueta: String) {
    INGRESO("Ingreso"),
    GASTO("Gasto")
}

/** Categorías de movimientos: cada una pertenece a un tipo (ingreso o gasto). */
enum class CategoriaMovimiento(val etiqueta: String, val tipo: TipoMovimiento) {
    ARRIENDO("Arriendo de dependencias", TipoMovimiento.INGRESO),
    CUOTA_SOCIAL("Cuotas sociales", TipoMovimiento.INGRESO),
    OTRO_INGRESO("Otro ingreso", TipoMovimiento.INGRESO),
    LUZ("Consumo eléctrico", TipoMovimiento.GASTO),
    AGUA("Consumo de agua", TipoMovimiento.GASTO),
    GAS("Consumo de gas", TipoMovimiento.GASTO),
    MANTENCION("Mantención y reparaciones", TipoMovimiento.GASTO),
    OTRO_GASTO("Otro gasto", TipoMovimiento.GASTO);

    companion object {
        /** Devuelve solo las categorías que corresponden al tipo elegido. */
        fun porTipo(tipo: TipoMovimiento): List<CategoriaMovimiento> =
            entries.filter { it.tipo == tipo }
    }
}

/**
 * Tabla "movimientos" en Room.
 * Los movimientos no se eliminan: así se mantiene la trazabilidad de la rendición de cuentas.
 * comprobanteUri guarda la foto del comprobante (cámara o galería).
 */
@Entity(tableName = "movimientos")
data class Movimiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: TipoMovimiento,
    val categoria: CategoriaMovimiento,
    val monto: Long,
    val fechaEpochDay: Long,
    val descripcion: String,
    val comprobanteUri: String? = null,
    val registradoPor: String,
    val creadoEn: Long = System.currentTimeMillis()
)

/** Resumen calculado a partir de los movimientos (no se guarda en la BD). */
data class ResumenFinanciero(
    val totalIngresos: Long = 0,
    val totalGastos: Long = 0
) {
    val saldo: Long get() = totalIngresos - totalGastos
}
