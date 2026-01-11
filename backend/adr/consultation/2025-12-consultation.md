## ADR: Consultation

### Fecha: 13/12/2025
### Contexto: Feature Consultation

## Contexto
Una Consultation representa un acto médico-asistencial concreto, fechado, asociado a un turno y a un odontólogo, con consecuencias clínicas y administrativas.

El sistema gestiona consultas odontológicas con distintos estados a lo largo de su ciclo de vida (ej. WAITING_ROOM, IN_CONSULTATION, FINISHED).
Cada cambio relevante debe quedar registrado en una bitácora histórica para fines de auditoría, trazabilidad y soporte.

Adicionalmente, durante una consulta puede producirse:

-   Un error operativo (cambio de estado incorrecto).

-   Una corrección del odontograma cargado durante la atención.

El sistema ya cuenta con:

-   Persistencia de consultas.

-   Entidad de historial (ConsultationHistory).

-   Comunicación en tiempo real mediante WebSocket.

Se busca definir un diseño que:

-   Sea claro semánticamente.

-   Mantenga trazabilidad.

-   Permita reglas de negocio configurables.

-   Evite sobrecargar el modelo con conceptos técnicos incorrectos.

## 1. Problema

### 1.1 Corrección de estados de una consulta

Algunos estados de una consulta pueden necesitar corrección (por error humano u operativo).
Pero hay que tener en cuenta lo siguiente:

-   Una consulta finalizada no puede reabrirse.

-   Debe quedar registro explícito de las correcciones.

-   El historial debe diferenciar cambios normales de cambios por corrección.

### 1.2 Modificación del odontograma

El odontograma forma parte del registro clínico y:

-   No debe modificarse directamente (inmutabilidad).

-   Puede requerir corrección, pero de forma controlada.

-   El sistema debe permitir limitar la cantidad de correcciones(parámetro de sistema), sin bloquear futuras decisiones.

#### Se debe evitar:

-   Pérdida de información histórica.

-   Uso indebido de mecanismos técnicos (ej. versionado para concurrencia).

## 2. Decisión principal

### 2.1 Historial de consultas basado en eventos

Se mantiene una única tabla de historial (ConsultationHistory) que registra:

-   El estado de la consulta (ConsultationStatus).

-   Un evento asociado opcional (ConsultationEvent).

-   Un campo de observación/motivo.

El evento:

-   Es null en transiciones normales.

-   Se informa únicamente en situaciones excepcionales (correcciones, anulaciones).

Ejemplos de eventos:

CONSULTATION_CORRECTED

CONSULTATION_VOIDED

Esto permite:

-   Un modelo simple.

-   Lectura cronológica clara.

-   Auditoría completa sin múltiples tablas.

### 2.2 Odontograma inmutable con correcciones por reemplazo

El odontograma:

-   Nunca se modifica una vez persistido.

-   Cada corrección crea un nuevo odontograma completo.

El odontograma anterior queda:

-   Deshabilitado mediante baja lógica.

-   Invisible para el uso operativo.

-   Conservado para auditoría.

Solo el último odontograma activo se utiliza funcionalmente.

### 2.3 Control de correcciones por política de sistema

La cantidad de correcciones permitidas:

-   No se modela como atributo fijo en la entidad.

-   Se controla desde la capa de servicio.

-   Se compara contra un parámetro de sistema (ej. MAX_ODONTOGRAM_CORRECTIONS).

-   El modelo permite múltiples odontogramas, pero la política del producto define cuántos se aceptan.

### 2.4 Exclusión de @Version

No se utiliza @Version para controlar correcciones funcionales, ya que:

-   Está destinado exclusivamente a control de concurrencia.

-   No representa semántica de negocio.

-   Introduce confusión conceptual.

## 3. Alternativas consideradas

### 3.1 Uso de @Version para correcciones

Descartada.

-   Mezcla infraestructura con reglas de negocio.

-   Genera confusión para futuros mantenimientos.

-   No representa correctamente el dominio.

### 3.2 Modificación directa del odontograma

Descartada.

-   Pierde trazabilidad.

-   Riesgo legal y clínico.

-   Dificulta auditorías y debugging.

### 3.3 Tabla separada de eventos y estados

-   Descartada para esta versión.

-   Mayor complejidad conceptual.

-   Mayor costo de desarrollo.

-   Poco valor agregado para el tamaño actual del producto.

### 3.4 Campo contador persistente en odontograma

Postergada.

-   Puede agregarse en el futuro por performance o reporting.

-   Actualmente es información derivable.

-   No es necesaria para la regla de negocio inicial.

## 4. Consecuencias

### Positivas

-   Modelo claro y expresivo.

-   Auditoría completa y cronológica.

-   Separación correcta entre dominio y política de producto.

-   Fácil extensión futura (más correcciones, más eventos).

-   Compatible con WebSocket y notificaciones en tiempo real.

### Negativas / Trade-offs

-   Requiere lógica adicional en servicios.

-   El conteo de correcciones implica queries adicionales.

## Conclusión

El diseño prioriza:

-   Claridad semántica.

-   Inmutabilidad clínica.

-   Flexibilidad de negocio.

-   Simplicidad para un entorno SaaS en crecimiento.

El sistema queda preparado para escalar reglas sin reestructurar el modelo.