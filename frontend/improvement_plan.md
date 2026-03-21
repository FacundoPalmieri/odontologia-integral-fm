# Plan de Mejora Arquitectónica Paso a Paso

Para evitar regresiones y no abrumarte con refactorizaciones gigantes, lo ideal es adoptar los cambios de manera incremental. Te sugiero este plan de 4 fases, enfocado primero en un módulo "Piloto" antes de expandirlo.

## Progreso Actual

| Fase       | Tarea                                                |    Estado     |
| :--------- | :--------------------------------------------------- | :-----------: |
| **Fase 1** | Limpieza de Carpetas (Clean Architecture)            | ✅ Completado |
| **Fase 2** | Módulo Piloto con SignalStore e Inyección (`Users`)  | ✅ Completado |
| **Fase 3** | Patrón Smart / Dumb Components (En el módulo Piloto) | ⏳ Pendiente  |
| **Fase 4** | Rollout al resto de features                         | ⏳ Pendiente  |
| **Fase 4** | OnPush Global en componentes Dumb                    | ⏳ Pendiente  |
| **Fase 4** | Control de Lazy Loading (Optimización de rutas)      | ✅ Completado |

---

## Fase 1: Limpieza de Carpetas (Clean Architecture)

**Objetivo:** Mover los elementos de infraestructura/datos fuera de la carpeta `domain` en todos tus features actuales. Esto no rompe la lógica, solo cambia imports.

1. **Crear carpetas `data` o `infrastructure`**:
   - Ve a cada carpeta dentro de `features` (ej. `calendar`, `appointments`, `patients`).
   - Crea un nuevo directorio llamado `data` (o `infrastructure`).
2. **Mover DTOs y Serializadores**:
   - Mueve las carpetas `dtos` y `serializers` (que actualmente están dentro de `domain`) hacia la nueva carpeta `data`.
   - _Ejemplo:_ `features/calendar/domain/dtos` -> `features/calendar/data/dtos`.
3. **Actualizar Imports**:
   - Arregla los imports en todos los archivos afectados. Tu IDE debería ayudarte a hacer esto automáticamente, pero asegúrate de que el proyecto compile (`ng build`).

---

## Fase 2: Módulo Piloto con SignalStore e Inyección

**Objetivo:** Introducir `@ngrx/signals` en un solo feature mediano/grande para probar el flujo de trabajo. Te sugiero usar `calendar` o `odontogram`.

1. **Instalar `@ngrx/signals`**:
   - Ejecuta `npm install @ngrx/signals`.
2. **Crear el Store Local (`CalendarStore`)**:
   - Dentro de `features/calendar/data/store` (o la carpeta `state`), crea un archivo `calendar.store.ts`.
   - Mueve el estado que hoy vive disperso en el componente (ej. `calendarMonthData`, `calendarWeekData`, `isLoadingCalendar`) al SignalStore.
   - Define los métodos (`rxMethod` o métodos síncronos) para cargar data usando el `CalendarService`.
3. **Refactorizar el `CalendarComponent`**:
   - Inyecta el nuevo `CalendarStore`.
   - Reemplaza las variables manuales y llamadas HTTP directas en el `ngOnInit` por llamadas al Store (ej. `store.loadMonthView()`).
   - En el HTML, lee las variables directamente del store (ej. `store.calendarMonthData()`).

---

## Fase 3: Patrón Smart / Dumb Components (En el módulo Piloto)

**Objetivo:** Reducir el tamaño de los componentes principales (como el de +1600 líneas) dividiéndolos en sub-componentes presentacionales.

1. **Identificar "Componentes Dumb"**:
   - Revisa el HTML de tu componente principal (ej. `CalendarComponent`).
   - Extrae el Header (botones de prev/next mes, selección de vista) a un nuevo componente `CalendarHeaderComponent`.
   - Extrae la grilla del mes a un `CalendarMonthlyGridComponent`.
   - Extrae la grilla del día/semana a un `CalendarDailyGridComponent`.
2. **Implementar `@Input` y `@Output`**:
   - Estos nuevos componentes _no_ deben inyectar servicios ni el Store.
   - Reciben los datos por `@Input` (ej. `@Input() monthData!`).
   - Emiten acciones por `@Output` (ej. `@Output() dateSelected = new EventEmitter<Date>()`).
3. **Limpiar el Smart Component**:
   - Tu `CalendarComponent` debería quedar de unas 200-300 líneas. Simplemente inyecta el `SignalStore`, y en el HTML rutea los datos hacia los componentes _Dumb_ organizados en Fase 3.1.

---

## Fase 4: Escalado y Performance Global

**Objetivo:** Aplicar las lecciones aprendidas al resto de la app y exprimir el motor de Angular.

1. **Rollout al resto de features**:
   - Repite la Fase 2 y Fase 3 para features grandes como `patients`, `appointments` o `odontogram`.
2. **OnPush Global**:
   - Una vez que tus componentes _Dumb_ se basan en `@Input` y Signals, ve a todos ellos y agrega `changeDetection: ChangeDetectionStrategy.OnPush` en el decorador `@Component`.
   - Esto eliminará verificaciones de cambios redundantes y mejorará la fluidez de las tablas y grillas pesadas.
3. **Control de Lazy Loading**:
   - Revisa tu `app.routes.ts`. Asegúrate de que las rutas principales (ej. `/calendar`, `/patients`) estén cargando sus componentes usando `loadComponent: () => import(...)` para mantener el bundle inicial pequeño.
