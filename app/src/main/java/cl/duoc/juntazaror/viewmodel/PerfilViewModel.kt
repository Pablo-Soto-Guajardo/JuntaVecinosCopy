package cl.duoc.juntazaror.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.juntazaror.data.local.AppDatabase
import cl.duoc.juntazaror.data.local.SesionDataStore
import cl.duoc.juntazaror.data.model.PerfilEdicionState
import cl.duoc.juntazaror.data.model.PerfilErrores
import cl.duoc.juntazaror.data.model.Usuario
import cl.duoc.juntazaror.data.repository.SesionRepository
import cl.duoc.juntazaror.data.repository.UsuarioRepository
import cl.duoc.juntazaror.utils.ImagenUtils
import cl.duoc.juntazaror.utils.Validaciones
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del perfil (Guía 13): foto con cámara/galería, edición de datos
 * y preferencia de notificaciones (DataStore, Guía 12).
 */
class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val baseDeDatos = AppDatabase.obtener(application)
    private val usuarioRepository = UsuarioRepository(baseDeDatos.usuarioDao())
    private val sesionRepository = SesionRepository(SesionDataStore(application))

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _notificacionesActivas = MutableStateFlow(true)
    val notificacionesActivas: StateFlow<Boolean> = _notificacionesActivas

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    private val _edicion = MutableStateFlow(PerfilEdicionState())
    val edicion: StateFlow<PerfilEdicionState> = _edicion

    /** Lee el usuario conectado (Room) y sus preferencias (DataStore). */
    fun cargarDatos() {
        viewModelScope.launch {
            val id = sesionRepository.obtenerUsuarioId()
            _usuario.value = if (id != null) usuarioRepository.obtenerPorId(id) else null
            _notificacionesActivas.value = sesionRepository.notificacionesActivas()
        }
    }

    // ======================= FOTO DE PERFIL (Guía 13) =======================
    private var uriPendienteCamara: Uri? = null

    fun crearUriCamara(): Uri {
        val uri = ImagenUtils.crearUriParaCamara(getApplication<Application>(), "perfil")
        uriPendienteCamara = uri
        return uri
    }

    fun onFotoTomada(exito: Boolean) {
        val uri = uriPendienteCamara
        if (exito && uri != null) actualizarFoto(uri.toString())
        uriPendienteCamara = null
    }

    fun onImagenGaleria(uri: Uri?) {
        if (uri == null) return
        val copia = ImagenUtils.copiarAAlmacenamientoInterno(getApplication<Application>(), uri, "perfil")
        if (copia != null) actualizarFoto(copia.toString()) else mostrarMensaje("No se pudo cargar la imagen")
    }

    private fun actualizarFoto(uri: String) {
        val actual = _usuario.value ?: return
        val actualizado = actual.copy(fotoUri = uri)
        viewModelScope.launch {
            usuarioRepository.actualizar(actualizado) // UPDATE en Room
            _usuario.value = actualizado
            mostrarMensaje("Foto de perfil actualizada")
        }
    }

    // ======================= EDICIÓN DE DATOS =======================

    fun iniciarEdicion() {
        val actual = _usuario.value ?: return
        _edicion.value = PerfilEdicionState(
            editando = true,
            nombre = actual.nombre,
            telefono = actual.telefono,
            direccion = actual.direccion
        )
    }

    fun cancelarEdicion() {
        _edicion.value = PerfilEdicionState()
    }

    fun onNombreChange(valor: String) {
        _edicion.update { it.copy(nombre = valor, errores = it.errores.copy(nombre = Validaciones.validarNombre(valor))) }
    }

    fun onTelefonoChange(valor: String) {
        val filtrado = valor.filter { it.isDigit() || it == '+' }.take(12)
        _edicion.update { it.copy(telefono = filtrado, errores = it.errores.copy(telefono = Validaciones.validarTelefono(filtrado))) }
    }

    fun onDireccionChange(valor: String) {
        _edicion.update { it.copy(direccion = valor, errores = it.errores.copy(direccion = Validaciones.validarDireccion(valor))) }
    }

    fun guardarCambios() {
        val actual = _usuario.value ?: return
        val e = _edicion.value
        val errores = PerfilErrores(
            nombre = Validaciones.validarNombre(e.nombre),
            telefono = Validaciones.validarTelefono(e.telefono),
            direccion = Validaciones.validarDireccion(e.direccion)
        )
        if (errores.hayErrores()) {
            _edicion.update { it.copy(errores = errores) }
            return
        }
        val actualizado = actual.copy(
            nombre = e.nombre.trim(),
            telefono = e.telefono.removePrefix("+56"),
            direccion = e.direccion.trim()
        )
        viewModelScope.launch {
            _edicion.update { it.copy(guardando = true) }
            delay(400)
            usuarioRepository.actualizar(actualizado) // UPDATE en Room
            _usuario.value = actualizado
            _edicion.value = PerfilEdicionState()
            mostrarMensaje("Datos actualizados correctamente")
        }
    }

    // ======================= PREFERENCIAS (DataStore) =======================
    fun cambiarNotificaciones(activas: Boolean) {
        viewModelScope.launch {
            sesionRepository.guardarNotificaciones(activas)
            _notificacionesActivas.value = activas
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
