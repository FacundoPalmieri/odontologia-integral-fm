# ADR-0001 — Adoptar composición sobre herencia para Person

- **Fecha**: 2025-12-10
- **Estado**: aceptado
- **Scope**: feature:domain
- **Supersedes**: —
- **Related**: —

## Contexto
El sistema necesita representar múltiples actores (Dentist, Patient, User) que comparten atributos comunes de identidad (nombre, DNI, email, teléfono). Se evaluó usar herencia JPA con Person como clase abstracta, pero generó problemas concretos: tablas fragmentadas, campos de Person no visibles en Dentist/Patient, imposibilidad de instanciar Person para roles sin entidad propia (ej: secretaria), y acoplamiento rígido ante nuevos actores.

## Decisión
Definir Person como entidad concreta e independiente. Dentist, Patient y User referencian a Person mediante @OneToOne. Cada actor tiene su propia identidad (Person) y sus atributos específicos de rol.

## Alternativas consideradas
- **Herencia JPA (JOINED)**: descartada porque genera queries complejos, tablas fragmentadas, dificulta auditoría y no permite instanciar Person para roles sin entidad dedicada.
- **Herencia JPA (SINGLE_TABLE)**: descartada porque requiere columna discriminadora y deja muchos campos null según el rol — poco mantenible.
- **Sin Person (duplicar datos en cada actor)**: descartada por alto riesgo de inconsistencia en nombre, DNI y email entre entidades.

## Consecuencias

**Positivas**:
- ✓ Tablas claras: person contiene identidad, dentist/patient/user solo datos de rol.
- ✓ Auditoría y debugging más simples.
- ✓ Nuevos actores se agregan sin tocar la jerarquía existente.

**Negativas / tradeoffs**:
- ✗ Requiere JOIN adicional en queries que necesiten datos de identidad + rol.
- ✗ La creación de Dentist/Patient requiere crear Person primero — lógica de servicio adicional.
