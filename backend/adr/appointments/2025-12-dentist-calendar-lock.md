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

### 2.1. Validar que el bloqueo esté dentro de la disponibilidad real del dentista solo para casos de recurrencia NO DIARIA. 

- Para los casos Daily(vacaciones) no se valida la jornada.

- Para el resto de los casos se ejecuta el método validateCoverage. Esto garantiza que el rango del lock intersecte un rango de disponibilidad real.

#### Resultado: no se permiten “bloqueos fantasma” que no tengan sentido operativo en los bloqueos NO DIARIOS.


### 2.2. Verificar conflictos con otros Calendar Locks
        
Se invoca:
        
-  conflictManagerService.verifyConflictsByDentistCalendarLock
        
construyendo previamente un DentistCalendarLock temporal.
        
Este paso impide:
        
-  superposición de locks,
        
-  duplicación de intervalos,
        
- rangos inconsistentes dentro del modelo de agenda.
        

### 2.3. Verificar conflictos con turnos reservados "createPreview"
        
Se cuenta "createPreview" que ejecuta la lógica para detectar y devolver al cliente los posibles conflictos con turnos existentes ante un nuevo bloqueo a crear.


En caso de decidir crear el bloqueo, previo al guardado se ejecuta:
        
- verifyLockMatchWithLock
        
-  Si en el rango del lock hay turnos ya asignados:
        
   - el bloqueo igualmente se crea,
        
   - el sistema informa la lista de turnos afectados,
        
   - no se genera un error que impida la operación.
        
El dentista o la administración deben tener la capacidad de bloquear igual, para luego gestionar manualmente la reprogramación con los pacientes.
        

 


### 2.4. El bloqueo modifica la disponibilidad efectiva final
        
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

Existen tres modalidades principales de bloqueo:

1) Bloqueo puntual - POINTUAL

Descripción: Bloqueo único en una fecha específica.

- days: null o vacío.
- mode: POINTUAL
- recurrence: NONE
- startDate == endDate -> Se interpreta como un bloqueo para un único día específico-

Persistencia:
- DentistCalendarDetail: No se registra nada.

Justificación: Un bloqueo puntual no puede repetirse ni depender de patrones semanales.


2) Bloqueo de varios días en una misma semana sin recurrencia - DAYS_IN_RANGE_NO_RECURRENCE

Descripción: Bloqueo de días específicos dentro de un rango de fechas, sin recurrencia automática

- days: obligatorio
- mode: DAYS_IN_RANGE_NO_RECURRENCE
- recurrence: NONE
- startDate != endDate -> Se interpreta como un bloqueo para varios días en una misma semana sin recurrencia

Persistencia:
- DentistCalendarDetail: Se registran los días

Justificación: El rango define el período de validez y los días determinan cuándo se aplica el bloqueo.


3) Bloqueo de varios continuos  - DAILY_CONTINUOUS

Descripción: Bloqueo diario continuo en un rango de fechas (por ejemplo: vacaciones o licencias).

- days: null o vacío.
- mode: DAILY_CONTINUOUS
- recurrence: DAILY
- startDate != endDate -> startDate debe ser distinta de endDate

Persistencia:
- DentistCalendarDetail: No se registra nada.

Justificación: El bloqueo aplica todos los días del rango, sin necesidad de discriminar días de la semana.


3) Bloqueo de días con recurrencia - RECURRENT_PATTERN

Descripción: Bloqueo recurrente basado en un patrón semanal (por ejemplo: todos los lunes y miércoles).

- days: Obligatorio
- mode: RECURRENT_PATTERN
- recurrence: No puede ser NONE
- startDate != endDate -> aplica como fechas anclas y la ventana de fechas debe coincidir para que se cumpla la recurrencia. Ej -> recurrence BIWEEKLY la fecha de inicio y fin debe tener al mínimo 14 días.

Persistencia:
- DentistCalendarDetail: Se registran los días

Justificación: La recurrencia define la frecuencia y los días especifican el patrón concreto.




#### Justificación

Este enfoque permite:

-  Se garantiza consistencia semántica de los bloqueos desde el momento de creación.

-  Se evita lógica defensiva posterior en capas de negocio o generación de eventos.

-  El código documenta explícitamente los casos de uso soportados.

-  Facilita la extensión futura mediante la incorporación de nuevos modos


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

###   5.1. Bloqueo puntual (fecha exacta) - POINTUAL

Input esperado:

```json
{
   "idLockType": 3,
   "mode": "POINTUAL",
   "days": [],
   "recurrence": "NONE",
   "startDate": "2026-01-12",
   "endDate": "2026-01-12",
   "startTime": "09:00:00",
   "endTime": "17:00:00",
   "observation": "Prueba"
}
```



### 5.2. Bloqueo de algunos días en un rango sin recurrencia - DAYS_IN_RANGE_NO_RECURRENCE


Input esperado:

```json
{
   "idLockType": 2,
   "mode": "DAYS_IN_RANGE_NO_RECURRENCE",
   "days": ["MONDAY"],
   "recurrence": "NONE",
   "startDate": "2026-01-05",
   "endDate": "2026-01-11",
   "startTime": "09:00:00",
   "endTime": "17:00:00",
   "observation": "Prueba"
}
```




### 5.3. Bloqueo diario continuo - DAILY_CONTINUOUS



Input esperado:

```json
{
   "idLockType": 1,
   "mode": "DAILY_CONTINUOUS",
   "days": [],
   "recurrence": "DAILY",
   "startDate": "2026-01-05",
   "endDate": "2026-01-11",
   "startTime": "09:00:00",
   "endTime": "17:00:00",
   "observation": "Prueba"
}
```


### 5.4. Bloqueo recurrente por patrón - RECURRENT_PATTERN

Input esperado:

```json
{
   "idLockType": 3,
   "mode": "RECURRENT_PATTERN",
   "days": ["MONDAY", "TUESDAY"],
   "recurrence": "WEEKLY",
   "startDate": "2026-01-05",
   "endDate": "2026-01-19",
   "startTime": "09:00:00",
   "endTime": "17:00:00",
   "observation": "Prueba"
}
```
