## ADR — Dentist Availability (disponibilidad de dentista)

###  Fecha: 2025-12-10

### Contexto: Feature Appointment

## 1. Contexto y Problema

La aplicación SaaS gestiona turnos odontológicos. Los dentistas deben poder declarar jornadas laborales con flexibilidad máxima: pueden declarar días puntuales o jornadas recurrentes (diarias, semanales, quincenales, mensuales, anuales).

La disponibilidad configurada debe:

-   Generar slots (según appointmentDuration) para que el frontend muestre/permita reservas.

-   Servir como referencia para validar nuevos bloqueos de calendario (calendar locks) y para detectar conflictos con turnos ya reservados.

-   Poder cambiarse completamente por el dentista; al actualizar, las disponibilidades previas se deshabilitan y se persiste la nueva configuración.
    
-   Calcular una effectiveDate para las disponibilidades recurrentes: el primer día dentro de los próximos 7 días que coincide con el dayName (clave semántica del día de semana).
    
El objetivo del ADR es dejar por escrito la decisión de diseño, sus motivos, alternativas consideradas y efectos operativos.


## 2. Decisiones tomadas

### 2.1 Se decidió soportar 2 modos excluyentes por entrada:
    
-   Modo puntual: specificDate (una fecha única).
        
-   Modo recurrente: dayName + recurrence (por ejemplo MONDAY + WEEKLY).
        
-   Nunca convivir specificDate y dayName/recurrence.
        
-   effectiveDate:
        
    - Si specificDate → effectiveDate = specificDate.
        
    - Si recurrente → backend calcula primer match del dayName dentro de los próximos 7 días a partir de la fecha de modificación (se usa now().plusDays(1) al crear/actualizar para evitar solapes).
        


### 2.2 Cambios en la jornada:
    
-   Las disponibilidades actuales se deshabilitan (soft delete / enabled = false).

        
-   Se persisten las nuevas disponibilidades con auditoría (createdAt, createdBy).

        
-   Se detectan conflictos con turnos reservados y se notifica al staff.

        
-   Validación de cobertura:
        
    - Al crear/editar bloqueos de calendario (calendar locks) se valida si todas las fechas efectivas del bloqueo están cubiertas por alguna disponibilidad del dentista (horario + recurrencia).
        

-   Motor de conflictos:
        
    - Detecta conflictos futuros, compara con conflictos existentes y persiste CREATE / REOPEN / RESOLVE según corresponda.
        
     
#### Modelo de datos
        
-   Entidad principal: DentistAvailability (tabla dentist_availabilities, cláusula @Where(enabled = true)):
        
    -   id
    - dentist (FK)
    - keyName (DayName)
    - recurrence (CalendarLockRecurrenceName)
    - specificDate
    - effectiveDate
    - startTime
    - endTime
    - appointmentDuration
    - auditoría
    - enabled.
        


-   DTO de entrada/respuesta: WorkingDayDTO con las validaciones:
        
    -   isExclusiveChoice() y isRecurrenceValid() garantizan specificDate XOR dayName y specificDate XOR recurrence.
        

#### Flujo de creación/actualización

Servicio: DentistAvailabilityService.create(id, List<WorkingDayDTO> days):
        
-   Valida existencia de Dentist.
        
-   Busca disponibilidades activas y las deshabilita (disabledAvailability()).

Para cada WorkingDayDTO:
        
-   Crea DentistAvailability con:
        
    -   effectiveDate = (specificDate == null) ? conflictManagerService.findFirstMatchingDate(LocalDate.now().plusDays(1), dto.dayName.toDayOfWeek()) : specificDate
        
    -   createdAt = now(), createdBy = authenticatedUser
        
    -   Persiste todos (saveAll).
        
    -   Actualiza DTOs resultantes con effectiveDate, idOriginConflict, originConflict para poder pasar al ConflictManagerService.
        
    -   Ejecuta conflictManagerService.verifyConflictsByDentistAvailability(id, days):
        
    -   Reevalúa turnos futuros, detecta conflictos, compara con los existentes y persiste los cambios (CREATE / REOPEN / RESOLVED).
        
    -   Retorna DentistAvailabilityResponseDTO con disponibilidades y conflictos detectados.




    
### 2.3 Servicio de detección y gestión de conflictos
    
Servicio: ConflictManagerService provee:
        
-   findFirstMatchingDate(startDate, DayOfWeek) → busca el primer día igual dentro de los próximos 7 días, retorna startDate si no encuentra.

- generateEffectiveDates(startDate, endDate, recurrence) → genera el conjunto de fechas del bloqueo según recurrencia (NONE, DAILY, WEEKLY, BIWEEKLY, MONTHLY, YEARLY).
        
-   validateRecurrence(recurrence, startDate, currentDate) → valida si currentDate corresponde a la recurrencia referenciada en startDate.
        
-   hasAppointmentMatchWithEvent(...) → determina si una cita cae dentro de un evento/bloqueo según fecha, día, recurrencia y horario.
        
-   hasBlockMatchWithAvailability(availabilities, startDateBlock, endDateBlock, startTimeBlock, endTimeBlock, recurrenceBlock) → valida que todas las fechas del bloqueo estén completamente cubiertas por alguna disponibilidad.
        
#### Gestión de conflictos
        
verifyConflictsByDentistAvailability(idDentist, days):
        
-   Busca turnos futuros (findFutureAppointmentsReservedByDentist).
        
-   Obtiene conflictos existentes (getAllByDentistIdAndAvailabilityConflict).
        
-   Llama conflictDetectorByDentistAvailability(appointments, days) → genera lista de AppointmentConflict.
        
-   compareConflictNewWithDataBase() → retorna listas para CREATE/REOPEN/RESOLVED/FINAL.
        
-   Persiste y notifica por email.
        
Se toma esta decisión por varias razones clave:
        
-   Simplicidad y claridad para el usuario final (dentista): un único modelo (DentistAvailability) soporta los dos modos (puntual y recurrente) y se valida en DTO, evitando duplicar entidades.
        
-   Determinismo en effectiveDate: calcular el primer día dentro de los 7 días evita ambigüedades del inicio de la recurrencia y alinea el comportamiento con la expectativa del dentista (la jornada empieza cuando coincide el día de la semana más cercano).
        
-   Inmutabilidad parcial y auditoría: deshabilitar en lugar de sobreescribir facilita auditoría, rollbacks lógicos y evita race conditions con turnos ya generados.
        
-   Separación de responsabilidades: DentistAvailabilityService persiste disponibilidades; ConflictManagerService se encarga de la detección/gestión de conflictos. Esto respeta SRP y facilita tests.
        
-   Escalabilidad lateral: la aproximación permite añadir fácilmente detección de conflictos para CalendarLock reutilizando las mismas utilidades (generateEffectiveDates, validateRecurrence, hasAppointmentMatchWithEvent).


## 3. Alternativas consideradas


-   Guardar sólo reglas y calcular fechas a demanda 

    -   Pros: estandarización, más expresivo para recurrencias complejas.

    -   Contras: mayor complejidad de parsing, dependencia de librería (p. ej. ical4j), curva de mantenimiento; para requerimientos actuales (recurrencias simples), overhead innecesario.
    
    Decisión: posponer. Considerar RRULE si aparecen reglas más complejas (p. ej. “primer lunes cada 2 meses” no cubierto).


    
-   Mantener disponibilidades y slots pre-generados en BD
    
    -   Pros: consultas rápidas para frontend.
    
    -   Contras: coste en espacio y complejidad al actualizar (tendría que regenerar/invalidar muchos records y mantener sincronía con turnos).
    
    Decisión: generar slots a demanda en el servicio/endpoint a partir de startTime, endTime, appointmentDuration y effectiveDate; si el rendimiento lo exige, añadir caché o tabla de slots materializada con proceso de regeneración.
  

  
-   No deshabilitar disponibilidades previas, sobrescribirlas
    
    -   Pros: simplicidad de update.
    
    -   Contras: pérdida de auditoría, complicaciones para detectar cambios históricos, mayor riesgo de race conditions con turnos.
    
    -   Decisión: mantener el enfoque de enabled = false.
    

-   Calcular effectiveDate como el primer match desde now() (sin plusDays(1))
    
    Consideración: se eligió now().plusDays(1) para evitar que una actualización afecte inmediatamente al día actual y choque con turnos/slots ya creados el mismo día.


## 4. Consecuencias

-   La solución implementada permite a los dentistas gestionar sus disponibilidades de forma flexible y clara, minimizando errores y conflictos con turnos ya reservados.

-   Cambios de jornada generan un proceso de reconciliación sobre turnos futuros que puede ser costoso si hay muchos turnos; se envía email al staff con los conflictos detectados.

-   Las actualizaciones se realizan en una transacción y deshabilitan las disponibilidades previas, lo que facilita rollback lógico.




