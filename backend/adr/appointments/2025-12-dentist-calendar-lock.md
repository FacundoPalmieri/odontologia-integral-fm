## ADR — Dentist Calendar Lock (Bloqueos de Calendario del Dentista)

### Fecha: 2025-12-10
### Contexto: Feature Appointment 

## 1. Contexto y Problema

Los dentistas requieren la capacidad de bloquear temporalmente su agenda para situaciones que no forman parte de su disponibilidad habitual (vacaciones, capacitaciones, imprevistos, eventos recurrentes, días no laborables, etc.).
    
El sistema ya posee un módulo de Dentist Availability, que define cuándo un dentista trabaja. Sin embargo, esa disponibilidad no contempla excepciones dinámicas y cambiantes.
    
El problema a resolver es:
    
-  Permitir que cada dentista bloquee fechas u horarios específicos dentro de rangos en los que realmente trabaja.
    
-  Evitar bloqueos inconsistentes ("bloqueos fantasma") que caen fuera del horario laboral del dentista.
    
-  Evitar superposición de locks que generen duplicidad o inconsistencias.
    
-  crear un lock cuando existen turnos previamente.
    
-  Permitir lograr un calendario final unificado que combine:
    
    - disponibilidad base,
    
    - turnos reservados,
    
    - bloqueos del profesional.


## 2. Decisiones Tomadas

### 2.1. Validar que el bloqueo esté dentro de la disponibilidad real del dentista
    
Antes de persistir un bloqueo, el sistema ejecuta:
        
-  validateAvailabilityForCalendarLock
        
-  validateExistCommonAvailabilityRange
        
Esto garantiza que el rango del lock intersecte un rango de disponibilidad real.

#### Resultado: no se permiten “bloqueos fantasma” que no tengan sentido operativo.



### 2.2. Verificar conflictos con otros Calendar Locks
        
Se invoca:
        
-  conflictManagerService.verifyConflictsByDentistCalendarLock
        
construyendo previamente un DentistCalendarLock temporal.
        
Este paso impide:
        
-  superposición de locks,
        
-  duplicación de intervalos,
        
- rangos inconsistentes dentro del modelo de agenda.
        

### 2.3. Verificar conflictos con turnos reservados
        
Previo al guardado se ejecuta:
        
-  appointmentService.getAppointmentsByConflictRange
        
-  Si en el rango del lock hay turnos ya asignados:
        
   - el bloqueo igualmente se crea,
        
   - el sistema informa la lista de turnos afectados,
        
   - no se genera un error que impida la operación.
        
El dentista o la administración deben tener la capacidad de bloquear igual, para luego gestionar manualmente la reprogramación con los pacientes.
        

### 2.4. Soporte para locks recurrentes
        
La entidad DentistCalendarLock permite:
        
-  DentistCalendarLockRange para rangos exactos,
        
-  DentistCalendarLockDay para días específicos,
        
-  DentistCalendarLockTime para rangos horarios,
        
-  RecurrenceType (DAILY, WEEKLY, MONTHLY, YEARLY).
        
Esto provee flexibilidad para gestionar ausencias repetitivas sin crear múltiples registros manuales.
        
    

### 2.5. Estructura relacional y lógica separada por concerns
        
Se definió una estructura modular:
        
-  Entidad principal: DentistCalendarLock
        
-  Componentes asociados: Range, Day, Time
        
-  Enumeraciones: DentistCalendarLockType, RecurrenceType, PriorityType
        
Servicios especializados:
        
   -  DentistCalendarLockServiceImpl
        
   -  DentistAvailabilityServiceImpl
        
   -  ConflictManagerService
        
   -  AppointmentService
     
   
Separar responsabilidades, asegurar reutilización y mantener el dominio escalable.
        
    

### 2.6. El bloqueo modifica la disponibilidad efectiva final
        
El lock funciona como una capa de exclusión aplicada sobre la disponibilidad base.
En el cálculo de turnos, un rango marcado como lock se considera no apto para reservas, respetando además sus reglas de recurrencia.

### 2.7. Bloqueos diarios por ausencias prolongadas (licencias, vacaciones, enfermedad)

Se definió un tipo especial de bloqueo denominado bloqueo diario por ausencia total, destinado a representar ausencias prolongadas y continuas del dentista, tales como:

-  Vacaciones

-  Licencias médicas

-  Licencias personales prolongadas

Estas ausencias presentan características funcionales distintas a otros bloqueos:

-  No representan excepciones parciales a la agenda.

-  No dependen de la jornada laboral habitual.

-  Afectan días completos de manera corrida entre dos fechas.

#### Reglas aplicadas

Cuando el tipo de evento (CalendarLockType) posee el flag absenceTotal = true, el sistema aplica las siguientes reglas:

El bloqueo:

-  Se interpreta como días corridos entre startDate y endDate.

-  Abarca toda la jornada diaria, independientemente de la disponibilidad configurada.

-  No se valida contra la jornada laboral del dentista.

No se permiten:

-  Días específicos (days)

-  Rangos horarios personalizados (startTime, endTime)

-  Recurrencias distintas de DAILY o ausencia de recurrencia

En estos casos, el sistema exige coherencia en los datos enviados y rechaza cualquier combinación incompatible mediante validaciones explícitas.

#### Justificación

Este enfoque permite:

-  Modelar correctamente licencias prolongadas sin fragmentarlas en múltiples bloqueos.

-  Evitar reglas artificiales que limiten el bloqueo solo a días laborales.

-  Mantener claridad semántica entre:

-  Bloqueos operativos (recurrentes, parciales, dependientes de jornada)

-  Ausencias estructurales del profesional (vacaciones/licencias)

De esta forma, el calendario final refleja fielmente la disponibilidad real del dentista.


## 3. Alternativas Consideradas

   
### 3.1. Permitir bloqueos sin validar disponibilidad

Descartado.
Hubiera permitido generar “bloqueos colgados” sin correlación con la agenda real del dentista, dificultando reportes, lógica de slots y consistencia de datos.



### 3.2. Forzar cancelación automática de turnos en conflicto

Descartado.
Implicaba mayor complejidad operativa y riesgo de impacto sobre pacientes.
Se decidió que el sistema alerte en la creación del lock en caso de conflicto con turnos, delegando decisiones de reprogramación al dentista.



### 3.3. Usar una sola tabla con rangos genéricos para disponibilidad y locks (modelo unificado)

Descartado.
Hubiera simplificado el modelo, pero:
        
-  se perdía claridad conceptual,
        
-  complicaba distinguir disponibilidad base de excepciones,
        
-  imposibilitaba reglas específicas para recurrentes.
        
-  El modelo actual separa “lo que el dentista trabaja” de “lo que el dentista bloquea”.



### 3.4. Simplificar y prohibir recurrencias

Descartado.
La mayoría de dentistas requiere recurrencias (cursos semanales, guardias, días dedicados a ortodoncia).
La recurrencia agrega valor operativo y evita múltiples registros manuales.


## 4. Consecuencias


### 4.1. Agenda consistente y sin superposiciones

La verificación de disponibilidad + conflictos evita datos incoherentes.



### 4.2. Flujo seguro para los pacientes

Los turnos existentes no se invalidan silenciosamente.
El sistema preserva la integridad operativa de la clínica.



### 4.3. Aumento de complejidad en la capa de dominio

El modelo requiere:
        
-  varias entidades complementarias,
        
-  recolección de rangos,
        
-  validaciones recursivas,
        
-  construcción de locks temporales,
        
-  manejo de reglas de recurrencia.
        
Sin embargo, esta complejidad es necesaria para un comportamiento confiable.



### 4.4. Simplificación futura en la generación de slots

Al unificar disponibilidad, locks y appointments, la capa de generación de slots recibe un calendario coherentizado.



## 5. Contrato de Input para Bloqueos de Calendario

###   5.1. Bloqueo puntual (fecha exacta)

Uso: Ausencia de un día, evento puntual.

Input esperado:

```json
{
  "startDate": "2025-12-27",
  "endDate": "2025-12-27",
  "days": [],
  "recurrence": "NONE"
}
```


Reglas:

-  days debe estar vacío.

-  recurrence debe ser NONE.

-  startDate == endDate.

No se generan detalles adicionales; la fecha se interpreta literalmente.


### 5.2. Bloqueo de algunos días en un rango sin recurrencia

Uso: “esta semana no atiendo lunes, miércoles y viernes”.

Input esperado:

```json
{
   "startDate": "2025-12-22",
   "endDate": "2025-12-28",
   "days": ["MONDAY", "WEDNESDAY", "FRIDAY"],
   "recurrence": "NONE"
}

```

Reglas:

-  days no puede estar vacío.

-  recurrence debe ser NONE.

-  startDate != endDate.

-  Las fechas reales se generan solo para los días de days dentro del rango.

-  No significa que se bloqueen todas las fechas del rango, solo las coincidencias con days.


### 5.3. Bloqueo diario continuo

Uso: ausencias prolongadas (vacaciones, licencias médicas).

Input esperado:

```json
{
   "startDate": "2025-01-10",
   "endDate": "2025-01-20",
   "days": [],
   "recurrence": "DAILY"
}


```

Reglas:

-  days debe estar vacío.

-  recurrence debe ser DAILY.

-  startDate != endDate.

-  Todas las fechas entre startDate y endDate se bloquean sin excepción.

No se generan detalles; se interpreta como días corridos reales.

### 5.4. Bloqueo recurrente por patrón

Uso: horarios fijos que no se atienden durante un período largo.

Input esperado:

```json
{
   "startDate": "2025-01-01",
   "endDate": "2025-03-31",
   "days": ["MONDAY", "WEDNESDAY"],
   "recurrence": "WEEKLY"
}

```

Reglas:

-  days no puede estar vacío.

-  recurrence puede ser WEEKLY, MONTHLY, YEARLY.

-  startDate != endDate.

Las fechas reales se calculan:

-  solo dentro del rango [startDate, endDate],

-  solo para días que coincidan con days,

-  respetando la recurrencia indicada.

-  Se generan detalles según corresponda.


### 5.5. Reglas generales de validación

startDate ≤ endDate.

Si days está vacío:

-  recurrence solo puede ser NONE o DAILY. 

-  startDate == endDate → bloqueo puntual.

-  startDate != endDate → bloqueo diario.

Si days NO está vacío:

-  recurrence DAILY es inválido.

-  recurrence NONE → patrón sin recurrencia.

-  recurrence WEEKLY/MONTHLY/YEARLY → patrón recurrente.

startDate y endDate:

-  son fechas reales solo en bloqueos puntuales o diarios.

-  son rango ancla en bloqueos por patrón.

No se permiten bloqueos superpuestos:

-  Se valida antes de persistir.

-  Si un bloqueo nuevo incluye fechas que ya están cubiertas por un bloqueo existente, se rechaza con ConflictException.

Horario de bloqueos:

-  El solapamiento se valida en intervalos [startTime, endTime] y cualquier intersección con otro lock existente es considerada conflicto.

-  La validación es total, no parcial: si un bloqueo A cubre toda la jornada y el nuevo B cae dentro, se considera conflicto.