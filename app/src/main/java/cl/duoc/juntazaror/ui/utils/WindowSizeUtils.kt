package cl.duoc.juntazaror.ui.utils

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable

/**
 * Calcula el tamaño de ventana actual (Guía 9).
 * Compact = teléfono vertical, Medium = teléfono horizontal / tablet pequeña,
 * Expanded = tablet o pantalla grande.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun obtenerWindowSizeClass(): WindowSizeClass =
    calculateWindowSizeClass(LocalActivity.current as android.app.Activity)

/** true cuando conviene usar diseño de pantalla ancha (NavigationRail, 2 columnas). */
@Composable
fun esPantallaAncha(): Boolean =
    obtenerWindowSizeClass().widthSizeClass != WindowWidthSizeClass.Compact
