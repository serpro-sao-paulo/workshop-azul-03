<!--
SYNC IMPACT REPORT — Constitution amendment
============================================
Version change: TEMPLATE (unversioned) → 1.0.0
Bump rationale: Initial ratification — placeholder template replaced with concrete,
project-specific governance for the SIFAP legacy-modernization workshop. MAJOR (first adoption).

Modified principles (placeholder → concrete):
  [PRINCIPLE_1_NAME] → I. Legacy Traceability (NON-NEGOTIABLE)
  [PRINCIPLE_2_NAME] → II. Spec-Driven Development with EARS
  [PRINCIPLE_3_NAME] → III. Test-First Discipline (NON-NEGOTIABLE)
  [PRINCIPLE_4_NAME] → IV. Modular Monolith Boundaries
  [PRINCIPLE_5_NAME] → V. Security & Data Protection by Default

Added sections:
  - Approved Toolchain & Target Stack (was [SECTION_2_NAME])
  - Development Workflow & Quality Gates (was [SECTION_3_NAME])
  - Governance (concrete rules)

Removed sections: none (all template slots populated)

Templates requiring updates:
  ✅ .specify/templates/plan-template.md — "Constitution Check" gate is generic ("Gates
     determined based on constitution file"); compatible, no edit required.
  ✅ .specify/templates/spec-template.md — no constitution-specific references; compatible.
  ✅ .specify/templates/tasks-template.md — no constitution-specific references; compatible.
  ✅ .specify/templates/checklist-template.md — no constitution-specific references; compatible.
  ✅ .github/copilot-instructions.md — already aligned (source of derived principles).

Follow-up TODOs: none. RATIFICATION_DATE set to initial adoption date (2026-06-10).
-->

# SIFAP Modernization Constitution

This constitution governs the modernization of the legacy **SIFAP** system (Sistema de
Fiscalização e Administração de Pagamentos — Natural/Adabas, ~29 years) into a Java 21 +
Next.js 15 target stack. It supersedes ad-hoc practice. All specs, code, tests, and reviews
MUST comply.

## Core Principles

### I. Legacy Traceability (NON-NEGOTIABLE)

Every requirement and every unit of new application code MUST be traceable to the legacy
source or be explicitly justified as new.

- Every requirement MUST carry a `source_legacy:` line pointing to
  `01-arqueologia/legado-sifap/natural-programs/*.NSN`,
  `01-arqueologia/legado-sifap/adabas-ddms/*.ddm`, or `[GREENFIELD]` + written justification.
- No new application code MAY be generated before the relevant legacy program/DDM has been
  read and a REQ-ID with `source_legacy:` exists.
- Legacy behaviors that are backdoors, dead code, or known defects (e.g., region 99 bypass,
  CPF "000" exception, hidden exclusions in audit) MUST NOT be silently reimplemented; each
  requires an explicit, documented product/security decision.

**Rationale:** The entire value of this modernization is faithful, auditable migration. The CI
job `legacy-traceability` rejects PRs that violate this; traceability is the spine that keeps
spec → code → test honest and demo-ready.

### II. Spec-Driven Development with EARS

Specification precedes implementation. Requirements are written before code, not after.

- Every requirement MUST use **EARS** notation (one of: Ubiquitous, Event-driven,
  State-driven, Optional, Unwanted, Complex) and carry a unique `REQ-NNN` ID.
- Mysteries, open questions, and unconfirmed legacy behavior MUST live in an "Open Questions"
  section and MUST NOT be promoted to requirements until resolved by a human decision.
- Architectural choices with two or more viable options MUST be captured in an ADR (MADR
  format) before the design depending on them is finalized.

**Rationale:** EARS + REQ-IDs give testable, unambiguous, reviewable requirements and a stable
anchor for tests and traceability across the four-stage workshop flow.

### III. Test-First Discipline (NON-NEGOTIABLE)

Tests are written while implementing, never after the fact.

- Business logic MUST have automated tests; tests MUST trace to their `REQ-ID` via inline
  comments.
- Migrated financial calculations (benefit, discounts, 13º, correction) MUST have
  equivalence/characterization tests asserting parity with the documented legacy behavior
  before refactoring or unification.
- Backend uses JUnit 5 + Testcontainers; frontend uses Vitest + Testing Library. A change to
  business logic without an accompanying test MUST NOT be merged.

**Rationale:** Migration risk is highest in duplicated/divergent calculation logic; tests that
pin legacy behavior are the only safe way to unify it without changing payouts.

### IV. Modular Monolith Boundaries

The target is a single deployable Modular Monolith organized by bounded context, not
microservices.

- Source MUST be package-by-feature: one top-level package per bounded context plus a minimal
  `shared` kernel.
- Only a module's `api/` (REST) and explicitly exported `spi/` interfaces are public; other
  modules MUST NOT import another context's `domain/`, `service/`, or `repository/`.
- Cross-context communication MUST be in-process (interface call or domain event); only IDs
  and DTO `record`s cross boundaries — never managed JPA entities. No HTTP between modules.

**Rationale:** Clear in-process boundaries preserve the discovered context map, prevent the
legacy "everything shares the data" coupling from reappearing, and keep a single, demoable
deployment.

### V. Security & Data Protection by Default

Security and LGPD compliance are built in, not bolted on.

- Code MUST be free of the OWASP Top 10 classes; inputs MUST be validated at every system
  boundary (controllers via `@Valid` + Bean Validation).
- Sensitive data (CPF, benefit values) MUST be masked in logs and consistently masked in
  UI/reports; secrets MUST NOT be hardcoded — use `azurerm_key_vault_secret` / managed
  identity.
- SQL access MUST go through JPA/JPQL (no string concatenation); CORS MUST be explicit (no
  wildcard `*` in production); auth via OAuth2/JWT (Spring Security).

**Rationale:** SIFAP handles social-benefit data for millions of citizens; a single leak or
fraud vector (the legacy system has four planted backdoors) is a compliance and human harm
event, not just a bug.

## Approved Toolchain & Target Stack

The toolchain is fixed; mixing tools breaks traceability and the workshop demos.

- **Editor/AI:** VS Code (or Insiders) + GitHub Copilot (Ask · Plan · Agent) only. Other AI
  assistants and alternative IDEs MUST NOT be used to generate code.
- **SDD:** GitHub Spec-Kit (`/speckit.*`); **Source of truth:** GitHub Issues/PRs/Actions.
- **Containers:** Docker + Docker Compose; **IaC:** Terraform (Azure provider ~> 3.x).
- **Target stack:** Java 21 + Spring Boot 3.3 + JPA/Hibernate + PostgreSQL 16 (backend);
  Next.js 15 App Router + TypeScript 5 `strict` + Tailwind + shadcn/ui (frontend).
- Java MUST use Java 21 idioms (records, sealed types, pattern matching, virtual threads),
  return `Optional` instead of `null` from public methods, and place `@Transactional` only in
  the service layer. TypeScript MUST be `strict: true` with named exports only and server
  actions for mutations.

## Development Workflow & Quality Gates

- **Branching:** `spec/<NNN>-<feature>` → `develop` → `main` (no `stage`). No merge to `main`
  without at least one peer review.
- **REST conventions:** paths `/api/v1/{resource}`, correct HTTP verbs and status codes
  (`201`/`204`/`409`), OpenAPI annotations on every endpoint.
- **Dependencies:** new dependencies MUST be justified in an ADR.
- **CI gates that MUST pass before merge:** `legacy-traceability`, build, lint,
  `terraform fmt` + `terraform validate` (for infra), and the test suites for changed areas.
- **Stage handoffs:** the guided handoff conversations at stage transitions
  (`00-TEAM-FLOW.md`) MUST NOT be skipped.

## Governance

- This constitution supersedes other practices. When guidance conflicts, the constitution
  wins; `.github/copilot-instructions.md` operationalizes it and MUST stay consistent with it.
- **Amendment procedure:** propose the change via PR editing this file, include a Sync Impact
  Report, obtain peer review, and update any dependent templates in the same PR.
- **Versioning policy (semantic):** MAJOR for backward-incompatible governance/principle
  removals or redefinitions; MINOR for a new principle/section or materially expanded guidance;
  PATCH for clarifications and non-semantic refinements.
- **Compliance review:** every PR and review MUST verify compliance with the principles above;
  any deviation MUST be justified in the PR (and, where structural, in an ADR). Unjustified
  complexity MUST be rejected.

**Version**: 1.0.0 | **Ratified**: 2026-06-10 | **Last Amended**: 2026-06-10
