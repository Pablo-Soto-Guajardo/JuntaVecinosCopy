package cl.duoc.juntazaror.data.repository

import cl.duoc.juntazaror.data.local.dao.UsuarioDao
import cl.duoc.juntazaror.data.model.Rol
import cl.duoc.juntazaror.data.model.Usuario
import cl.duoc.juntazaror.utils.Formatos

/**
 * Repository: única puerta de acceso a los datos de usuarios.
 * El ViewModel no sabe si los datos vienen de Room, de una API, etc.
 */
class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    suspend fun obtenerPorId(id: Long): Usuario? = usuarioDao.obtenerPorId(id)

    suspend fun existeCorreo(correo: String): Boolean =
        usuarioDao.buscarPorCorreo(correo.trim().lowercase()) != null

    /** Registra un nuevo vecino/a. Retorna el id generado. */
    suspend fun registrarVecino(
        nombre: String,
        rut: String,
        correo: String,
        telefono: String,
        direccion: String,
        clave: String
    ): Long = usuarioDao.insertar(
        Usuario(
            nombre = nombre.trim(),
            rut = Formatos.rut(rut),
            correo = correo.trim().lowercase(),
            telefono = telefono.replace(" ", "").removePrefix("+56"),
            direccion = direccion.trim(),
            clave = clave,
            rol = Rol.VECINO
        )
    )

    /** Retorna el usuario si el correo y la contraseña coinciden; si no, null. */
    suspend fun autenticar(correo: String, clave: String): Usuario? {
        val usuario = usuarioDao.buscarPorCorreo(correo.trim().lowercase()) ?: return null
        return if (usuario.clave == clave) usuario else null
    }

    suspend fun actualizar(usuario: Usuario) = usuarioDao.actualizar(usuario)
}
