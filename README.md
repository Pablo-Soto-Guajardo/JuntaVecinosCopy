# Junta Zaror · App de gestión de arriendos y transparencia financiera

**Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles
**Evaluación:** Parcial 2 – Encargo (avance EFT)
**Integrantes:**
- Pablo Esteban Soto Aranda
- Maximiliano Andrés Mondaca Grandon

---

## 1. Descripción del proyecto

Aplicación Android para la **Junta de Vecinos Población Zaror** (Los Jazmines 425, Cerrillos).
Resuelve el problema del caso: el control de reservas de las dependencias comunitarias es manual
(riesgo de reservas duplicadas) y la información financiera está dispersa, lo que reduce la
transparencia hacia los vecinos.

La app permite:
- **Solicitar y gestionar arriendos** de las dependencias (salón, sala de reuniones, multicancha) **sin duplicidad** de horarios.
- **Registrar ingresos y gastos** con **foto del comprobante** (cámara o galería).
- **Transparentar** el saldo, los ingresos y los gastos a todos los vecinos.
- **Notificar** la confirmación de reservas y el registro de pagos.

> ⚠️ Todos los datos (nombres, RUT, montos, reservas) son **ficticios**, según las restricciones del caso.
> La app no se conecta a bancos ni pasarelas de pago: el registro de pagos es informativo.

## 2. Perfiles de usuario y reglas de negocio

| Perfil | Qué puede hacer |
|---|---|
| **Vecino/a** | Registrarse, solicitar arriendos, ver/cancelar **sus** solicitudes pendientes, consultar las finanzas (solo lectura). |
| **Directiva / Secretaría** | Ver **todas** las reservas, **confirmar o rechazar** solicitudes, registrar movimientos. |
| **Tesorería** | Registrar **ingresos y gastos** con comprobante, consultar reservas. |

Reglas de negocio implementadas:
1. **No se permiten reservas duplicadas**: una dependencia no puede tener dos reservas activas (pendiente o confirmada) en la misma fecha y bloque. Los bloques ocupados aparecen deshabilitados y además se valida en la base de datos antes de guardar.
2. Las reservas solo se pueden pedir desde hoy y con **máximo 90 días** de anticipación.
3. La cantidad de asistentes **no puede superar la capacidad** de la dependencia.
4. Todo **gasto** requiere **foto del comprobante** (respaldo para la rendición de cuentas).
5. Los movimientos **no se pueden eliminar** (trazabilidad) ni tener fecha futura.
6. No se pueden crear dos cuentas con el mismo correo. El RUT se valida con su dígito verificador.

## 3. Funcionalidades implementadas (según pauta)

| Requisito de la pauta | Implementación |
|---|---|
| Interfaz organizada y navegación clara | Material Design 3, `NavHost` + `NavController`, rutas tipo-seguras con `sealed class Screen`, barra inferior (`NavigationBar`) y `NavigationRail` en pantallas anchas, `TopAppBar` en todas las pantallas. |
| Formularios validados con íconos y mensajes | 5 formularios: **Login, Registro, Solicitud de arriendo, Registro de movimiento, Edición de perfil**. Cada campo tiene ícono, borde rojo (`isError`), ícono de error y mensaje (`supportingText`). |
| Validaciones desde la lógica | `utils/Validaciones.kt` (funciones puras, sin Compose) usadas por los ViewModels. |
| Animaciones funcionales | Transiciones entre pantallas, splash con logo animado (`spring`), botón con estado de carga (`AnimatedContent`), mensajes de éxito/error (`AnimatedVisibility`), color de estado animado (`animateColorAsState`), saldo tipo contador (`animateFloatAsState`), tarjetas expandibles (`animateContentSize`), listas animadas (`animateItem`), `Crossfade` en imágenes. |
| Estructura modular + persistencia local | MVVM. **Room (SQLite)** para usuarios, reservas y movimientos. **DataStore Preferences** para la sesión y la preferencia de notificaciones. |
| Recursos nativos (mín. 2) | **Cámara** (`TakePicture` + `FileProvider` + permiso en tiempo de ejecución), **Galería** (`GetContent`) y **Notificaciones locales** (`POST_NOTIFICATIONS` en Android 13+). |
| Adaptabilidad | `WindowSizeClass` (`ui/utils/WindowSizeUtils.kt`): Inicio con diseño compacto/expandido, `NavigationRail`, grilla adaptable de reservas, doble panel en Finanzas (`BoxWithConstraints`), formularios con ancho máximo. |

## 4. Arquitectura y estructura de carpetas

Patrón **MVVM**: la UI (Composables) solo muestra el estado y avisa eventos; el **ViewModel** contiene la
lógica de presentación y expone `StateFlow` (la UI lo lee con `collectAsState`); el **Repository** es la única puerta a los datos (Room / DataStore).
Cada ViewModel extiende `AndroidViewModel` y carga sus datos con `cargarDatos()` dentro de `viewModelScope.launch`, como en la Guía 12.

```
app/src/main/java/cl/duoc/juntazaror/
├── MainActivity.kt            → Punto de entrada: crea el canal de notificaciones y llama a AppNavigation
├── data/
│   ├── model/                 → Entidades Room (Usuario, Reserva, Movimiento) y estados de formularios
│   ├── local/                 → AppDatabase (Room), DAOs, Converters, SesionDataStore, DatosDemo
│   └── repository/            → UsuarioRepository, ReservaRepository, MovimientoRepository, SesionRepository
├── viewmodel/                 → MainViewModel (navegación), AuthViewModel, ReservaViewModel,
│                                FinanzasViewModel, PerfilViewModel
├── navigation/                → Screen (rutas), NavigationEvent, AppNavigation (NavHost)
├── ui/
│   ├── screens/               → Pantallas (login, registro, inicio/, reservas/, finanzas/, perfil)
│   ├── components/            → Componentes reutilizables (CampoTextoValidado, SelectorDesplegable,
│   │                            CampoFecha, BotonCarga, ImagenInteligente, BotonesImagen, tarjetas…)
│   ├── theme/                 → Colores, tipografía y tema Material 3
│   └── utils/                 → WindowSizeUtils (adaptabilidad)
└── utils/                     → Validaciones, Formatos, Seguridad, ImagenUtils, NotificacionHelper
```

## 5. Tecnologías

Kotlin 2.0 · Jetpack Compose · Material Design 3 · Navigation Compose · ViewModel + StateFlow ·
Room (SQLite) + KSP · DataStore Preferences · Coil · Activity Result API · FileProvider.

## 6. Pasos para ejecutar

1. Clonar el repositorio:
   ```bash
   git clone <URL-del-repositorio>
   ```
2. Abrir la carpeta en **Android Studio** (Meerkat o superior) y esperar la sincronización de Gradle.
3. Crear un emulador en **Device Manager** (recomendado: Pixel con Android 13 / API 33 o superior)
   o conectar un teléfono con **Depuración USB** activada.
4. Presionar **Run ▶ 'app'**.
5. Ingresar con una cuenta de prueba (contraseña de todas: **`junta123`**):

| Perfil | Correo |
|---|---|
| Vecino/a | `vecino@zaror.cl` |
| Directiva | `directiva@zaror.cl` |
| Tesorería | `tesoreria@zaror.cl` |

   También se puede crear una cuenta nueva de vecino/a desde **"Regístrate"**.

### Permisos que solicita la app
- **Cámara**: al tocar "Cámara" por primera vez (foto de perfil o comprobante).
- **Notificaciones** (Android 13+): al ingresar por primera vez o al activarlas en Perfil.
- La **galería** usa el selector del sistema y no necesita permiso en tiempo de ejecución.

> En el emulador, la cámara muestra una escena virtual; para probarla con una foto real conviene usar un teléfono físico.

## 7. Planificación y trabajo colaborativo

- Repositorio GitHub con ramas por funcionalidad (`feature/...`) y commits descriptivos.
- Tablero Trello con las listas *Por hacer / En curso / Finalizado* y tareas asignadas a cada integrante.
