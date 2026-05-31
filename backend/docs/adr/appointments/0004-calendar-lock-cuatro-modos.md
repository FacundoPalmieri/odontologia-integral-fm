# ADR-0004 — Modelar bloqueos de calendario con 4 modos explícitos

- **Fecha**: 2025-12-10
- **Estado**: aceptado
- **Scope**: feature:appointments
- **Supersedes**: —
- **Related**: ADR-0003

## Contexto
Los dentistas necesitan bloquear su agenda ante situaciones que no forman parte de su disponibilidad habitual (vacaciones, guardias, cursos recurrentes). La disponibilidad base no contempla estas excepciones dinámicas. Se requería un modelo que soporte desde un bloqueo puntual hasta ausencias prolongadas con o sin recurrencia.

## Decisión
Modelar los bloqueos como una capa de exclusión independiente sobre la disponibilidad, con 4 modos explícitos y semántica propia: POINTUAL (fecha exacta), DAYS_IN_RANGE_NO_RECURRENCE (días específicos en un rango), DAILY_CONTINUOUS (ausencia prolongada corrida) y RECURRENT_PATTERN (patrón semanal recurrente). Los modos DAILY_CONTINUOUS no validan cobertura contra disponibilidad. El resto sí.

## Alternativas consideradas
- **Modelo unificado de disponibilidad y locks en una sola tabla**: descartada porque mezcla conceptos distintos e impide reglas específicas por modo.
- **Prohibir recurrencias**: descartada porque la mayoría de dentistas necesita bloqueos recurrentes para actividades fijas.
- **Permitir bloqueos sin validar cobertura**: descartada porque genera bloqueos sin correlación con la agenda real.

## Consecuencias

**Positivas**:
- ✓ Cada modo tiene semántica clara — el código documenta explícitamente los casos soportados.
- ✓ Facilita extensión futura incorporando nuevos modos sin romper los existentes.
- ✓ Separación limpia entre disponibilidad base y excepciones.

**Negativas / tradeoffs**:
- ✗ Mayor complejidad en la capa de dominio — validaciones, recurrencias y construcción de locks temporales por modo.
- ✗ Turnos en conflicto no se cancelan automáticamente — requiere gestión manual por parte del dentista.
