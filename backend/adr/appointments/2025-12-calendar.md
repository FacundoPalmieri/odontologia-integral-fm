## ADR – Lógica del Calendario del Dentista.

### Contexto:
Implementar una agenda que muestre disponibilidad del dentista por día, semana y mes, considerando turnos, bloqueos, feriados y disponibilidad habitual.

## 1. Problema

   La disponibilidad real de un dentista no depende solo de horario habitual:

   -   Hay feriados (que puede o no trabajar)

   -   Hay bloqueos manuales/recurrentes

   -   Hay turnos ya reservados

   -   Hay una duración fija de slot
   

   La agenda debe consultarse por día/semana/mes, por lo que se necesita una solución limpia y mantenible que no duplique lógica.


## 2. Decisión principal

Centralizar todo en un método único:

-   buildCalendarDay(dentistId, date)

Este método genera un día completo, y todo el resto (semana y mes) solo lo reutiliza.

## 3. Flujo del día

### 1) Validar dentista

Si no existe → error.

### 2) Obtener disponibilidad del día

Si no hay → día marcado como NOT_AVAILABLE.

### 3) Ver si es feriado

Si NO es feriado → usar disponibilidad normal

Si es feriado y el dentista no lo trabaja → día NOT_AVAILABLE

Si lo trabaja → usar la jornada especial del feriado

### 4) Generar slots

Con:

-  inicio

-  fin

-  duración del turno

generateSlot(startTime, endTime, duration)

### 5) Rellenar slots

-  El bloqueo tiene prioridad

-  Si no hay bloqueo, ver si hay turno reservado

-  Si no hay nada, queda FREE

fillSlot(slots, appointments, locks)

### 6) Evaluar estado global del día
   deriveDayStatus(slots)

## 4. Semana y mes

No tienen lógica propia.
Simplemente hacen:

for each day → buildCalendarDay


Mes devuelve un DTO más liviano (estado global por día).

## 5. ¿Por qué esta decisión?

-  Evita duplicar código para día/semana/mes

-  Es fácil de testear

-  Permite extender: nuevos tipos de bloqueos, turnos especiales, etc.

-  Es simple de mantener en un proyecto freelance

-  El día es la “unidad mínima” y todo se construye sobre eso

## 6. Alternativas evaluadas (rápido)

-  Duplicar lógica en semana/mes → descartado, difícil de mantener

-  Precalcular todo el mes en BD → demasiado para un SaaS chico

-  Generar slots en controlador → mezcla capas, difícil de escalar

## 7. Resultado

La implementación permite construir el calendario mensual de forma consistente a partir de:
- la disponibilidad configurada del odontólogo,
- los bloqueos (simples o recurrentes),
- los turnos reservados existentes.

Cada día se deriva a un estado único (FREE, FULL, LOCKED o NOT_AVAILABLE), lo que simplifica la UI y evita lógica duplicada en el frontend.  
La estructura modular facilita extender reglas futuras (feriados, tipos de turno, buffers entre turnos, etc.) sin romper la arquitectura.
