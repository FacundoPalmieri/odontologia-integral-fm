# ADR-0003 — Modelar disponibilidad del dentista con modos excluyentes y soft delete

- **Fecha**: 2025-12-10
- **Estado**: aceptado
- **Scope**: feature:appointments
- **Supersedes**: —
- **Related**: ADR-0002

## Contexto
El sistema necesita que cada dentista declare cuándo trabaja, soportando tanto fechas puntuales como jornadas recurrentes (diaria, semanal, quincenal, etc.). Al actualizar la jornada, los cambios deben auditarse y no afectar turnos ya generados.

## Decisión
Usar una única entidad DentistAvailability con dos modos excluyentes: puntual (specificDate) o recurrente (dayName + recurrence). Al actualizar la jornada, las disponibilidades activas se deshabilitan (enabled=false) y se persisten las nuevas. Nunca se sobrescriben registros existentes.

## Alternativas consideradas
- **Entidades separadas para disponibilidad puntual y recurrente**: descartada porque duplicaría lógica de validación, detección de conflictos y generación de slots.
- **Sobrescribir disponibilidades al actualizar**: descartada por pérdida de auditoría y riesgo de race conditions con turnos ya reservados.

## Consecuencias

**Positivas**:
- ✓ Modelo simple: un único DTO y una única entidad manejan ambos casos.
- ✓ Historial de cambios auditables — enabled=false preserva el registro anterior.
- ✓ Facilita detección de conflictos al cambiar jornada (turnos futuros afectados).

**Negativas / tradeoffs**:
- ✗ La tabla acumula registros deshabilitados — requiere @Where(enabled=true) en todas las queries para no leer datos obsoletos.
- ✗ Cambios de jornada disparan reconciliación de turnos futuros, que puede ser costosa con muchos turnos.
