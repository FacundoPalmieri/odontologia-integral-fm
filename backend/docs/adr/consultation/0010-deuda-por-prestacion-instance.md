# ADR-0010 — Gestionar deuda financiera por PrestationInstance, no por ConsultationInstance

- **Fecha**: 2026-06-07
- **Estado**: aceptado
- **Scope**: feature:consultation
- **Supersedes**: —
- **Related**: ADR-0007, ADR-0009

## Contexto
Una prestación odontológica puede ejecutarse en múltiples consultas (ej: tratamiento de conducto en 3 sesiones). El valor corresponde al tratamiento completo — los pasos individuales no tienen precio propio. Asociar la deuda a ConsultationInstance generaba la pregunta: ¿en cuál de las 3 sesiones se registra? Además, el pago puede ocurrir en cualquier momento, independientemente del avance clínico: un paciente puede saldar la deuda en la primera visita aunque falten 2 steps.

## Decisión
La unidad de deuda financiera es PrestationInstance. Payment apunta a Patient (quién paga) y a Consultation de forma nullable (trazabilidad de visita). PaymentDetail imputa cada pago a una o más PrestationInstance. El saldo del paciente se calcula como la suma de (PrestationInstance.finalAmount − pagos imputados) por prestación. ConsultationStatus es puramente clínico: WAITING_ROOM → IN_CONSULTATION → FINISHED.

## Alternativas consideradas
- **Deuda por ConsultationInstance** (modelo original): Payment apuntaba directamente a ConsultationInstance. Descartada porque rompe con prestaciones multi-step — la deuda no puede asociarse a una sola sesión cuando el tratamiento abarca varias consultas. Además, mezclaba el estado clínico con el financiero en un único campo (PENDING_PAYMENT).
- **Solo paymentDate sin referencia a Consultation**: descartada porque la fecha sola no identifica unívocamente la visita — un paciente puede tener múltiples consultas el mismo día. Matchear por fecha sería una query frágil. La referencia directa nullable es más precisa y eficiente.

## Consecuencias

**Positivas**:
- ✓ Modelo correcto para prestaciones multi-step: la deuda vive en el objeto facturable, no en la sesión.
- ✓ Estado clínico y financiero son independientes — una consulta puede estar FINISHED con saldo pendiente.
- ✓ Saldo del paciente calculable con precisión como suma de saldos por prestación.
- ✓ Un pago puede imputarse a prestaciones de distintas consultas y en cualquier estado (IN_PROGRESS, COMPLETED, CANCELLED).
- ✓ Flexibilidad ante el caso futuro de pago sin visita presencial (consultationId queda null).

**Negativas / tradeoffs**:
- ✗ Payment actual (apunta a ConsultationInstance) debe rediseñarse antes de implementar el UC de pago — hay deuda técnica sobre el modelo existente.
- ✗ Calcular el saldo del paciente requiere agregar sobre PaymentDetail por prestación — query más compleja que leer un campo directo.
- ✗ La UI debe mostrar estado clínico y saldo financiero como dos dimensiones separadas — mayor responsabilidad de composición en frontend.

## Notas
Payment.consultationId es nullable por diseño: si el paciente paga sin venir al consultorio, la referencia queda null sin romper el modelo. No es un campo opcional por descuido — es flexibilidad intencional documentada acá para que no se lo "corrija" como NOT NULL en el futuro.