# ADR-0011 — Organizar entidades de pago por dependencia de dominio

- **Fecha**: 2026-06-12
- **Estado**: aceptado
- **Scope**: feature:consultation
- **Supersedes**: 
- **Related**: ADR-0010

## Contexto
La feature `consultation` tiene entidades de pago (`Payment`, `PaymentDetail`) y el módulo `payment` tiene `PaymentAccount`. Al crecer el dominio surgió la duda de si todo lo financiero debía vivir junto en `feature/payment` o distribuirse según dependencias.

## Decisión
Las entidades financieras que dependen de `PrestationInstance` (`Payment`, `PaymentDetail`) viven en `consultation/core/payment`. Las entidades financieras independientes del ciclo clínico (`PaymentAccount`, `PaymentProvider`) viven en `feature/payment`. El criterio es dependencia de dominio, no tema financiero.

## Alternativas consideradas
- **Todo en `feature/payment`**: centralizar todas las entidades financieras en un único módulo — descartada porque `Payment`/`PaymentDetail` referencian `PrestationInstance`, que pertenece al dominio clínico; moverlas crea una dependencia inversa (`payment` → `consultation`).

## Consecuencias

**Positivas**:
- ✓ Las dependencias de dominio son explícitas: `Payment` vive donde vive `PrestationInstance`.
- ✓ `feature/payment` queda limpio de entidades con acoplamiento clínico.

**Negativas / tradeoffs**:
- ✗ Las entidades "de pago" están en dos módulos distintos — un dev nuevo puede no entender por qué `Payment` no está en `feature/payment`.
