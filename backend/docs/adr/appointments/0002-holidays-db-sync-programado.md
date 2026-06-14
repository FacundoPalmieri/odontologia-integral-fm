# ADR-0002 — Persistir feriados en DB con sync programado

- **Fecha**: 2025-12-08
- **Estado**: aceptado
- **Scope**: feature:appointments
- **Supersedes**: —
- **Related**: —

## Contexto
El sistema de turnos necesita conocer los feriados nacionales para impedir reservas en esas fechas. Se requería una fuente confiable de datos que no dependiera de la disponibilidad de un servicio externo en tiempo de ejecución.

## Decisión
Persistir los feriados en base de datos con restricción de unicidad por fecha. Sincronizar automáticamente con la API externa (argentinadatos.com) mediante una tarea programada. Exponer endpoints REST para gestión manual con control de acceso por rol.

## Alternativas consideradas
- **Solo en memoria**: descartada porque los datos se pierden al reiniciar la aplicación e impiden validaciones de calendario.
- **Consultar API externa en tiempo real por cada request**: descartada por sobrecarga de red, latencia y dependencia del servicio externo ante cada validación de turno.

## Consecuencias

**Positivas**:
- ✓ El sistema funciona sin depender de la disponibilidad de la API externa en tiempo real.
- ✓ Permite gestión manual de feriados como fallback ante datos incorrectos o ausentes.
- ✓ Consultas rápidas desde DB sin latencia de red.

**Negativas / tradeoffs**:
- ✗ Requiere tarea programada activa y monitoreo — si falla el sync, los feriados pueden estar desactualizados.
- ✗ Datos pueden quedar desfasados si la API externa actualiza feriados fuera del ciclo de sync.
