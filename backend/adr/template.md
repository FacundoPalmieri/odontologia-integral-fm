# Feature: [Nombre de la feature]
### Fecha: 13/12/2025

---

## Objetivo
[Qué querés lograr con esta feature]

---

## Contexto
[Por qué es necesaria / situación actual]

El consultorio odontológico gestiona la atención de pacientes de manera manual o no estructurada, sin un sistema centralizado que represente el flujo completo de una consulta.

La información clínica, el estado del paciente y el seguimiento de la atención no se encuentran unificados en una única entidad o proceso.



---



## Problema
[Qué problema concreto estás resolviendo, qué falla o qué necesidad concreta existe]

Esta forma de trabajo genera:

- Falta de trazabilidad sobre las consultas realizadas
- Dificultad para reconstruir el historial de atenciones de un paciente
- Riesgo de inconsistencias o pérdida de información clínica
- Ausencia de un flujo claro desde la recepción hasta el cierre de la consulta

---

##  Opciones evaluadas

### Opción 1: [Nombre]
- ✔ Pros:
- ❌ Contras:

### Opción 2: [Nombre]
- ✔ Pros:
- ❌ Contras:


---



## Decisión
[Qué opción elegís y por qué. Detalles técnicos necesarios.]





---

## Diseño





### Modelo conceptual

- Consultation: representa una atención completa
- ConsultationStatusHistory: historial de estados
- Patient: paciente





### Relaciones

- Una Consultation pertenece a un Patient
- Una Consultation tiene múltiples cambios de estado





### Reglas de negocio
[Validaciones en el servicio]

- Una consulta finalizada no puede reabrirse
- Toda modificación de estado debe registrarse
- Las correcciones deben diferenciarse de cambios normales






### Flujo

1. Recepción
2. Espera
3. Atención
4. Finalización





---

## Riesgos / Deuda técnica
[Qué riesgos o deuda técnica estás asumiendo con esta decisión]


---

