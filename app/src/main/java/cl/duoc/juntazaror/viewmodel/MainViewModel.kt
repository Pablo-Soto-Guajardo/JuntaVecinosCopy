package cl.duoc.juntazaror.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.juntazaror.navigation.NavigationEvent
import cl.duoc.juntazaror.navigation.Screen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de navegación (Guía 10).
 * Las pantallas NO usan el NavController directamente: emiten eventos aquí
 * y AppNavigation los ejecuta. Así la UI queda desacoplada de la navegación.
 */
class MainViewModel : ViewModel() {

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    fun navigateTo(
        screen: Screen,
        popUpTo: Screen? = null,
        inclusive: Boolean = false,
        singleTop: Boolean = false,
        limpiarPila: Boolean = false
    ) {
        viewModelScope.launch {
            _navigationEvents.emit(
                NavigationEvent.NavigateTo(screen, popUpTo, inclusive, singleTop, limpiarPila)
            )
        }
    }

    /** Navegación de la barra inferior: vuelve a Inicio como base y no duplica pantallas. */
    fun navegarPestana(screen: Screen) =
        navigateTo(screen, popUpTo = Screen.Inicio, singleTop = true)

    fun navigateBack() {
        viewModelScope.launch { _navigationEvents.emit(NavigationEvent.PopBackStack) }
    }

    fun navigateUp() {
        viewModelScope.launch { _navigationEvents.emit(NavigationEvent.NavigateUp) }
    }
}
