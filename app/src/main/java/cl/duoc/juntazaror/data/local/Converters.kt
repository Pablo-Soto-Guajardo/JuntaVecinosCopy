package cl.duoc.juntazaror.data.local

import androidx.room.TypeConverter
import cl.duoc.juntazaror.data.model.BloqueHorario
import cl.duoc.juntazaror.data.model.CategoriaMovimiento
import cl.duoc.juntazaror.data.model.Dependencia
import cl.duoc.juntazaror.data.model.EstadoReserva
import cl.duoc.juntazaror.data.model.Rol
import cl.duoc.juntazaror.data.model.TipoMovimiento

/**
 * SQLite no conoce los enum de Kotlin.
 * Estos convertidores le indican a Room cómo guardarlos (como texto) y cómo leerlos.
 */
class Converters {
    @TypeConverter fun rolATexto(valor: Rol): String = valor.name
    @TypeConverter fun textoARol(valor: String): Rol = Rol.valueOf(valor)

    @TypeConverter fun dependenciaATexto(valor: Dependencia): String = valor.name
    @TypeConverter fun textoADependencia(valor: String): Dependencia = Dependencia.valueOf(valor)

    @TypeConverter fun bloqueATexto(valor: BloqueHorario): String = valor.name
    @TypeConverter fun textoABloque(valor: String): BloqueHorario = BloqueHorario.valueOf(valor)

    @TypeConverter fun estadoATexto(valor: EstadoReserva): String = valor.name
    @TypeConverter fun textoAEstado(valor: String): EstadoReserva = EstadoReserva.valueOf(valor)

    @TypeConverter fun tipoATexto(valor: TipoMovimiento): String = valor.name
    @TypeConverter fun textoATipo(valor: String): TipoMovimiento = TipoMovimiento.valueOf(valor)

    @TypeConverter fun categoriaATexto(valor: CategoriaMovimiento): String = valor.name
    @TypeConverter fun textoACategoria(valor: String): CategoriaMovimiento = CategoriaMovimiento.valueOf(valor)
}
