# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

REST API backend for a dental clinic management system (Odontología Integral FM). Built with **Spring Boot 3.2.5**, **Java 17**, **Maven**, **MySQL 8**, and **Hibernate/JPA** with **Envers** for auditing.

## Commands

```bash
mvn spring-boot:run          # Start dev server (localhost:8080)
mvn test                     # Run all tests
mvn test -Dtest=ClassName    # Run a single test class
mvn clean install            # Full build
mvn clean install -DskipTests # Build without tests
```

Swagger UI available at `http://localhost:8080/swagger-ui.html` when running.

## Architecture

### Feature-driven layered structure

All domain code lives under `src/main/java/.../feature/` organized by domain module:

```
feature/
├── authentication/     # JWT login, refresh tokens, password reset, OAuth2
├── user/               # User accounts (UserSec entity)
├── person/             # Shared person entity + catalogs (gender, nationality, DNI, locality)
├── patient/            # Patient profiles + catalogs (health plans, medical risks)
├── dentist/            # Dentist profiles + catalogs (specialties)
├── appointmentscheduling/  # Appointments, calendar, dentist availability, holidays
├── consultation/       # Consultation lifecycle, odontogram, treatments (prestations)
├── payment/            # Payment accounts, transactions + catalogs
└── developer/          # Admin-only endpoints for system config
```

Cross-cutting concerns live in `infrastructure/` (auditing, email, exceptions, logging, scheduler, WebSocket, system parameters) and `shared/` (base DTOs, base entities, custom exceptions, enums).

### Layers within each feature

Each module follows: `controller → service → repository → model`, plus a `dto/` folder.

Services that encapsulate complex business logic are split into **Use Cases**: `VerbNounUseCase` (e.g., `CreateConsultationUseCase`, `CallPatientUseCase`). This is the preferred pattern for anything non-trivial — create a dedicated use case class rather than bloating the service.

### Response wrapper

All API responses use `Response<T>` (a record in `shared/dto/`). Use it consistently.

### Auditing

- All significant entities extend `Auditable` and are annotated with `@Audited` (Hibernate Envers). This auto-generates `*_AUD` tables tracking INSERT/UPDATE/DELETE with revision numbers.
- `created_by` / `updated_by` / `disabled_by` are populated automatically via `JpaConfig`.
- The `@LogAction` AOP annotation writes structured logs to the database. Use it on service methods for key actions.

### Soft deletes

Entities are never hard-deleted. They have an `enabled` flag + `disabled_at` timestamp. Queries must filter by `enabled = true` unless explicitly fetching disabled records.

### Security

Three-level RBAC: **Role → Permission → Action**. Method-level authorization uses custom annotations in `securityconfig/annotations/`. JWT is stateless with a separate refresh token flow. BCrypt for passwords.

### Naming conventions

| Thing | Pattern |
|---|---|
| Service interface | `IEntityService` |
| Service impl | `EntityService` |
| Repository | `IEntityRepository extends JpaRepository` |
| DTOs | `EntityCreateDTO`, `EntityUpdateDTO`, `EntityResponseDTO` |
| Use cases | `VerbNounUseCase` |
| Mappers | MapStruct `@Mapper` |

### Custom exceptions

Use the domain exceptions in `shared/exception/`: `NotFoundException`, `BadRequestException`, `ConflictException`, `ForbiddenException`, `UnauthorizedException`, `DataBaseException`. The global handler in `infrastructure/exception/` translates these to HTTP responses with user-friendly Spanish messages from `messages.properties`.

### QueryService vs direct repository access

When fetching an entity by ID: if absence is a valid result, call the repository directly. If absence is a business error that must throw, delegate to the domain's `EntityQueryService` — it encapsulates the `NotFoundException`. Do not call the repository directly from a use case when absence should throw.

## Key domain concepts

- **Odontogram**: tooth-level diagram attached to a consultation, tracking treatment state per tooth/face.
- **Prestation**: a treatment procedure, potentially multi-step with prerequisites.
- **Consultation lifecycle**: state machine — draft → pending → in-progress → finished. Transitions are validated in use cases.
- **Dentist availability**: working hours per weekday with breaks. Calendar locks (punctual/daily/recurring) block slots. Dentist holidays can override defaults.

## Environment

Requires a `.env` file (not committed) with: `BD_URL`, `BD_USER`, `BD_PASSWORD`, `PRIVATE_KEY`, `USER_GENERATOR`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `EMAIL_USERNAME`, `EMAIL_PASSWORD`, `ALLOWED_ORIGINS`.

Database schema is auto-managed by `hibernate.ddl-auto=update`. Timezone is `America/Argentina/Buenos_Aires`.

## Tests

Tests are in `src/test/.../feature/consultation/` and use **JUnit 5 + Mockito**. Focus is on use case business rules (date validation, duplicate checks, status transitions). Follow the same use case test pattern when adding new tests.

### Test naming convention
`methodName_condition_expectedResult` (camelCase). No `should`, no `_test` suffix. Each test has a Javadoc with `CASO:`, `Regla:` (when applicable) and `Validación:`.

### Out of scope by definition

The following are **never tested** in this project — do not propose tests for them and exclude them from any coverage audit:

- **Query services** (`*QueryService`): pure reads, no domain rules.
- **Catalogs** (`feature/*/catalogs/**`): static lookup data and CRUD without business logic.
- **JPA entities / models** (`*/model/*.java`): data classes with annotations — no behavior to validate.
- **Mappers** (MapStruct `@Mapper`): generated code.
- **DTOs**: records / data carriers.
- **Repositories** (`I*Repository`): Spring Data interfaces.

Tests target **use cases** (`*UseCase`) and **domain services** (`*DomainService`) where the business rules live. When auditing test coverage for a feature, declare the items above as "out of scope by definition" rather than gaps.



