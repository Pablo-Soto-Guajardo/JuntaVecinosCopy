package cl.duoc.juntazaror.data.repository

import cl.duoc.juntazaror.data.local.SesionDataStore
import kotlinx.coroutines.flow.first

/**
 * Repository de la sesión: lee y guarda en DataStore el usuario conectado
 * y la preferencia de notificaciones (patrón de la Guía 12).
 */
class SesionRepository(private val sesionDataStore: SesionDataStore) {

    /** Id del usuario con sesión iniciada, o null si no hay sesión. */
    suspend fun obtenerUsuarioId(): Long? = sesionDataStore.usuarioId.first()

    suspend fun notificacionesActivas(): Boolean = sesionDataStore.notificacionesActivas.first()

    suspend fun iniciarSesion(usuarioId: Long) = sesionDataStore.guardarSesion(usuarioId)

    suspend fun cerrarSesion() = sesionDataStore.cerrarSesion()

    suspend fun guardarNotificaciones(activas: Boolean) =
        sesionDataStore.guardarNotificaciones(activas)
}
