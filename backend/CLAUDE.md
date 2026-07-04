# CLAUDE.md

Este archivo le da contexto a Claude Code (claude.ai/code) sobre este repositorio.

## Proyecto

API REST backend para un sistema de gestión de clínica odontológica (Odontología Integral FM). Construido con **Spring Boot 3.2.5**, **Java 17**, **Maven**, **MySQL 8** e **Hibernate/JPA** con **Envers** para auditoría.

## Comandos

```bash
mvn spring-boot:run          # Levantar servidor de desarrollo (localhost:8080)
mvn test                     # Correr todos los tests
mvn test -Dtest=ClassName    # Correr una clase de test específica
mvn clean install            # Build completo
mvn clean install -DskipTests # Build sin tests
```

Swagger UI disponible en `http://localhost:8080/swagger-ui.html` con el servidor corriendo.

## Arquitectura

### Estructura por features

Todo el código de dominio vive en `src/main/java/.../feature/` organizado por módulo:

```
feature/
├── <modulo>/
│   ├── catalogs/   # Datos de selectores y lookup sin lógica de negocio
│   └── core/       # Lógica de negocio del módulo
└── ...
```

Ejemplos de módulos: `authentication`, `user`, `person`, `patient`, `dentist`, `appointmentscheduling`, `consultation`, `payment`, `developer`.

Las preocupaciones transversales viven en `infrastructure/` (auditoría, email, excepciones, logging, scheduler, WebSocket, parámetros del sistema) y `shared/` (DTOs base, entidades base, excepciones personalizadas, enums).

### Capas dentro de cada feature

Cada módulo sigue: `controller → service → repository → model`, más una carpeta `dto/`.

Cuando el módulo crece, sus sub-dominios se agrupan bajo `catalogs/` (datos de selectores y lookup, sin lógica de negocio) y `core/` (lógica de negocio).

Los servicios que encapsulan lógica de negocio compleja se dividen en **Use Cases**: `VerbNounUseCase` (ej: `CreateConsultationUseCase`, `CallPatientUseCase`). Es el patrón preferido para cualquier cosa no trivial — crear una clase de use case dedicada en lugar de engordar el servicio.

### Wrapper de respuesta

Todas las respuestas de la API usan `Response<T>` (un record en `shared/dto/`). Usarlo de forma consistente.

### Auditoría

- Todas las entidades significativas extienden `Auditable` y están anotadas con `@Audited` (Hibernate Envers). Esto genera automáticamente tablas `*_AUD` que registran INSERT/UPDATE/DELETE con números de revisión.
- `created_by` / `updated_by` / `disabled_by` se poblan automáticamente mediante `JpaConfig`.
- La anotación AOP `@LogAction` escribe logs estructurados en la base de datos. Usarla en métodos de servicio para acciones clave.

### Soft deletes

Las entidades nunca se eliminan físicamente. Tienen un flag `enabled` + timestamp `disabled_at`. Las queries deben filtrar por `enabled = true` salvo que se estén buscando registros deshabilitados explícitamente.

### Seguridad

RBAC de tres niveles: **Rol → Permiso → Acción**. La autorización a nivel de método usa anotaciones personalizadas en `securityconfig/annotations/`. JWT sin estado con flujo separado de refresh token. BCrypt para contraseñas.

### Convenciones de nomenclatura

| Elemento | Patrón |
|---|---|
| Interfaz de servicio | `IEntityService` |
| Implementación de servicio | `EntityService` |
| Repositorio | `IEntityRepository extends JpaRepository` |
| DTOs | `EntityCreateDTO`, `EntityUpdateDTO`, `EntityResponseDTO` |
| Use cases | `VerbNounUseCase` |
| Mappers | MapStruct `@Mapper` |

### Excepciones personalizadas

Usar las excepciones de dominio en `shared/exception/`: `NotFoundException`, `BadRequestException`, `ConflictException`, `ForbiddenException`, `UnauthorizedException`, `DataBaseException`. El handler global en `infrastructure/exception/` las traduce a respuestas HTTP con mensajes en español desde `messages.properties`.

### QueryService vs acceso directo al repositorio

Al buscar una entidad por ID: si la ausencia es un resultado válido, llamar al repositorio directamente. Si la ausencia es un error de negocio que debe lanzar excepción, delegar al `EntityQueryService` del dominio — encapsula el `NotFoundException`. No llamar al repositorio directamente desde un use case cuando la ausencia debería lanzar excepción.

## Conceptos clave del dominio

- **Odontograma**: diagrama a nivel de pieza dental adjunto a una consulta, que registra el estado del tratamiento por diente/cara.
- **Prestación**: un procedimiento de tratamiento, potencialmente de múltiples pasos con prerequisitos.
- **Ciclo de vida de la consulta**: máquina de estados — borrador → pendiente → en curso → finalizada. Las transiciones se validan en use cases.
- **Disponibilidad del odontólogo**: horarios de trabajo por día de la semana con descansos. Los bloqueos del calendario (puntuales/diarios/recurrentes) bloquean slots. Los feriados del odontólogo pueden sobreescribir los valores por defecto.

## Entorno

Requiere un archivo `.env` (no committeado) con: `BD_URL`, `BD_USER`, `BD_PASSWORD`, `PRIVATE_KEY`, `USER_GENERATOR`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `EMAIL_USERNAME`, `EMAIL_PASSWORD`, `ALLOWED_ORIGINS`.

El esquema de la base de datos es gestionado automáticamente por `hibernate.ddl-auto=update`. Zona horaria: `America/Argentina/Buenos_Aires`.

## Tests

Los tests están en `src/test/.../feature/` y usan **JUnit 5 + Mockito**. El foco está en las reglas de negocio de los use cases (validación de fechas, chequeos de duplicados, transiciones de estado).

### Estrategia de tipos de test: @Tag + Surefire

Este proyecto usa `@Tag` de JUnit 5 para diferenciar tipos de test dentro de un único módulo Maven. El árbol de tests refleja `src/main/`, organizado por dominio — no por tipo de test.

- `@Tag("unit")` — Tests de UseCase y DomainService. Mockito puro, sin contexto Spring. Rápidos.
- `@Tag("integration")` — Tests de repositorio (`@DataJpaTest`) y smoke tests de contexto (`@SpringBootTest`). Requieren DB.

Ejecución selectiva:
- `mvn test -Dgroups=unit` — solo tests unitarios
- `mvn test -Dgroups=integration` — solo tests de integración
- `mvn test` — todos los tests

Si los tests de integración empiezan a tardar minutos (DB real, servicios externos), migrar al plugin Failsafe + naming `*IT.java` — es un rename mecánico, sin cambios estructurales.

### Estructura de tests

```
src/test/java/.../feature/
└── <modulo>/
    ├── catalogs/
    │   └── <subdominio>/
    │       └── repository/     # @integration
    └── core/
        └── <subdominio>/
            └── service/        # @unit (UseCases y DomainServices)
```

### Convención de nombres de tests
`methodName_condition_expectedResult` (camelCase). Sin `should`, sin sufijo `_test`. Cada test tiene Javadoc con `CASO:`, `Regla:` (cuando aplica) y `Validación:`.

### Fuera de scope por definición

Los siguientes **nunca se testean** en este proyecto — no proponer tests para ellos ni incluirlos en auditorías de cobertura:

- **Query services** (`*QueryService`): lecturas puras, sin reglas de dominio.
- **Catálogos** (`feature/*/catalogs/**`): datos de lookup estáticos y CRUD sin lógica de negocio.
- **Entidades/modelos JPA** (`*/model/*.java`): clases de datos con anotaciones — sin comportamiento a validar.
- **Mappers** (MapStruct `@Mapper`): código generado.
- **DTOs**: records / portadores de datos.
- **Repositorios** (`I*Repository`): solo métodos derivados de Spring Data — sin comportamiento a validar. Los métodos `@Query` personalizados con lógica de filtrado no trivial son candidatos a tests de integración.

Los tests apuntan a **use cases** (`*UseCase`) y **domain services** (`*DomainService`) donde viven las reglas de negocio. Al auditar cobertura de una feature, declarar los ítems anteriores como "fuera de scope por definición" en lugar de gaps.