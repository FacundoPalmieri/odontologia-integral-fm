## ADR: Gestión de Feriados en la API de Consultorio Odontológico SaaS

### Fecha: 2025-12-08

### Contexto: Feature Appointment

## 1. Contexto y Problema


Los turnos de los dentistas no pueden asignarse en días no laborables (feriados).

Se requiere una forma de:
    
-   Registrar feriados manualmente.
    
-   Consultar feriados por año para validaciones y calendario.
    
-   Sincronizar automáticamente los feriados nacionales de Argentina mediante una API externa.
    
Requisitos clave:
    
-   Solo usuarios con permisos específicos pueden crear, actualizar o consultar feriados.
    
-   No se pueden crear feriados en fechas pasadas.
    
-   No debe existir duplicidad de feriados para la misma fecha.


## 2. Decisiones tomadas


### 2.1 Persistencia de feriados
    
-   Se creó la entidad Holiday con campos: id, date, type, name, year y metadatos de auditoría.
    
-   La columna date tiene restricción de unicidad para prevenir duplicados.
    
-   Se filtra por enabled = true para soportar borrado lógico.
    
Trade-off:
    
-   La elección de persistir el año (year) permite consultas más rápidas por año sin tener que calcularlo en tiempo de ejecución.




### 2.2 API REST para gestión de feriados

Endpoints:
    
-   GET /api/holiday/all?year= → Obtener todos los feriados de un año.
    
-   POST /api/holiday → Crear feriado (permiso: Configuración Creación).
    
-   PATCH /api/holiday → Actualizar feriado (permiso: Configuración Actualización).
    
Se usan DTOs (HolidayResponseDTO, HolidayCreateRequestDTO, HolidayUpdateRequestDTO) para desacoplar entidad de la API.
    
Validaciones:
    
-   Fecha no anterior al día actual.
    
-   No duplicar feriados.
    
Trade-off:
    
-   Se prioriza claridad y control de errores sobre performance en la carga manual (válido para el tamaño esperado de feriados).



### 2.3 Integración con API externa (ArgentinaDatosClient)

Objetivo: sincronizar automáticamente feriados nacionales.
    
Implementado con WebClient apuntando a https://api.argentinadatos.com/v1/feriados/{year}.
    
Método loadHolidays(year):
    
-   Es una tarea programada.

-   Verifica si ya existen feriados cargados.
    
-   Consulta la API, mapea DTOs a entidad Holiday y persiste todos.
    
-   Loguea duración y cantidad de feriados cargados.
    
Trade-offs:
    
-   Bloquea el hilo con .block() por simplicidad y control secuencial, adecuado para tareas programadas.
    
-   Alternativa de usar programación reactiva completa descartada por complejidad innecesaria en este proyecto freelance.



### 2.4 Seguridad y auditoría
    
Métodos protegidos con anotaciones personalizadas (@OnlyAccessConfigurationReadOrAppointmentsManagement, @OnlyAccessConfigurationCreate, etc.).
    
Logs con @LogAction para auditoría de creación, actualización y carga programada.
    
Justificación:
    
-   Permite cumplir con requisitos de negocio y regulaciones de auditoría.


## 3. Alternativas consideradas


La primera alternativa considerada fue guardar los feriados únicamente en memoria.
-   Pros: simple y rápido.
-   Contras: los datos se pierden al reiniciar la aplicación y no pueden utilizarse para cálculos de calendario o validaciones de turnos. Esta opción fue descartada.


La segunda alternativa fue consultar la API externa directamente en cada solicitud de disponibilidad.
-   Pros: los datos siempre están actualizados.
-   Contras: genera sobrecarga de red, mayor latencia y dependencia excesiva del servicio externo en tiempo real. Esta opción también fue descartada.


## 4. Consecuencias


-   Garantiza que la feature Appointment nunca programe turnos en feriados, si no existe relación con dentistas.
    
-   Permite gestionar feriados manualmente y de manera automatizada.
    
-   Mantiene trazabilidad y control de cambios.