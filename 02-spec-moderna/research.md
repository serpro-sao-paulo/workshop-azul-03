# Phase 0 — Research: SIFAP Core Modernization

> Output of `/speckit.plan` Phase 0. Resolves the `NEEDS CLARIFICATION` from the Technical Context and consolidates the technology decisions (most already fixed by the [constitution](../.specify/memory/constitution.md) and [ADRs](ADRs/)). Format per decision: **Decision · Rationale · Alternatives considered**.

## Unknowns from Technical Context

| # | Unknown | Source | Status |
| - | ------- | ------ | ------ |
| U1 | Performance / SLO targets (p95 latency, monthly folha batch window) | Technical Context "Performance Goals" | **Open** — not in spec; carried to Open Questions, non-blocking for design |
| U2 | Authoritative legacy→canonical `PAGAMENTO` status mapping | OQ-01 / ADR-002 | Open — needs SENARC/CGPB validation; design proceeds with proposed canonical set |
| U3 | Origin of Fator-K constant `0.347215` and double-reajuste semantics | OQ-04 | Open — needs gestor validation; data migration blocked until resolved |
| U4 | Backdoor disposition (region 99, CPF 000, VALDOCS prefixes, hidden exclusions) | OQ-S1 | Open — product/security/legal decision; not implemented until decided |

> U2–U4 are **business/security decisions**, not technology research; they remain in [SPECIFICATION.md Open Questions](SPECIFICATION.md#open-questions-não-são-requisitos-ainda). The design is structured so each can be plugged in without rework (status enum + de-para table; externalized Fator-K parameter; eligibility/validation hooks).

## Technology Decisions

### R1 — Backend platform: Java 21 + Spring Boot 3.3 + Spring Modulith

- **Decision:** Java 21 / Spring Boot 3.3 with **Spring Modulith** for module boundaries and the in-process event registry.
- **Rationale:** Fixed by the constitution (Approved Toolchain). Spring Modulith directly enforces Principle IV (package-by-feature boundaries, only `api/`+`spi/` public) and provides a durable event publication registry that ADR-004 relies on for the audit trail. Java 21 records/sealed types/virtual threads suit DTOs, the status union, and the high-fan-out monthly batch.
- **Alternatives considered:** Plain Spring Boot + ArchUnit only (no Modulith) — rejected: loses the event registry durability ADR-004 needs. Quarkus/Micronaut — rejected: outside the approved toolchain.

### R2 — Persistence: PostgreSQL 16, schema-per-context, Flyway

- **Decision:** Single PostgreSQL 16 database, one schema per bounded context; Flyway migrations under `db/migration`; JPA/Hibernate 6 access only.
- **Rationale:** Constitution mandates PostgreSQL 16 and JPA/JPQL-only (Principle V). Schema-per-context keeps data ownership explicit (a module touches only its schema) while staying a single deployable Modular Monolith (no cross-service DB). Flyway gives rollback-safe, reviewable schema changes (database instructions).
- **Alternatives considered:** Database-per-context — rejected: that is microservices, violates Principle IV. Liquibase — viable, but Flyway is the simpler convention for SQL-first migrations here.

### R3 — Adabas MU (scalar multi-value) → relational

- **Decision:** Map the single MU field (`PROGRAMA-SOCIAL.TIPO-DSCT-APLIC`) as `@ElementCollection` of a `TipoDesconto` enum.
- **Rationale:** [ADR-001](ADRs/adr-001-map-adabas-mu-fields-to-jsonb-vs-elementcollection.md). Controlled vocabulary on a ~45-row reference table; REQ-025/026/027 need reliable, consultable membership; integrity + SQL portability beat JSONB flexibility at this volume.
- **Alternatives considered:** JSONB array — rejected (loss of integrity/typing, non-portable membership queries). Reserved for high-volume PE only.

### R4 — Adabas PE (periodic groups) → relational `@ElementCollection`/`@Embeddable`

- **Decision:** All four PE groups (`GRP-DEPENDENTE`, `GRP-FAIXA-CALCULO`, `GRP-PARAM-REGIONAL`, `GRP-DESCONTO`) map to child tables; `pagamento_desconto` is partitioned by competência with an index on `tipo_desconto`.
- **Rationale:** [ADR-003](ADRs/adr-003-mapeamento-grupos-periodicos-pe-adabas-para-jpa.md). Discount requirements need per-type queryability and integrity; the ~180M-row volume risk is managed by partitioning/indexing rather than by surrendering to JSONB. Uniform mapping reduces cognitive load.
- **Alternatives considered:** Hybrid JSONB for `GRP-DESCONTO` — rejected now, reserved as a measured optimization if Stage-3 profiling proves it a bottleneck.

### R5 — `PAGAMENTO` status machine

- **Decision:** `StatusPagamento` enum with explicit transitions, owned solely by Payments; legacy codes normalized via a documented, version-controlled de-para during data migration. Canonical set: `GERADO → EMITIDO → PAGO | DEVOLVIDO | ERRO`, with `CANCELADO`/`ESTORNADO` as distinct terminal states.
- **Rationale:** [ADR-002](ADRs/adr-002-maquina-de-status-unica-do-pagamento.md), resolving OQ-01. Eliminates the legacy 'E' (erro × estornado) and 'P' (pendente × pago) collisions over 180M rows; single source of truth removes the uncoordinated mutation by 8 programs.
- **Alternatives considered:** Preserve single-char legacy codes — rejected: transports the defect into the new system.

### R6 — Cross-context communication (mixed)

- **Decision:** Synchronous in-process SPI interface calls for reads (Payments→Beneficiaries/Programs; Beneficiaries→Programs/Payments); one-way domain events for the audit trail (Spring Modulith event publication).
- **Rationale:** Matches `communication=mixed` in the design. Reads need immediate, transactional data; audit must be decoupled, durable, and off the folha critical path ([ADR-004](ADRs/adr-004-auditoria-desacoplada-por-domain-events.md)). Only IDs and DTO records cross boundaries (Principle IV).
- **Alternatives considered:** All-synchronous audit calls — rejected (couples audit to business transactions, risks the 180M-row folha). All-events — rejected (eventual consistency unacceptable for reads feeding a calculation).

### R7 — Unifying duplicated legacy calculation (CALCBENF × BATCHPGT)

- **Decision:** One `CalculoBeneficioService` in Payments is the single calculation rule for both online and batch (REQ-018); pinned by equivalence/characterization tests before unification.
- **Rationale:** OQ-02 / Principle III. The legacy duplicated tables and diverged discounts (3% simplified in batch vs full CALCDSCT) are the highest financial migration risk; characterization tests asserting parity with documented legacy outputs are the only safe way to merge them.
- **Alternatives considered:** Keep two code paths — rejected: perpetuates divergence.

### R8 — Truncation, masking, and LGPD

- **Decision:** A `Money` value object truncates (never rounds) to 2 decimals (REQ-021); a `Cpf` value object centralizes Módulo 11 validation and a single masking rule normalizing short CPFs to 11 digits before masking (REQ-015); sensitive data masked in logs.
- **Rationale:** Principle V + REQ-015/021. The legacy had inconsistent masks (CONSBENF vs RELPGT) and a known LGPD leak on short CPFs; the unified value objects fix both by construction. BATCHREL's rounding divergence (OQ-06) is resolved by mandating truncation everywhere.
- **Alternatives considered:** Per-call masking/formatting — rejected: that is exactly how the legacy diverged.

### R9 — Security: OAuth2/JWT, validation, secrets

- **Decision:** Spring Security with OAuth2/JWT; `@Valid` + Bean Validation at every controller boundary; explicit CORS (no `*` in prod); secrets via Azure Key Vault + managed identity.
- **Rationale:** Constitution Principle V / security instructions. Validation at boundaries (REQ-001…011 are largely Unwanted/validation rules) maps cleanly to Bean Validation; no secret in code.
- **Alternatives considered:** Session auth — rejected (SPA + API favors stateless JWT). App-managed secrets — rejected (managed identity required).

### R10 — Testing strategy

- **Decision:** JUnit 5 + Testcontainers (PostgreSQL) for integration; per-REQ unit tests with inline `REQ-ID` traceability; an `equivalence/` suite for financial parity; OpenAPI contract tests; ArchUnit module-boundary tests; Vitest + Testing Library for frontend.
- **Rationale:** Principle III (test-first, REQ-traced) + tests instructions. Testcontainers gives real-PostgreSQL fidelity for the partitioned `pagamento_desconto`; ArchUnit guards Principle IV.
- **Alternatives considered:** H2 in-memory — rejected (diverges from PostgreSQL partitioning/JSONB semantics). Mock-only — rejected (misses integration risk on the data hub).

### R11 — Performance/SLO (U1)

- **Decision:** Proceed with design now; record SLOs as an open item to confirm with stakeholders. Working assumptions for design (not commitments): online reads p95 < 300 ms; monthly folha must complete within its scheduled batch window processing ~4.2M active beneficiaries idempotently (REQ-029/030/031). Partitioning `pagamento` by competência and indexing support both.
- **Rationale:** Spec does not state SLOs; design choices (partitioning, batch resilience, virtual threads) are robust across plausible targets, so the unknown does not block Phase 1.
- **Alternatives considered:** Block on SLOs — rejected: unnecessary; revisit in `/speckit.clarify`.

## Summary

All technology unknowns are resolved or pinned by constitution/ADRs. The only true `NEEDS CLARIFICATION` (U1, performance SLOs) is non-blocking and recorded for `/speckit.clarify`. Business/security Open Questions (U2–U4) are intentionally outside research scope and are designed around, not coded, until human decisions land.
