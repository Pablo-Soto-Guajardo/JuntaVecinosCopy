package cl.duoc.juntazaror.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.juntazaror.data.local.AppDatabase
import cl.duoc.juntazaror.data.local.SesionDataStore
import cl.duoc.juntazaror.data.model.CategoriaMovimiento
import cl.duoc.juntazaror.data.model.Movimiento
import cl.duoc.juntazaror.data.model.MovimientoErrores
import cl.duoc.juntazaror.data.model.MovimientoFormState
import cl.duoc.juntazaror.data.model.ResumenFinanciero
import cl.duoc.juntazaror.data.model.Rol
import cl.duoc.juntazaror.data.model.TipoMovimiento
import cl.duoc.juntazaror.data.model.Usuario
import cl.duoc.juntazaror.data.repository.MovimientoRepository
import cl.duoc.juntazaror.data.repository.SesionRepository
import cl.duoc.juntazaror.data.repository.UsuarioRepository
import cl.duoc.juntazaror.utils.Formatos
import cl.duoc.juntazaror.utils.ImagenUtils
import cl.duoc.juntazaror.utils.NotificacionHelper
import cl.duoc.juntazaror.utils.Validaciones
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel de finanzas: transparencia de ingresos/gastos y registro de movimientos
 * con comprobante (cámara o galería).
 * - Vecino/a: solo lectura.
 * - Tesorería y Directiva: pueden registrar movimientos.
 */
class FinanzasViewModel(application: Application) : AndroidViewModel(application) {

    private val baseDeDatos = AppDatabase.obtener(application)
    private val movimientoRepository = MovimientoRepository(baseDeDatos.movimientoDao())
    private val usuarioRepository = UsuarioRepository(baseDeDatos.usuarioDao())
    private val sesionRepository = SesionRepository(SesionDataStore(application))

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    /** Estructura de control: solo tesorería y directiva pueden registrar. */
    private val _puedeRegistrar = MutableStateFlow(false)
    val puedeRegistrar: StateFlow<Boolean> = _puedeRegistrar

    // Lista completa (se usa también para la pantalla de detalle)
    private val _movimientos = MutableStateFlow<List<Movimiento>>(emptyList())
    val movimientos: StateFlow<List<Movimiento>> = _movimientos

    private val _movimientosFiltrados = MutableStateFlow<List<Movimiento>>(emptyList())
    val movimientosFiltrados: StateFlow<List<Movimiento>> = _movimientosFiltrados

    private val _resumen = MutableStateFlow(ResumenFinanciero())
    val resumen: StateFlow<ResumenFinanciero> = _resumen

    private val _filtroTipo = MutableStateFlow<TipoMovimiento?>(null)
    val filtroTipo: StateFlow<TipoMovimiento?> = _filtroTipo

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    private val _form = MutableStateFlow(MovimientoFormState())
    val form: StateFlow<MovimientoFormState> = _form

    // ======================= CARGA DE DATOS =======================

    /** Lee el usuario conectado y los movimientos desde Room. Se llama al abrir cada pantalla. */
    fun cargarDatos() {
        viewModelScope.launch {
            val id = sesionRepository.obtenerUsuarioId()
            val usuarioActual = if (id != null) usuarioRepository.obtenerPorId(id) else null
            _usuario.value = usuarioActual
            _puedeRegistrar.value = usuarioActual != null && usuarioActual.rol != Rol.VECINO

            val lista = movimientoRepository.obtenerTodos()
            _movimientos.value = lista
            _resumen.value = movimientoRepository.calcularResumen(lista)
            aplicarFiltro()
        }
    }

    private fun aplicarFiltro() {
        val tipo = _filtroTipo.value
        _movimientosFiltrados.value =
            if (tipo == null) _movimientos.value else _movimientos.value.filter { it.tipo == tipo }
    }

    fun cambiarFiltro(tipo: TipoMovimiento?) {
        _filtroTipo.value = tipo
        aplicarFiltro()
    }

    // ======================= FORMULARIO =======================

    fun onTipoChange(valor: TipoMovimiento) {
        _form.update {
            // Si la categoría elegida no corresponde al nuevo tipo, se limpia
            val categoria = if (it.categoria?.tipo == valor) it.categoria else null
            it.copy(tipo = valor, categoria = categoria, errores = it.errores.copy(categoria = null, comprobante = null))
        }
    }

    fun onCategoriaChange(valor: CategoriaMovimiento) {
        _form.update { it.copy(categoria = valor, errores = it.errores.copy(categoria = null)) }
    }

    fun onMontoChange(valor: String) {
        val filtrado = valor.filter { it.isDigit() }.take(9)
        _form.update {
            val error = if (it.errores.monto != null) Validaciones.validarMonto(filtrado) else null
            it.copy(monto = filtrado, errores = it.errores.copy(monto = error))
        }
    }

    fun onFechaChange(valor: LocalDate) {
        _form.update { it.copy(fecha = valor, errores = it.errores.copy(fecha = Validaciones.validarFechaMovimiento(valor))) }
    }

    fun onDescripcionChange(valor: String) {
        val recortado = valor.take(150)
        _form.update {
            val error = if (it.errores.descripcion != null) Validaciones.validarDescripcion(recortado) else null
            it.copy(descripcion = recortado, errores = it.errores.copy(descripcion = error))
        }
    }

    // ----- Recurso nativo: CÁMARA (Guía 13) -----
    private var uriPendienteCamara: Uri? = null

    /** Crea el archivo destino antes de abrir la cámara. */
    fun crearUriCamara(): Uri {
        val uri = ImagenUtils.crearUriParaCamara(getApplication<Application>(), "comprobante")
        uriPendienteCamara = uri
        return uri
    }

    /** Resultado de TakePicture: si el usuario canceló (exito = false) no se hace nada. */
    fun onFotoTomada(exito: Boolean) {
        val uri = uriPendienteCamara
        if (exito && uri != null) {
            _form.update { it.copy(comprobanteUri = uri.toString(), errores = it.errores.copy(comprobante = null)) }
        }
        uriPendienteCamara = null
    }

    // ----- Recurso nativo: GALERÍA (Guía 13) -----
    fun onImagenGaleria(uri: Uri?) {
        if (uri == null) return // El usuario cerró la galería sin elegir: no pasa nada
        // Se copia a la carpeta de la app para que la imagen quede guardada
        val copia = ImagenUtils.copiarAAlmacenamientoInterno(getApplication<Application>(), uri, "comprobante")
        _form.update {
            if (copia != null) {
                it.copy(comprobanteUri = copia.toString(), errores = it.errores.copy(comprobante = null))
            } else {
                it.copy(errores = it.errores.copy(comprobante = "No se pudo cargar la imagen seleccionada"))
            }
        }
    }

    fun quitarComprobante() {
        _form.update { it.copy(comprobanteUri = null) }
    }

    fun limpiarFormulario() {
        _form.value = MovimientoFormState()
    }

    fun guardarMovimiento(onExito: () -> Unit) {
        val usuarioActual = _usuario.value ?: return
        if (usuarioActual.rol == Rol.VECINO) return // Un vecino no puede registrar

        val f = _form.value
        val errores = MovimientoErrores(
            categoria = Validaciones.validarSeleccion(f.categoria, "una categoría"),
            monto = Validaciones.validarMonto(f.monto),
            fecha = Validaciones.validarFechaMovimiento(f.fecha),
            descripcion = Validaciones.validarDescripcion(f.descripcion),
            comprobante = Validaciones.validarComprobante(f.tipo, f.comprobanteUri)
        )
        if (errores.hayErrores()) {
            _form.update { it.copy(errores = errores) }
            return
        }
        val categoria = f.categoria ?: return
        val monto = f.monto.toLong()

        viewModelScope.launch {
            _form.update { it.copy(guardando = true) }
            delay(500)
            movimientoRepository.registrar(
                Movimiento(
                    tipo = f.tipo,
                    categoria = categoria,
                    monto = monto,
                    fechaEpochDay = f.fecha.toEpochDay(),
                    descripcion = f.descripcion.trim(),
                    comprobanteUri = f.comprobanteUri,
                    registradoPor = usuarioActual.nombre
                )
            )
            limpiarFormulario()
            cargarDatos()
            if (sesionRepository.notificacionesActivas()) {
                NotificacionHelper.mostrar(
                    getApplication<Application>(),
                    "${f.tipo.etiqueta} registrado",
                    "${categoria.etiqueta}: ${Formatos.pesos(monto)} · registrado por ${usuarioActual.nombre}"
                )
            }
            mostrarMensaje("${f.tipo.etiqueta} de ${Formatos.pesos(monto)} registrado")
            onExito()
        }
    }

    private fun mostrarMensaje(texto: String) {
        viewModelScope.launch {
            _mensaje.value = texto
            delay(2500)
            _mensaje.value = null
        }
    }
}
