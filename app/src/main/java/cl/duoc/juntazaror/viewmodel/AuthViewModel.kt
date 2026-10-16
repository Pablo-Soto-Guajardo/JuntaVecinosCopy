package cl.duoc.juntazaror.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.juntazaror.data.local.AppDatabase
import cl.duoc.juntazaror.data.local.DatosDemo
import cl.duoc.juntazaror.data.local.SesionDataStore
import cl.duoc.juntazaror.data.model.LoginErrores
import cl.duoc.juntazaror.data.model.LoginUiState
import cl.duoc.juntazaror.data.model.RegistroErrores
import cl.duoc.juntazaror.data.model.RegistroUiState
import cl.duoc.juntazaror.data.model.Usuario
import cl.duoc.juntazaror.data.repository.SesionRepository
import cl.duoc.juntazaror.data.repository.UsuarioRepository
import cl.duoc.juntazaror.utils.Validaciones
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estados posibles de la sesión (se usan en el Splash para decidir a dónde ir). */
sealed class EstadoSesion {
    data object Cargando : EstadoSesion()
    data object SinSesion : EstadoSesion()
    data class Activa(val usuario: Usuario) : EstadoSesion()
}

/**
 * ViewModel de autenticación: inicio de sesión, registro y cierre de sesión.
 * Extiende AndroidViewModel porque necesita el contexto para Room y DataStore (Guía 12).
 * Cada formulario guarda su estado en un MutableStateFlow (Guía 11).
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // Base de datos y DataStore creados con el contexto de la aplicación
    private val baseDeDatos = AppDatabase.obtener(application)
    private val usuarioRepository = UsuarioRepository(baseDeDatos.usuarioDao())
    private val sesionRepository = SesionRepository(SesionDataStore(application))

    // ======================= SESIÓN =======================
    private val _estadoSesion = MutableStateFlow<EstadoSesion>(EstadoSesion.Cargando)
    val estadoSesion: StateFlow<EstadoSesion> = _estadoSesion

    init {
        // Al iniciar la app se revisa si había una sesión guardada en DataStore
        verificarSesion()
    }

    private fun verificarSesion() {
        viewModelScope.launch {
            DatosDemo.cargarSiEstaVacia(baseDeDatos) // Solo la primera vez
            delay(1200) // Simula la carga para mostrar el loader del Splash (Guía 12)
            val id = sesionRepository.obtenerUsuarioId()
            val usuario = if (id != null) usuarioRepository.obtenerPorId(id) else null
            _estadoSesion.value = if (usuario == null) EstadoSesion.SinSesion else EstadoSesion.Activa(usuario)
        }
    }

    // ======================= LOGIN =======================
    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    // Si el campo ya mostraba un error, se vuelve a validar mientras el usuario escribe
    fun onLoginCorreoChange(valor: String) {
        _login.update {
            val error = if (it.errores.correo != null) Validaciones.validarCorreo(valor) else null
            it.copy(correo = valor, errores = it.errores.copy(correo = error), errorGeneral = null)
        }
    }

    fun onLoginClaveChange(valor: String) {
        _login.update {
            val error = if (it.errores.clave != null) Validaciones.validarClaveLogin(valor) else null
            it.copy(clave = valor, errores = it.errores.copy(clave = error), errorGeneral = null)
        }
    }

    fun iniciarSesion(onExito: () -> Unit) {
        val estado = _login.value
        val errores = LoginErrores(
            correo = Validaciones.validarCorreo(estado.correo),
            clave = Validaciones.validarClaveLogin(estado.clave)
        )
        if (errores.correo != null || errores.clave != null) {
            _login.update { it.copy(errores = errores) }
            return
        }

        viewModelScope.launch {
            _login.update { it.copy(cargando = true, errorGeneral = null) }
            delay(600) // Pequeña espera para que se aprecie el indicador de carga
            val usuario = usuarioRepository.autenticar(estado.correo, estado.clave)
            if (usuario == null) {
                _login.update { it.copy(cargando = false, errorGeneral = "Correo o contraseña incorrectos") }
            } else {
                sesionRepository.iniciarSesion(usuario.id)
                _estadoSesion.value = EstadoSesion.Activa(usuario)
                _login.value = LoginUiState()
                onExito()
            }
        }
    }

    // ======================= REGISTRO =======================
    private val _registro = MutableStateFlow(RegistroUiState())
    val registro: StateFlow<RegistroUiState> = _registro

    fun onNombreChange(valor: String) {
        _registro.update {
            val error = if (it.errores.nombre != null) Validaciones.validarNombre(valor) else null
            it.copy(nombre = valor, errores = it.errores.copy(nombre = error))
        }
    }

    fun onRutChange(valor: String) {
        // Solo se permiten dígitos, K, puntos y guion (máx. 12 caracteres)
        val filtrado = valor.filter { it.isDigit() || it == 'k' || it == 'K' || it == '.' || it == '-' }.take(12)
        _registro.update {
            val error = if (it.errores.rut != null) Validaciones.validarRut(filtrado) else null
            it.copy(rut = filtrado, errores = it.errores.copy(rut = error))
        }
    }

    fun onCorreoChange(valor: String) {
        _registro.update {
            val error = if (it.errores.correo != null) Validaciones.validarCorreo(valor) else null
            it.copy(correo = valor, errores = it.errores.copy(correo = error))
        }
    }

    fun onTelefonoChange(valor: String) {
        val filtrado = valor.filter { it.isDigit() || it == '+' }.take(12)
        _registro.update {
            val error = if (it.errores.telefono != null) Validaciones.validarTelefono(filtrado) else null
            it.copy(telefono = filtrado, errores = it.errores.copy(telefono = error))
        }
    }

    fun onDireccionChange(valor: String) {
        _registro.update {
            val error = if (it.errores.direccion != null) Validaciones.validarDireccion(valor) else null
            it.copy(direccion = valor, errores = it.errores.copy(direccion = error))
        }
    }

    fun onClaveChange(valor: String) {
        _registro.update {
            val error = if (it.errores.clave != null) Validaciones.validarClave(valor) else null
            it.copy(clave = valor, errores = it.errores.copy(clave = error))
        }
    }

    fun onConfirmarClaveChange(valor: String) {
        _registro.update {
            val error = if (it.errores.confirmarClave != null) Validaciones.validarConfirmacionClave(it.clave, valor) else null
            it.copy(confirmarClave = valor, errores = it.errores.copy(confirmarClave = error))
        }
    }

    fun onTerminosChange(valor: Boolean) {
        _registro.update { it.copy(aceptaTerminos = valor, errores = it.errores.copy(terminos = null)) }
    }

    /** Valida TODOS los campos; retorna true si el formulario es válido (Guía 11). */
    private fun validarRegistro(): Boolean {
        val s = _registro.value
        val errores = RegistroErrores(
            nombre = Validaciones.validarNombre(s.nombre),
            rut = Validaciones.validarRut(s.rut),
            correo = Validaciones.validarCorreo(s.correo),
            telefono = Validaciones.validarTelefono(s.telefono),
            direccion = Validaciones.validarDireccion(s.direccion),
            clave = Validaciones.validarClave(s.clave),
            confirmarClave = Validaciones.validarConfirmacionClave(s.clave, s.confirmarClave),
            terminos = Validaciones.validarTerminos(s.aceptaTerminos)
        )
        _registro.update { it.copy(errores = errores) }
        return !errores.hayErrores()
    }

    fun registrar(onExito: () -> Unit) {
        if (!validarRegistro()) return
        val s = _registro.value

        viewModelScope.launch {
            _registro.update { it.copy(cargando = true) }
            delay(500)
            // Regla de negocio: no se permiten dos cuentas con el mismo correo
            if (usuarioRepository.existeCorreo(s.correo)) {
                _registro.update {
                    it.copy(cargando = false, errores = it.errores.copy(correo = "Este correo ya está registrado"))
                }
                return@launch
            }
            val nuevoId = usuarioRepository.registrarVecino(s.nombre, s.rut, s.correo, s.telefono, s.direccion, s.clave)
            // Se inicia sesión automáticamente con la cuenta recién creada
            sesionRepository.iniciarSesion(nuevoId)
            val usuario = usuarioRepository.obtenerPorId(nuevoId)
            if (usuario != null) _estadoSesion.value = EstadoSesion.Activa(usuario)
            _registro.value = RegistroUiState()
            onExito()
        }
    }

    fun limpiarRegistro() {
        _registro.value = RegistroUiState()
    }

    // ======================= CERRAR SESIÓN =======================
    fun cerrarSesion(onListo: () -> Unit) {
        viewModelScope.launch {
            sesionRepository.cerrarSesion()
            _estadoSesion.value = EstadoSesion.SinSesion
            onListo()
        }
    }
}
