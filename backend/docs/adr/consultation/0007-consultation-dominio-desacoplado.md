# ADR-0007 — Modelar consulta odontológica como dominio desacoplado

- **Fecha**: 2025-12-13
- **Estado**: aceptado
- **Scope**: feature:consultation
- **Supersedes**: —
- **Related**: —

## Contexto
El consultorio no contaba con un sistema digitalizado. La información clínica era manual y sin estructura. Se necesitaba modelar el proceso completo de atención — desde la recepción hasta el cierre — con trazabilidad entre diagnóstico, tratamiento y facturación.

## Decisión
Modelar la consulta como un dominio desacoplado con entidades de responsabilidad específica: Consultation (raíz y ciclo de vida), ConsultationInstance (cada visita clínica), PrestationInstance (tratamiento ejecutado) y OdontogramDetail (diagnóstico por pieza dental). Cada entidad tiene su propia lógica y puede evolucionar de forma independiente.

## Alternativas consideradas
- **Modelo simple centrado solo en Consultation**: descartada porque no permite múltiples instancias de atención, no soporta evolución clínica entre visitas y dificulta integrar odontograma y prestaciones de forma consistente.

## Consecuencias

**Positivas**:
- ✓ Separación clara de responsabilidades — cada entidad modela un concepto clínico real.
- ✓ Soporta múltiples visitas dentro de una misma consulta con trazabilidad completa.
- ✓ Permite integrar pagos, promociones y odontograma sin acoplarlos a la entidad raíz.

**Negativas / tradeoffs**:
- ✗ Mayor complejidad de implementación — más entidades, más validaciones de negocio y más relaciones a mantener.
- ✗ Requiere buena documentación y testing para no perder consistencia entre dominios.
