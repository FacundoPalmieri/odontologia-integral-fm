# Feature: Gestión de Consultas Odontológicas (Consultation)
### Fecha: 13/12/2025

---

## Objetivo
Implementar un sistema integral para gestionar consultas odontológicas que permita:

- Centralizar la información clínica del paciente
- Registrar cada instancia de atención dentro de una consulta
- Gestionar prestaciones (tratamientos) realizadas
- Integrar odontograma, promociones, descuentos y pagos
- Garantizar trazabilidad completa desde la apertura hasta el cierre de la consulta

---

## Contexto

Actualmente el consultorio odontológico **no cuenta con una aplicación** para la gestión de consultas.

Toda la operatoria se realiza de forma manual (papel, memoria del profesional o herramientas no integradas), lo que implica que:

- No existe una entidad central que represente la consulta odontológica
- La información clínica no está estructurada ni normalizada
- El seguimiento del paciente depende de fichas físicas.
- No hay integración entre diagnóstico, tratamiento y facturación

Como resultado, el proceso completo de atención (desde la recepción hasta el cierre) carece de trazabilidad, consistencia y soporte tecnológico.

Esta situación limita la capacidad del consultorio para escalar, auditar información y mejorar la calidad del servicio.

---

## Problema

Esta forma de trabajo genera:

- Falta de trazabilidad sobre las consultas realizadas
- Dificultad para reconstruir el historial de atenciones de un paciente
- Riesgo de inconsistencias o pérdida de información clínica
- Ausencia de un flujo claro desde la recepción hasta el cierre de la consulta
- Desacople entre diagnóstico clínico y prestaciones ejecutadas
- Falta de control sobre precios, promociones y descuentos aplicados

---

##  Opciones evaluadas

### Opción 1: Modelo simple centrado solo en Consultation
- ✔ Pros:
    - Fácil implementación
    - Menor complejidad inicial
- ❌ Contras:
    - No permite múltiples instancias de atención
    - No soporta evolución clínica
    - Difícil integrar odontograma y prestaciones
    - Baja escalabilidad

---

### Opción 2: Modelo desacoplado por dominios (Consultation + Instances + Prestations + Odontograma)
- ✔ Pros:
    - Alta escalabilidad
    - Permite trazabilidad completa
    - Separación clara de responsabilidades
    - Soporta múltiples atenciones dentro de una consulta
    - Permite modelar odontograma y prestaciones de forma consistente
    - Integración con pagos, promociones y pricing versionado
- ❌ Contras:
    - Mayor complejidad de implementación
    - Requiere más validaciones de negocio

---

## Decisión

Se elige la **Opción 2: Modelo desacoplado por dominios**.

### Justificación

Se prioriza un modelo robusto y extensible que permita:

- Representar correctamente la realidad del dominio odontológico
- Soportar crecimiento futuro (nuevos tratamientos, reglas, pricing, etc.)
- Mantener consistencia entre diagnóstico, tratamiento y facturación

### Decisiones técnicas clave

- Separación entre:
    - **Consultation** (entidad raíz)
    - **ConsultationInstance** (evolución clínica)
    - **PrestationInstance** (tratamientos ejecutados)
    - **OdontogramDetail** (diagnóstico por pieza dental)
- Uso de enums para estados y tipos (status, scope, descuentos)
- Versionado de precios mediante `PrestationTypePrice`
- Aplicación exclusiva de **promoción o descuento manual (nunca ambos)**
- Soporte para prestaciones con y sin ubicación (odontograma)
- Modelo de persistencia **delta** para odontograma y prestaciones: solo se persisten cambios o registros nuevos, nunca copias completas por visita
- `PrestationStepInstance` referencia la `ConsultationInstance` en la que fue ejecutado, permitiendo reconstruir el estado por fecha
- `Consultation` y `ConsultationInstance` tienen relación **1:1**: cada visita genera exactamente una de cada una
- El request al crear una `ConsultationInstance` está dividido en tres listas independientes: `odontogram[]`, `prestationNew[]` y `stepAdvancements[]`

---

## Diseño

### Modelo conceptual

- **Consultation**: representa una atención completa del paciente
- **ConsultationInstance**: cada interacción clínica dentro de la consulta
- **Patient**: paciente asociado
- **Dentist**: profesional que atiende
- **OdontogramDetail**: diagnóstico sobre piezas dentales
- **Treatment / TreatmentCondition**: definición clínica
- **PrestationType**: catálogo de prestaciones
- **PrestationInstance**: ejecución real de una prestación
- **PrestationStep / PrestationStepInstance**: workflow de pasos
- **Promotion**: descuentos predefinidos
- **Payment / PaymentDetail**: gestión de pagos

---

### Relaciones

- Una **Consultation** pertenece a un **Patient**
- Una **Consultation** pertenece a un **Dentist**
- Una **Consultation** tiene un **Appointment**
- Una **Consultation** tiene múltiples **ConsultationInstance**

- Una **ConsultationInstance**:
    - pertenece a una Consultation
    - tiene múltiples **OdontogramDetail**
    - tiene múltiples **PrestationInstance**
    - tiene múltiples **Payment**

- Un **OdontogramDetail**:
    - pertenece a una ConsultationInstance
    - referencia un **Treatment**
    - tiene un **TreatmentCondition**

- Un **PrestationInstance**:
    - pertenece a una ConsultationInstance
    - referencia un **PrestationType**
    - puede estar asociado a un **OdontogramDetail** (opcional)
    - puede tener múltiples **PrestationStepInstance**

- Un **PrestationStepInstance**:
    - se crea en un ConsultationInstance
    - avanza mediante relación a ConsultationInstance



- Un **PrestationType**:
    - tiene múltiples **Treatment**
    - tiene múltiples **PrestationStep**
    - tiene precios versionados (**PrestationTypePrice**)

- Un **Payment**:
    - pertenece a una ConsultationInstance
    - tiene múltiples **PaymentDetail**

- Un **PaymentDetail**:
    - pertenece a un Payment
    - referencia una PrestationInstance

---

### Reglas de negocio

#### Consultation

- Una consulta tiene un estado (`ConsultationStatusType`)
- Debe estar asociada a un paciente y un odontólogo

---

#### ConsultationInstance

- Representa una evolución clínica
- Permite registrar observaciones (texto libre)

---

#### Odontograma

- Cada **OdontogramDetail**:
    - Debe tener un `TreatmentCondition`
    - Puede o no tener `ToothFace`
- Permite representar:
    - Diagnóstico preexistente
    - Tratamientos requeridos
    - Tratamientos en progreso o finalizados

##### Modelo de persistencia (delta)

- Cada registro es una **observación inmutable**: nunca se modifica, solo se inserta uno nuevo cuando algo cambia.
- El frontend envía **solo los dientes que tuvieron algún cambio o son nuevos** en esa visita.
- **Estado actual de la boca** = último registro por diente para ese paciente (`ORDER BY date DESC`).
- **Snapshot en fecha X** = último registro por diente donde `consultation_date <= X`.


#### Prestaciones (PrestationInstance)

- Debe tener:
    - tipo (`PrestationType`)
    - Si tiene flag isUnique → debe ser la única prestación en la ConsultationInstance; no puede combinarse con ninguna otra
    - precio congelado al momento de creación
- Puede:
    - Tener ubicación (odontograma)
    - No tener ubicación → usar `scope`

##### Modelo de persistencia (delta)

- Una `PrestationInstance` se crea **una sola vez**, en la visita donde nació. No se copia entre visitas.
- Las prestaciones `IN_PROGRESS` de visitas anteriores son visibles en nuevas consultas mediante query por paciente.
- `PrestationStepInstance` registra en qué visita fue ejecutado cada step mediante FK a `ConsultationInstance`.
- **Snapshot de prestaciones en fecha X**:
    - Prestaciones: todas las del paciente con `createdAt <= X`
    - Steps: todos los de esas prestaciones con `consultationInstance.date <= X`

##### Regla: unicidad por ubicación en prestaciones IN_PROGRESS

- No se puede crear una nueva `PrestationInstance` del mismo `PrestationType` en la misma ubicación (mismo diente + cara, o mismo scope) si ya existe una `PrestationInstance` `IN_PROGRESS` para ese paciente en esa ubicación → error


---

#### Ubicación de prestación

**Caso 1 — Con tratamiento (odontograma)**

- La prestación referencia un `OdontogramDetail` existente en el mismo request.
- La ubicación (diente + cara) queda implícita en ese registro de odontograma.
- No se envían campos de scope.

**Caso 2 — Sin tratamiento (scope explícito)**

- Se debe definir:
    - `scope` (TOOTH, QUADRANT, FULL_MOUTH, etc.)
    - Campos asociados según el scope:
        - tooth
        - toothFace
        - quadrant
        - maxillary
- Se debe validar que el scope tenga relación con el campo enviado.
    Ej: scope: TOOTH, se debe recibir solo un dato tooth.



---

#### Estados de prestación

- IN_PROGRESS
- COMPLETED
- CANCELLED

---

#### Descuentos y promociones

- Solo puede existir uno:
    - ✔ Promoción
    - ✔ Descuento manual
    - ❌ Ambos al mismo tiempo

- Promoción:
    - Siempre calculada → `promotionAmount`

- Descuento manual:
    - Puede ser:
        - PERCENTAGE
        - FIXED
    - Se calcula → `discountAmount`

---

#### Precio final
finalAmount = price - promotionAmount - discountAmount


- Nunca puede ser negativo
- Debe persistirse

---

#### PrestationType

- `isUnique = true` → la prestación debe venir sola en la ConsultationInstance; no puede combinarse con ninguna otra prestación
- `hasSteps = true` → requiere workflow de pasos
- `requiresLocation = true` → debe tener odontograma o scope

---

#### Workflow de pasos

- Ordenados (`order`)
- Puede haber pasos obligatorios (`required`)
- Cada instancia tiene su propio estado (`PrestationStepStatus`)

##### Estados de PrestationStepInstance

| Estado | Descripción |
|---|---|
| `IN_PROGRESS` | El step fue iniciado pero aún no finalizado. Puede permanecer abierto entre visitas. |
| `COMPLETED` | El step fue completado. |
| `OMITTED` | El frontend lo envía explícitamente cuando el odontólogo decide no realizar un step opcional. El backend valida que el step no sea `required`. |
| `CANCELLED` | Estado de corrección. No forma parte del flujo de creación de `ConsultationInstance`. Solo disponible en el flujo de corrección de instancias (use case dedicado, pendiente de implementación). |

##### Reglas de transición de steps en una PrestationInstance

- En `stepAdvancements[]`, únicamente se aceptan los estados `IN_PROGRESS`, `COMPLETED` y `OMITTED`
- El estado `CANCELLED` no es válido en `stepAdvancements[]`
- Una `PrestationInstance` con un estado diferente a `IN_PROGRESS` no puede recibir `stepAdvancements[]`
- Si el step enviado en `stepAdvancements[]` no corresponde a ningún `PrestationStep` del `PrestationType` de la `PrestationInstance` referenciada → error
- Si un `PrestationStepInstance` ya existe en la DB con estado `IN_PROGRESS` y llega nuevamente en `stepAdvancements[]`, el único estado válido es `COMPLETED` — no puede volver a enviarse como `IN_PROGRESS` ni como `OMITTED` → error si se envía otro estado
- Un `PrestationStepInstance` puede crearse directamente como `COMPLETED` sin haber pasado por `IN_PROGRESS` — aplica cuando el step nace y se finaliza en la misma visita
- Si existe un `PrestationStepInstance` en `IN_PROGRESS` para una `PrestationInstance`, y el request incluye un step con `order` mayor para esa misma prestación, el mismo request debe incluir la resolución del `IN_PROGRESS` anterior a `COMPLETED`; de lo contrario → error
- Pueden enviarse múltiples entries para la misma `prestationInstanceId` en `stepAdvancements[]` — el sistema los procesa ordenados por `PrestationStep.order`, no por posición en el array
- Un step con estado `COMPLETED` no puede volver a registrarse → error
- Tras procesar cada step, el sistema evalúa automáticamente si la `PrestationInstance` puede cerrarse (ver *Finalización de PrestationInstance*)

##### Finalización de PrestationInstance

Una `PrestationInstance` pasa automáticamente a `COMPLETED` cuando se cumplen todas las siguientes condiciones:

- Ningún `PrestationStepInstance` en estado `IN_PROGRESS`
- Todos los `PrestationStep` marcados como `required` tienen un `PrestationStepInstance` con estado `COMPLETED`
- Todos los `PrestationStep` no `required` tienen un `PrestationStepInstance` con estado `COMPLETED` u `OMITTED`

La evaluación es automática tras cada procesamiento de step — no requiere una acción explícita adicional.

**Caso: último step opcional no ejecutado**
Si el odontólogo no desea realizar el último step y este no es `required`, el frontend lo envía explícitamente como `OMITTED`. El sistema crea el `PrestationStepInstance` con ese estado, evalúa las condiciones y cierra la `PrestationInstance` automáticamente.
- `OMITTED` es un estado válido en `stepAdvancements[]` — el frontend lo envía explícitamente cuando el odontólogo decide no realizar un step; el backend crea el `PrestationStepInstance` con estado `OMITTED` para mantener trazabilidad
- Un step enviado como `OMITTED` que sea `required` → error
- Antes de registrar un step con `order` N, todos los steps con `order` menor deben estar en estado final (`COMPLETED` u `OMITTED`), ya sea en la DB o en el mismo request; si algún step previo no existe en ninguno de los dos → error
- Si un step no llega en el request, no se registra — el backend no crea ni infiere nada por su ausencia

---

#### Pagos

- Un pago pertenece a una ConsultationInstance
- Puede dividirse en múltiples prestaciones
- `PaymentDetail` permite pagos parciales o totales

---


#### Cuentas Bancarias 

- No pueden repetirse en la base de datos Alías o CBU(accountIdentifier), ya que Ids de negocios.
- Son marcados como unique en la base. 

### Flujo

1. **Creación de Consultation**
    - Se asigna paciente, odontólogo y turno (`Appointment`)

2. **Apertura de atención**
    - Se crea la `ConsultationInstance` (1:1 con `Consultation`)
    - El frontend pre-carga el estado actual de la boca del paciente (odontograma reconstruido por query delta) y la grilla de prestaciones `IN_PROGRESS` de visitas anteriores

3. **El odontólogo trabaja durante la consulta**
    - Registra cambios en dientes (nuevas condiciones o nuevos dientes)
    - Crea prestaciones nuevas con o sin tratamiento
    - Avanza steps de prestaciones existentes

4. **Finalización de la consulta — el frontend envía un único request**

    El request contiene tres listas:

    - `odontogram[]` — solo los dientes con observaciones **nuevas o condición modificada** en esta visita
    - `prestationNew[]` — prestaciones que **nacen en esta visita**, con tipo, ubicación (odontograma o scope) y opcionalmente el primer step
    - `stepAdvancements[]` — avances de steps de prestaciones **ya existentes** de visitas anteriores, identificadas por `prestationInstanceId`

5. **Cómo persiste la API (todo en una transacción)**
    - Valida que no exista ya una `ConsultationInstance` para esa `Consultation` (1:1)
    - Guarda `ConsultationInstance`
    - Inserta los registros de `Odontogram` del request (delta)
    - Para cada prestación nueva:
        - Resuelve la referencia al `Odontogram` desde la lista del mismo request
        - Crea la `PrestationInstance` con precio congelado
        - Si viene step inicial, crea el `PrestationStepInstance` vinculado a la `ConsultationInstance` actual
    - Para cada avance de step:
        - Valida que la prestación pertenece al mismo paciente y está `IN_PROGRESS`
        - Crea el `PrestationStepInstance` vinculado a la `ConsultationInstance` actual

6. **Finalización**
    - Se marca la `Consultation` como completada

7. **Facturación**
    - Se generan pagos (`Payment`) vinculados a la `ConsultationInstance`
    - Se distribuyen en `PaymentDetail` por prestación (parcial o total)

---

## Riesgos / Deuda técnica

- Todos los enums ubicados en `consultation/catalogs/enums/` están hardcodeados también en el frontend → No existe un endpoint REST que exponga estos valores. Se debe crear endpoint para que el frontend consuma dinámicamente estos catálogos (ConsultationStatusType, PrestationStatusType, DiscountType, etc.)
- Complejidad alta del modelo → requiere buena documentación y testing
- Validaciones críticas deben centralizarse en servicios de dominio
- Posible sobrecarga en queries (uso intensivo de relaciones LAZY)
- Riesgo de inconsistencias si no se validan:
    - promociones vs descuentos
    - ubicación de prestaciones
    - estados de workflow
- Cancelación y corrección de `PrestationStepInstance` y `PrestationInstance` no forman parte del flujo de creación de `ConsultationInstance` — cada tipo de corrección tendrá su propio flujo dedicado

---