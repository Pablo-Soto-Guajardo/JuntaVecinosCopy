package cl.duoc.juntazaror.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore con nombre "preferencias_junta" (patrón de la Guía 12)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferencias_junta")

/**
 * Guarda preferencias simples clave-valor:
 *  - id del usuario con sesión iniciada (la sesión se mantiene al cerrar la app)
 *  - si el usuario quiere recibir notificaciones
 */
class SesionDataStore(private val context: Context) {

    private val USUARIO_ID = longPreferencesKey("usuario_id")
    private val NOTIFICACIONES = booleanPreferencesKey("notificaciones_activas")

    /** Flow reactivo: emite null cuando no hay sesión. */
    val usuarioId: Flow<Long?> = context.dataStore.data.map { preferencias ->
        preferencias[USUARIO_ID]
    }

    /** Por defecto las notificaciones están activadas. */
    val notificacionesActivas: Flow<Boolean> = context.dataStore.data.map { preferencias ->
        preferencias[NOTIFICACIONES] ?: true
    }

    suspend fun guardarSesion(usuarioId: Long) {
        context.dataStore.edit { preferencias -> preferencias[USUARIO_ID] = usuarioId }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { preferencias -> preferencias.remove(USUARIO_ID) }
    }

    suspend fun guardarNotificaciones(activas: Boolean) {
        context.dataStore.edit { preferencias -> preferencias[NOTIFICACIONES] = activas }
    }
}
