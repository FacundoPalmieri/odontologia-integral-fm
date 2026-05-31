# ADR-0005 — Centralizar construcción del calendario en buildCalendarDay

- **Fecha**: 2025-12-13
- **Estado**: aceptado
- **Scope**: feature:appointments
- **Supersedes**: —
- **Related**: ADR-0003, ADR-0004

## Contexto
El calendario del dentista debe consultarse por día, semana y mes, combinando disponibilidad base, bloqueos y turnos reservados. Sin un punto central de construcción, cada vista requeriría replicar las mismas reglas de feriados, slots y estado del día.

## Decisión
Centralizar toda la lógica de construcción en un único método buildCalendarDay(dentistId, date) que genera un día completo con sus slots y estado final. Las vistas de semana y mes iteran sobre días y delegan en este método sin lógica propia.

## Alternativas consideradas
- **Lógica duplicada por vista (día/semana/mes)**: descartada porque cualquier cambio en las reglas del día requeriría actualizarse en múltiples lugares.
- **Precalcular y persistir el mes en BD**: descartada por complejidad innecesaria para el tamaño del proyecto.
- **Generar slots en el controlador**: descartada porque mezcla capas y dificulta el testeo.

## Consecuencias

**Positivas**:
- ✓ Un solo lugar para modificar reglas del calendario — feriados, bloqueos, duración de slot.
- ✓ Fácil de testear: el día es la unidad mínima y es independiente.
- ✓ Semana y mes son triviales — solo iteran días.

**Negativas / tradeoffs**:
- ✗ Vista mensual ejecuta N llamadas a buildCalendarDay — puede ser costoso sin caché si el mes tiene muchos días con turnos.
