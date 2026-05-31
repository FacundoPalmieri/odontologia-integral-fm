# ADR-0009 — Separar Consultation de ConsultationInstance en relación 1:1

- **Fecha**: 2025-12-13
- **Estado**: aceptado
- **Scope**: feature:consultation
- **Supersedes**: —
- **Related**: ADR-0007

## Contexto
La atención odontológica tiene dos conceptos con ciclos de vida distintos: el flujo general de la consulta (llegada, espera, atención, cierre) y el acto clínico en sí (odontograma, prestaciones, pagos). Modelarlos en una sola entidad mezclaría responsabilidades administrativas y clínicas.

## Decisión
Separar en dos entidades con relación 1:1: Consultation representa el flujo general y el ciclo de vida de la atención; ConsultationInstance representa el acto clínico concreto ejecutado en esa visita. Cada Consultation tiene exactamente una ConsultationInstance — se valida unicidad al crear.

## Alternativas consideradas
- **Una única entidad Consultation sin ConsultationInstance**: descartada porque acopla el ciclo de vida administrativo con los datos clínicos, dificultando extender o razonar sobre cada responsabilidad de forma independiente.

## Consecuencias

**Positivas**:
- ✓ Separación limpia entre flujo administrativo y acto clínico.
- ✓ Consultation puede cambiar de estado (recepción, espera, cierre) sin afectar los datos clínicos.
- ✓ ConsultationInstance concentra toda la información médica — odontograma, prestaciones, pagos.

**Negativas / tradeoffs**:
- ✗ Requiere validación explícita de unicidad al crear ConsultationInstance — error si ya existe una para esa Consultation.
- ✗ Queries que necesiten datos de ambos conceptos requieren JOIN entre las dos tablas.
