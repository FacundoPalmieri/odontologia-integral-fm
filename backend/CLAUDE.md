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

Todo el código de dominio vive en `src/main/java/.../feature/` organizado por módulo. Hay dos formas, según cuántos subdominios tenga el módulo:

```
feature/
├── <modulo-de-un-solo-dominio>/       # ej: dentist, patient, user
│   └── controller/ dto/ model/ repository/ service/
│
└── <modulo-con-subdominios>/          # ej: consultation, appointmentscheduling, person
    ├── <subdominio>/
    │   └── controller/ dto/ model/ repository/ service/
    └── <subdominio>/
```

Un módulo se divide en subdominios cuando agrupa conceptos con ciclo de vida propio. Ejemplo: `person` tiene `person`, `geo` (país, provincia, localidad) y `attachedfile` (archivos adjuntos); `consultation` tiene `prestation`, `treatment`, `consultationinstance`, etc.

Ejemplos de módulos: `authentication`, `user`, `person`, `patient`, `dentist`, `appointmentscheduling`, `consultation`, `payment`, `developer`.

Las preocupaciones transversales viven en `infrastructure/` (auditoría, email, excepciones, logging, scheduler, WebSocket, parámetros del sistema) y `shared/` (DTOs base, entidades base, excepciones personalizadas, enums).

### Capas dentro de cada feature

Cada módulo sigue: `controller → service → repository → model`, más una carpeta `dto/`.

Esas capas viven en el módulo (si es de un solo dominio) o dentro de cada subdominio (si el módulo los tiene).

La lógica de negocio no trivial se divide en dos patrones:

- **UseCase** (`VerbNounUseCase`, ej: `CreateConsultationUseCase`) — orquesta: coordina múltiples pasos, llama repos, valida reglas de negocio, tiene un único propósito de entrada (`execute()`). Un CU del req → un UseCase.
- **DomainService** — razona sobre el dominio: lógica que no pertenece a una entidad pero tampoco es orquestación (ej: calcular disponibilidad, validar solapamientos). Lo llaman los UseCases, no el controller.

### Dónde vive un cálculo o regla de negocio

IMPORTANTE: Ver **ADR-0023** (`docs/architecture/adr/_cross-cutting/0023-donde-vive-un-calculo-de-negocio.md`) para el detalle y ejemplos. 

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
    └── <subdominio>/             # igual que en main (sin este nivel si el módulo es de un solo dominio)
        ├── repository/           # @integration
        └── service/              # @unit (UseCases y DomainServices)
```

### Convención de nombres de tests
`methodName_condition_expectedResult` (camelCase). Sin `should`, sin sufijo `_test`. Cada test tiene Javadoc con `CASO:`, `Regla:` (cuando aplica) y `Validación:`.

### Mocks vs. entidades reales (`build()`) en tests de UseCase/DomainService

Regla para decidir si una entidad colaboradora se mockea o se construye real vía su `build()` (ADR-0031):

- **Si la entidad es un dato de entrada** que el método bajo test consume o interpreta (lee getters para decidir una rama, comparar estados, clasificar) → **mock**. Aísla el test de la lógica de construcción de esa entidad, que no es lo que se está probando. Ejemplo: `PrestationInstanceDomainService` razona sobre `Odontogram`/`PrestationInstance` que otro ya construyó y persistió — no es su responsabilidad validar que `build()` arme bien la entidad (esa invariante la garantiza el propio constructor privado, y si hiciera falta, un test de la entidad misma — fuera de scope en este proyecto). Mockear también evita que un cambio de firma en `build()` rompa tests que no dependen de esa lógica.
- **Si la entidad es un dato de salida** que el método bajo test construye (llama a `Entity.build(...)` y la pasa a `repository.save(...)`) → **construila real y capturala con `ArgumentCaptor`** para verificar su estado (`getId()`, relaciones). Un mock no tiene comportamiento real que conectar — mockear el resultado del propio método que se está probando lo vuelve tautológico. Ejemplo: `CreateConsultationInstanceUseCase` resuelve un `tempId` y arma una `PrestationInstance` que referencia el `Odontogram` recién persistido — verificar eso exige leer el estado real del objeto construido, no un stub.

### Tests de forma de DTOs (`@AssertTrue`, `@NotNull`, `@NotEmpty`, `@Valid`)

Todo test de un DTO pasa por un `Validator` real (`Validation.buildDefaultValidatorFactory().getValidator()`), sin Spring ni Mockito. Nunca se llama `isX()` directo: eso prueba que el método devuelve `false`, pero no que la anotación exista. Si el dev se olvida el `@AssertTrue`, el método sigue devolviendo `false` en el test y el endpoint acepta el payload inválido en producción.

- **Casos que esperan rechazo:** se arma un ítem base válido en todo lo demás y se rompe solo la regla bajo test. Se afirma con `containsExactly` sobre el `propertyPath` de las violaciones (el nombre del método sin `is` y con minúscula inicial: `isLocationConsistent()` → `locationConsistent`; para `@NotEmpty`/`@NotNull`, el nombre del campo). No alcanza con contar violaciones: otra regla del mismo DTO podría ser la que falla. No se afirma el texto ni la clave del mensaje: es una decisión de producción.
- **Un campo que rompe dos reglas a la vez** (por ejemplo, `discountType` sin `discountValue` rompe también `isDiscountConsistent`): se afirman las dos violaciones con `containsExactlyInAnyOrder` y el Javadoc explica por qué. No se reemplaza el input por uno que evite la segunda violación: se pierde cobertura de ese campo por separado.
- **Casos que esperan aceptación:** se afirma que ninguna violación tiene el `propertyPath` de esa regla. No detectan una anotación olvidada; esa protección la dan los casos de rechazo.
- **Cascade (`@Valid`):** el DTO raíz con listas anidadas lleva un caso con un ítem anidado inválido, que afirma el `propertyPath` completo (por ejemplo `prestation[0].steps`).
- **Límite:** esto no prueba que el controller responda 400. Eso depende del `@Valid` en el parámetro del controller y solo lo cubre un `@WebMvcTest`.

### Fuera de scope por definición

Los siguientes **nunca se testean** en este proyecto — no proponer tests para ellos ni incluirlos en auditorías de cobertura:

- **Query services** (`*QueryService`): lecturas puras, sin reglas de dominio.
- **Servicios CRUD de lookup** (por ejemplo `GenderService`, `CountryService`, `DentistSpecialtyService`): lecturas o altas simples de datos de selectores, sin reglas de dominio. Lo que decide si algo se testea es el tipo de clase: todo `*UseCase` y `*DomainService` se testea, aunque trate sobre datos de selectores (por ejemplo `CreatePromotionUseCase`).
- **Entidades/modelos JPA** (`*/model/*.java`): clases de datos con anotaciones — sin comportamiento a validar.
- **Mappers** (MapStruct `@Mapper`): código generado.
- **DTOs**: records / portadores de datos. Excepción: los DTOs con `@AssertTrue` de forma (ADR-0030) llevan lógica de validación y sí se testean, con la regla de la sección "Tests de forma de DTOs".
- **Repositorios** (`I*Repository`): solo métodos derivados de Spring Data — sin comportamiento a validar. Los métodos `@Query` personalizados con lógica de filtrado no trivial son candidatos a tests de integración.

Los tests apuntan a **use cases** (`*UseCase`) y **domain services** (`*DomainService`) donde viven las reglas de negocio. Al auditar cobertura de una feature, declarar los ítems anteriores como "fuera de scope por definición" en lugar de gaps.