package cl.duoc.juntazaror.data.repository

import cl.duoc.juntazaror.data.local.dao.MovimientoDao
import cl.duoc.juntazaror.data.model.Movimiento
import cl.duoc.juntazaror.data.model.ResumenFinanciero
import cl.duoc.juntazaror.data.model.TipoMovimiento

/** Repository de movimientos financieros (ingresos y gastos). */
class MovimientoRepository(private val movimientoDao: MovimientoDao) {

    suspend fun obtenerTodos(): List<Movimiento> = movimientoDao.obtenerTodos()

    suspend fun registrar(movimiento: Movimiento): Long = movimientoDao.insertar(movimiento)

    /** Suma los ingresos y los gastos de la lista. */
    fun calcularResumen(movimientos: List<Movimiento>): ResumenFinanciero =
        ResumenFinanciero(
            totalIngresos = movimientos.filter { it.tipo == TipoMovimiento.INGRESO }.sumOf { it.monto },
            totalGastos = movimientos.filter { it.tipo == TipoMovimiento.GASTO }.sumOf { it.monto }
        )
}
