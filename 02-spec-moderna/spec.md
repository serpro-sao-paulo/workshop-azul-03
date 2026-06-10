# Feature Spec (bridge): SIFAP Core Modernization

> **Canonical spec:** [SPECIFICATION.md](SPECIFICATION.md) — 37 EARS requirements (REQ-001…037) with `source_legacy:`, acceptance criteria, Open Questions and traceability matrix. This `spec.md` is a thin Spec-Kit bridge that maps those requirements to **prioritized user stories** (one per bounded context) so `/speckit.tasks` can organize work. Do not duplicate requirement detail here — read the canonical spec.

## Overview

Modernize the SIFAP social-benefit payment system into a single deployable Modular Monolith (4 bounded contexts) with a versioned REST API and Next.js frontend. Architecture: [bounded-contexts.md](bounded-contexts.md), [modular-monolith-design.md](modular-monolith-design.md), [ADRs](ADRs/). Plan: [plan.md](plan.md).

## User Stories (prioritized)

Priority reflects the migration order in the discovery report §5.1 and the SPI dependency graph (Payments depends on Beneficiaries + Programs; Audit consumes events from both).

### US1 — Cadastro & Elegibilidade de Beneficiários (Priority: P1)

As an internal operator, I register and maintain beneficiaries (and their dependents) with unified CPF/document validation, and determine who is eligible for a program, so that only valid, eligible citizens enter the payment flow.

- **Bounded context:** Beneficiaries (owns BENEFICIARIO / FNR 150)
- **Requirements:** REQ-001…REQ-015
- **Independent test:** Run quickstart S1–S4 — invalid CPF/identity rejected with accumulated errors; status rules (initial A, auto-suspend >75); ≤5 dependents; eligibility by status/age/income/program type. Testable without Payments or Audit.

### US2 — Catálogo de Programas Sociais (Priority: P1)

As a program administrator, I maintain the catalog of social programs (values, factors, Fator-K, eligibility rules, applicable discount types), so that calculation and eligibility have authoritative reference data.

- **Bounded context:** Programs (owns PROGRAMA-SOCIAL / FNR 151)
- **Requirements:** REQ-016, REQ-017
- **Independent test:** Run quickstart S3 — unique program code (409 on dup); persisted vlrBase adjusted by Fator-K. Foundational reference data consumed by US1 (eligibility) and US3 (calculation).

### US3 — Pagamentos & Folha (Priority: P2)

As the payments operator, I generate the monthly folha, calculate benefit/discounts/13º/correction with a single unified rule, govern the payment status machine, and reconcile bank returns, so that beneficiaries are paid correctly and auditably.

- **Bounded context:** Payments & Folha (owns PAGAMENTO / FNR 152)
- **Requirements:** REQ-018…REQ-035
- **Independent test:** Run quickstart S5–S7 — idempotent folha (status GERADO), unified calculation with equivalence parity, 30% cap with judicial exemption, net floor 0, CNAB reconciliation (P/D/E), divergence detection, idempotent correction. Depends on US1 + US2 SPIs.

### US4 — Auditoria & Conformidade (Priority: P3)

As an auditor, I consult an immutable audit trail (with exclusions always visible) fed by domain events, so that all benefit and payment changes are accountable per IN-TCU 63.

- **Bounded context:** Audit (owns AUDITORIA / FNR 153)
- **Requirements:** REQ-036, REQ-037
- **Independent test:** Run quickstart S8 — conciliation/divergence audit events present; exclusions ('EX') visible; trail immutable. Consumes events from US3 + US1.

## Cross-cutting / Foundational

- Shared kernel value objects: `Cpf` (Módulo 11 + unified mask, REQ-001/015), `Competencia`, `Money` (truncate 2 decimals, REQ-021), `CodPrograma`.
- Module-boundary enforcement (Spring Modulith + ArchUnit), security (OAuth2/JWT, `@Valid`, CORS), Flyway schema-per-context.

## Open Questions

See [SPECIFICATION.md Open Questions](SPECIFICATION.md#open-questions-não-são-requisitos-ainda) (OQ-01…OQ-11, OQ-S1). These gate parts of US3 (status de-para, Fator-K origin) and the eligibility/validation backdoors (OQ-S1) — to be resolved with stakeholders before the corresponding data migration / code is finalized.

## Success Criteria

- All 37 requirements implemented with tests tracing to their REQ-ID (Principle III).
- Each user story independently testable per its quickstart scenarios.
- Constitution Check passes (see [plan.md](plan.md)).
