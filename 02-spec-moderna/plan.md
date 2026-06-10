# Implementation Plan: SIFAP Core Modernization (4 Bounded Contexts)

**Branch**: `001-sifap-core` (pinned dir: `02-spec-moderna`) | **Date**: 2026-06-10 | **Spec**: [SPECIFICATION.md](SPECIFICATION.md)

**Input**: Feature specification from `02-spec-moderna/SPECIFICATION.md` (37 EARS requirements across 4 bounded contexts)

**Note**: This plan was produced by `/speckit.plan`. It consumes the Stage-2 architect artifacts already in this folder: [bounded-contexts.md](bounded-contexts.md), [modular-monolith-design.md](modular-monolith-design.md), [openapi.yaml](openapi.yaml), and [ADRs/](ADRs/). The canonical spec is `SPECIFICATION.md` (this repo uses it instead of `spec.md`).

## Summary

Modernize the SIFAP social-benefit payment system (legacy Natural/Adabas, ~29 years) into a single deployable **Modular Monolith** with four bounded contexts — **Beneficiaries**, **Programs**, **Payments & Folha**, **Audit** — backed by PostgreSQL 16 and exposed through a versioned REST API consumed by a Next.js 15 frontend.

Technical approach (from design + ADRs): package-by-feature Java 21 / Spring Boot 3.3 / Spring Modulith; one module per context plus a minimal `shared` kernel; cross-context communication is **in-process** (SPI interfaces for synchronous reads, domain events for the one-way audit trail). Payments is the single owner of `PAGAMENTO` and of a redesigned, unambiguous status machine ([ADR-002](ADRs/adr-002-maquina-de-status-unica-do-pagamento.md)). Adabas MU/PE structures map to relational JPA ([ADR-001](ADRs/adr-001-map-adabas-mu-fields-to-jsonb-vs-elementcollection.md), [ADR-003](ADRs/adr-003-mapeamento-grupos-periodicos-pe-adabas-para-jpa.md)). Audit is decoupled via domain events with exclusions always visible ([ADR-004](ADRs/adr-004-auditoria-desacoplada-por-domain-events.md)). Duplicated legacy calculation logic (CALCBENF × BATCHPGT) is unified into one tested rule.

## Technical Context

**Language/Version**: Java 21 (backend); TypeScript 5 `strict` (frontend)

**Primary Dependencies**: Spring Boot 3.3, Spring Modulith (in-process events + module boundaries), Spring Data JPA / Hibernate 6, Spring Security (OAuth2/JWT), Bean Validation; Next.js 15 (App Router), Tailwind CSS, shadcn/ui

**Storage**: PostgreSQL 16 — one logical database, schema-per-context (`beneficiaries`, `programs`, `payments`, `audit`); `pagamento_desconto` partitioned by competência (ADR-003)

**Testing**: JUnit 5 + Testcontainers (backend, incl. equivalence/characterization tests for financial calculations); Vitest + Testing Library (frontend); ArchUnit for module-boundary enforcement

**Target Platform**: Linux container (Docker); deployed to Azure (Terraform, Azure provider ~> 3.x); local parity via Docker Compose

**Project Type**: Web application (Java backend Modular Monolith + Next.js frontend)

**Performance Goals**: Not quantified in the spec — see Open Question. Known volume drivers: `PAGAMENTO` ~180M rows (+~3.8M/month), `BENEFICIARIO` ~4.2M rows. Monthly folha batch must process ~4.2M active beneficiaries resiliently (REQ-031). **NEEDS CLARIFICATION**: explicit p95 latency / batch-window SLOs are not stated in `SPECIFICATION.md`.

**Constraints**: LGPD — CPF and benefit values masked in logs and consistently masked in UI/reports (REQ-015); audit trail immutable, 10-year retention, exclusions always visible (IN-TCU 63, REQ-037); financial values truncated (not rounded) to 2 decimals (REQ-021); every requirement traceable to legacy (`source_legacy:`).

**Scale/Scope**: 37 requirements, 4 bounded contexts, 4 legacy DDMs (FNR 150–153), 15 analyzed legacy programs. 12 Open Questions (OQ-01…OQ-11, OQ-S1) gate parts of the financial/security spec.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.* Evaluated against [constitution.md](../.specify/memory/constitution.md) v1.0.0.

| Principle | Gate | Status |
| --------- | ---- | ------ |
| I. Legacy Traceability (NON-NEGOTIABLE) | Every REQ carries `source_legacy:`; no app code before a REQ exists; backdoors not silently reimplemented | ✅ PASS — all 37 REQs carry `source_legacy:`; backdoors isolated in Open Questions (OQ-S1), not requirements |
| II. Spec-Driven Development with EARS | EARS + REQ-IDs; mysteries in Open Questions; ADRs for ≥2-option choices | ✅ PASS — 37 EARS REQs; 12 Open Questions; 4 ADRs (MADR) |
| III. Test-First Discipline (NON-NEGOTIABLE) | Tests trace to REQ-IDs; equivalence tests for migrated calculations before unification | ✅ PASS (by design) — research/plan mandate Testcontainers + characterization tests for CALCBENF/CALCDSCT/BATCHPGT |
| IV. Modular Monolith Boundaries | package-by-feature; only `api/`+`spi/` public; in-process comms; no JPA entities across boundaries | ✅ PASS — design enforces this; ArchUnit + Spring Modulith verify it |
| V. Security & Data Protection by Default | OWASP; boundary validation; CPF/value masking; no hardcoded secrets; JPA/JPQL only; explicit CORS; OAuth2/JWT | ✅ PASS (by design) — `@Valid` at controllers, unified CPF mask (REQ-015), Key Vault/managed identity |

**Gate result:** PASS. No principle violations. One **NEEDS CLARIFICATION** (performance SLOs) is non-blocking for design and recorded in research.md. No entries in Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
02-spec-moderna/                 # pinned feature directory (SPECS_DIR)
├── SPECIFICATION.md             # canonical EARS spec (37 REQs)
├── bounded-contexts.md          # context map (Stage 2)
├── modular-monolith-design.md   # design + C4 component diagram
├── openapi.yaml                 # API skeleton (contract source)
├── ADRs/                        # ADR-001..004
├── plan.md                      # This file (/speckit.plan output)
├── research.md                  # Phase 0 output (/speckit.plan)
├── data-model.md                # Phase 1 output (/speckit.plan)
├── quickstart.md                # Phase 1 output (/speckit.plan)
├── contracts/                   # Phase 1 output (/speckit.plan) — per-context contract notes + openapi link
└── tasks.md                     # Phase 2 output (/speckit.tasks — NOT created here)
```

### Source Code (repository root)

Created in Stage 3 by the `@builder-agent`. Web application: Java Modular Monolith backend + Next.js frontend.

```text
backend/
├── src/main/java/com/datacorp/app/
│   ├── beneficiaries/        # BC1 — owns BENEFICIARIO (FNR 150)
│   │   ├── api/              # REST controllers (public)
│   │   ├── domain/           # Beneficiario, Dependente, Cpf, StatusBeneficiario
│   │   ├── service/          # CadastroService, ElegibilidadeService
│   │   ├── repository/
│   │   └── spi/              # BeneficiarioQuery, ElegibilidadeApi + DTOs (public)
│   ├── programs/             # BC2 — owns PROGRAMA-SOCIAL (FNR 151)
│   │   ├── api/ domain/ service/ repository/
│   │   └── spi/              # ProgramaCatalogQuery + ProgramaPolicy
│   ├── payments/             # BC3 — owns PAGAMENTO (FNR 152)
│   │   ├── api/ domain/ service/ repository/
│   │   └── spi/              # PagamentoHistoryQuery + PagamentoResumo
│   ├── audit/                # BC4 — owns AUDITORIA (FNR 153), event listeners
│   │   ├── api/ domain/ service/ repository/
│   │   └── spi/              # AuditoriaQuery
│   └── shared/               # kernel: Cpf, Competencia, Money, CodPrograma; events; exceptions
├── src/main/resources/db/migration/   # Flyway: V1__*.sql per context schema
└── src/test/java/com/datacorp/app/
    ├── contract/             # OpenAPI contract tests per context
    ├── integration/          # Testcontainers (PostgreSQL) per module
    ├── equivalence/          # characterization tests vs legacy calculation behavior
    └── unit/                 # per-REQ business-logic unit tests

frontend/
├── app/                      # Next.js 15 App Router
├── components/               # shadcn/ui
└── tests/                    # Vitest + Testing Library
```

**Structure Decision**: Web application (backend + frontend). Backend is a **single deployable Modular Monolith**, package-by-feature with one top-level package per bounded context (`beneficiaries`, `programs`, `payments`, `audit`) plus `shared`. Only `api/` and `spi/` are public; boundaries enforced by Spring Modulith + ArchUnit (Principle IV). This 1:1 mirrors [bounded-contexts.md](bounded-contexts.md) and [modular-monolith-design.md](modular-monolith-design.md).

## Complexity Tracking

> No constitution violations — section intentionally empty.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| _(none)_ | — | — |
