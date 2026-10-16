package cl.duoc.juntazaror.navigation

/** Representa los distintos tipos de eventos de navegación (patrón de la Guía 10). */
sealed class NavigationEvent {

    /**
     * Navegar a un destino.
     * @param popUpToRoute  hasta qué ruta se limpia la pila (null = no se limpia)
     * @param inclusive     si también se elimina popUpToRoute
     * @param singleTop     evita apilar dos veces la misma pantalla (útil en la barra inferior)
     * @param limpiarPila   borra todo el historial (login / cerrar sesión)
     */
    data class NavigateTo(
        val route: Screen,
        val popUpToRoute: Screen? = null,
        val inclusive: Boolean = false,
        val singleTop: Boolean = false,
        val limpiarPila: Boolean = false
    ) : NavigationEvent()

    /** Volver a la pantalla anterior. */
    data object PopBackStack : NavigationEvent()

    /** Navegar "hacia arriba" en la jerarquía. */
    data object NavigateUp : NavigationEvent()
}
