# ADR-0008 — Persistir odontograma y prestaciones como registros delta

- **Fecha**: 2025-12-13
- **Estado**: aceptado
- **Scope**: feature:consultation
- **Supersedes**: —
- **Related**: ADR-0007

## Contexto
El odontograma y las prestaciones evolucionan visita a visita. Se necesitaba una estrategia de persistencia que permitiera reconstruir el estado clínico en cualquier fecha sin duplicar información ni generar inconsistencias ante correcciones.

## Decisión
Persistir solo los cambios de cada visita (delta): el frontend envía únicamente los dientes o prestaciones nuevas o modificadas. Cada registro es inmutable — nunca se actualiza, solo se inserta uno nuevo. El estado actual se reconstruye con la query del último registro por elemento (ORDER BY date DESC). Un snapshot en fecha X se obtiene filtrando por fecha.

## Alternativas consideradas
- **Copiar el estado completo del odontograma en cada visita**: descartada porque cualquier corrección requeriría actualizar múltiples registros históricos y genera riesgo de inconsistencia entre visitas.

## Consecuencias

**Positivas**:
- ✓ Historial completo y auditable — cada cambio queda registrado con fecha y visita.
- ✓ Snapshot en cualquier fecha sin lógica adicional — solo filtrar por fecha.
- ✓ Sin riesgo de inconsistencia entre visitas — los registros son inmutables.

**Negativas / tradeoffs**:
- ✗ La tabla crece con cada visita — requiere queries con ORDER BY y posible índice por paciente + fecha.
- ✗ El estado actual no es inmediato — siempre se reconstruye por query.
