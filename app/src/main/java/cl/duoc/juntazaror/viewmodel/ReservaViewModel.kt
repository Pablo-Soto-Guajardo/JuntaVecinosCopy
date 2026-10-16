package cl.duoc.juntazaror.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.juntazaror.data.local.AppDatabase
import cl.duoc.juntazaror.data.local.SesionDataStore
import cl.duoc.juntazaror.data.model.BloqueHorario
import cl.duoc.juntazaror.data.model.Dependencia
import cl.duoc.juntazaror.data.model.EstadoReserva
import cl.duoc.juntazaror.data.model.Reserva
import cl.duoc.juntazaror.data.model.ReservaErrores
import cl.duoc.juntazaror.data.model.ReservaFormState
import cl.duoc.juntazaror.data.model.Rol
import cl.duoc.juntazaror.data.model.Usuario
import cl.duoc.juntazaror.data.repository.ReservaRepository
import cl.duoc.juntazaror.data.repository.SesionRepository
import cl.duoc.juntazaror.data.repository.UsuarioRepository
import cl.duoc.juntazaror.utils.Formatos
import cl.duoc.juntazaror.utils.NotificacionHelper
import cl.duoc.juntazaror.utils.Validaciones
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel de reservas (arriendo de dependencias).
 * - Vecino/a: solicita reservas y ve SOLO las suyas.
 * - Directiva: ve todas y las confirma o rechaza.
 */
class ReservaViewModel(application: Application) : AndroidViewModel(application) {

    private val baseDeDatos = AppDatabase.obtener(application)
    private val reservaRepository = ReservaRepository(baseDeDatos.reservaDao())
    private val usuarioRepository = UsuarioRepository(baseDeDatos.usuarioDao())
    private val sesionRepository = SesionRepository(SesionDataStore(application))

    // Todas las reservas leídas desde Room
    private var todas: List<Reserva> = emptyList()

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    // Filtro por estado (null = todas)
    private val _filtro = MutableStateFlow<EstadoReserva?>(null)
    val filtro: StateFlow<EstadoReserva?> = _filtro

    /** Lista que ve el usuario según su ROL y el filtro elegido. */
    private val _reservasVisibles = MutableStateFlow<List<Reserva>>(emptyList())
    val reservasVisibles: StateFlow<List<Reserva>> = _reservasVisibles

    /** Próximas reservas confirmadas de la sede (transparencia de ocupación en Inicio). */
    private val _proximasConfirmadas = MutableStateFlow<List<Reserva>>(emptyList())
    val proximasConfirmadas: StateFlow<List<Reserva>> = _proximasConfirmadas

    private val _cantidadPendientes = MutableStateFlow(0)
    val cantidadPendientes: StateFlow<Int> = _cantidadPendientes

    /** Bloques ya ocupados para la dependencia y fecha elegidas en el formulario. */
    private val _bloquesOcupados = MutableStateFlow<Set<BloqueHorario>>(emptySet())
    val bloquesOcupados: StateFlow<Set<BloqueHorario>> = _bloquesOcupados

    // Mensaje de éxito temporal (se muestra con AnimatedVisibility)
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    private val _form = MutableStateFlow(ReservaFormState())
    val form: StateFlow<ReservaFormState> = _form

    // ======================= CARGA DE DATOS =======================

    /** Lee el usuario conectado y las reservas desde Room. Se llama al abrir cada pantalla. */
    fun cargarDatos() {
        viewModelScope.launch {
            val id = sesionRepository.obtenerUsuarioId()
            _usuario.value = if (id != null) usuarioRepository.obtenerPorId(id) else null
            todas = reservaRepository.obtenerTodas()
            actualizarListas()
        }
    }

    /** Recalcula todas las listas que muestra la UI (estructuras de control: when / filter). */
    private fun actualizarListas() {
        val usuarioActual = _usuario.value
        val hoy = LocalDate.now().toEpochDay()

        val segunRol = when {
            usuarioActual == null -> emptyList()
            usuarioActual.rol == Rol.VECINO -> todas.filter { it.usuarioId == usuarioActual.id }
            else -> todas
        }
        val filtroActual = _filtro.value
        _reservasVisibles.value = segunRol
            .filter { filtroActual == null || it.estado == filtroActual }
            .sortedWith(compareBy({ it.fechaEpochDay }, { it.bloque.ordinal }))

        _proximasConfirmadas.value = todas
            .filter { it.estado == EstadoReserva.CONFIRMADA && it.fechaEpochDay >= hoy }
            .sortedWith(compareBy({ it.fechaEpochDay }, { it.bloque.ordinal }))
            .take(3)

        _cantidadPendientes.value = todas.count { it.estado == EstadoReserva.PENDIENTE }

        actualizarBloquesOcupados()
    }

    private fun actualizarBloquesOcupados() {
        val dependencia = _form.value.dependencia
        val fecha = _form.value.fecha
        _bloquesOcupados.value = if (dependencia == null || fecha == null) {
            emptySet()
        } else {
            todas.filter {
                it.dependencia == dependencia &&
                    it.fechaEpochDay == fecha.toEpochDay() &&
                    it.estado != EstadoReserva.RECHAZADA
            }.map { it.bloque }.toSet()
        }
    }

    fun cambiarFiltro(nuevo: EstadoReserva?) {
        _filtro.value = nuevo
        actualizarListas()
    }

    // ======================= FORMULARIO =======================

    fun onDependenciaChange(valor: Dependencia) {
        _form.update {
            // Si ya había asistentes ingresados, se revalida contra la nueva capacidad
            val errorAsistentes = if (it.asistentes.isNotBlank()) Validaciones.validarAsistentes(it.asistentes, valor.capacidad) else null
            it.copy(
                dependencia = valor,
                errores = it.errores.copy(dependencia = null, bloque = null, asistentes = errorAsistentes)
            )
        }
        actualizarBloquesOcupados()
    }

    fun onFechaChange(valor: LocalDate) {
        _form.update {
            it.copy(fecha = valor, errores = it.errores.copy(fecha = Validaciones.validarFechaReserva(valor), bloque = null))
        }
        actualizarBloquesOcupados()
    }

    fun onBloqueChange(valor: BloqueHorario) {
        _form.update { it.copy(bloque = valor, errores = it.errores.copy(bloque = null)) }
    }

    fun onAsistentesChange(valor: String) {
        val filtrado = valor.filter { it.isDigit() }.take(3)
        _form.update {
            val error = if (it.errores.asistentes != null) Validaciones.validarAsistentes(filtrado, it.dependencia?.capacidad) else null
            it.copy(asistentes = filtrado, errores = it.errores.copy(asistentes = error))
        }
    }

    fun onMotivoChange(valor: String) {
        val recortado = valor.take(200)
        _form.update {
            val error = if (it.errores.motivo != null) Validaciones.validarMotivo(recortado) else null
            it.copy(motivo = recortado, errores = it.errores.copy(motivo = error))
        }
    }

    fun limpiarFormulario() {
        _form.value = ReservaFormState()
        _bloquesOcupados.value = emptySet()
    }

    fun guardarReserva(onExito: () -> Unit) {
        val usuarioActual = _usuario.value ?: return
        val f = _form.value
        val errores = ReservaErrores(
            dependencia = Validaciones.validarSeleccion(f.dependencia, "una dependencia"),
            fecha = Validaciones.validarFechaReserva(f.fecha),
            bloque = Validaciones.validarSeleccion(f.bloque, "un bloque horario"),
            asistentes = Validaciones.validarAsistentes(f.asistentes, f.dependencia?.capacidad),
            motivo = Validaciones.validarMotivo(f.motivo)
        )
        if (errores.hayErrores()) {
            _form.update { it.copy(errores = errores) }
            return
        }
        val dependencia = f.dependencia ?: return
        val fecha = f.fecha ?: return
        val bloque = f.bloque ?: return

        viewModelScope.launch {
            _form.update { it.copy(guardando = true) }
            delay(500)
            // Regla de negocio: no pueden existir dos reservas activas en el mismo bloque
            if (!reservaRepository.estaDisponible(dependencia, fecha, bloque)) {
                _form.update {
                    it.copy(guardando = false, errores = it.errores.copy(bloque = "Este bloque ya está reservado, elige otro"))
                }
                return@launch
            }
            reservaRepository.crear(
                Reserva(
                    usuarioId = usuarioActual.id,
                    nombreSolicitante = usuarioActual.nombre,
                    dependencia = dependencia,
                    fechaEpochDay = fecha.toEpochDay(),
                    bloque = bloque,
                    motivo = f.motivo.trim(),
                    asistentes = f.asistentes.toInt(),
                    monto = dependencia.tarifaPorBloque
                )
            )
            limpiarFormulario()
            cargarDatos()
            notificar(
                "Solicitud de reserva enviada",
                "${dependencia.nombre} · ${Formatos.fechaCorta(fecha)} · ${bloque.etiqueta}. Queda pendiente de revisión por la directiva."
            )
            mostrarMensaje("Solicitud enviada: queda pendiente de confirmación")
            onExito()
        }
    }

    // ======================= ACCIONES SOBRE RESERVAS =======================

    /** Solo la Directiva puede confirmar o rechazar. */
    fun cambiarEstado(reserva: Reserva, nuevoEstado: EstadoReserva) {
        if (_usuario.value?.rol != Rol.DIRECTIVA) return
        viewModelScope.launch {
            reservaRepository.cambiarEstado(reserva, nuevoEstado)
            cargarDatos()
            val texto = if (nuevoEstado == EstadoReserva.CONFIRMADA) "confirmada" else "rechazada"
            notificar(
                "Reserva $texto",
                "${reserva.dependencia.nombre} · ${Formatos.fechaCorta(reserva.fechaEpochDay)} · solicitada por ${reserva.nombreSolicitante}"
            )
            mostrarMensaje("Reserva $texto correctamente")
        }
    }

    /** El vecino puede cancelar su propia solicitud mientras esté pendiente. */
    fun cancelar(reserva: Reserva) {
        val actual = _usuario.value ?: return
        if (reserva.usuarioId != actual.id || reserva.estado != EstadoReserva.PENDIENTE) return
        viewModelScope.launch {
            reservaRepository.eliminar(reserva)
            cargarDatos()
            mostrarMensaje("Solicitud cancelada")
        }
    }

    /** Muestra el mensaje y lo oculta después de 2,5 segundos (Guía 12). */
    private fun mostrarMensaje(texto: String) {
        viewModelScope.launch {
            _mensaje.value = texto
            delay(2500)
            _mensaje.value = null
        }
    }

    /** Respeta la preferencia del usuario guardada en DataStore. */
    private suspend fun notificar(titulo: String, mensaje: String) {
        if (sesionRepository.notificacionesActivas()) {
            NotificacionHelper.mostrar(getApplication<Application>(), titulo, mensaje)
        }
    }
}
