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

---

#### Prestaciones (PrestationInstance)

- Debe tener:
    - tipo (`PrestationType`)
    - Si tiene flag isUnique → no puede repetirse en la misma ConsultationInstance(odontogramDetail)
    - precio congelado al momento de creación
- Puede:
    - Tener ubicación (odontograma)
    - No tener ubicación → usar `scope`

---

#### Ubicación de prestación

Si **NO hay OdontogramDetail**:

- Se debe definir:
    - `scope` (TOOTH, QUADRANT, FULL_MOUTH, etc.)
    - Campos asociados según el scope:
        - tooth
        - toothFace
        - quadrant
        - maxillary

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

- `isUnique = true` → no puede repetirse en el mismo odontograma
- `hasSteps = true` → requiere workflow de pasos
- `requiresLocation = true` → debe tener odontograma o scope

---

#### Workflow de pasos

- Ordenados (`order`)
- Puede haber pasos obligatorios (`required`)
- Cada instancia tiene su propio estado

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
    - Se asigna paciente, odontólogo y turno

2. **Inicio de atención**
    - Se crea una o más ConsultationInstance

3. **Registro clínico**
    - Se completa odontograma (OdontogramDetail)

4. **Asignación de prestaciones**
    - Se crean PrestationInstance
    - Se define precio, descuentos o promociones

5. **Ejecución de prestaciones**
    - Cambio de estado
    - Ejecución de pasos (si aplica)

6. **Facturación**
    - Se generan pagos (Payment)
    - Se distribuyen en PaymentDetail

7. **Finalización**
    - Se marca la consulta como completada

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

---