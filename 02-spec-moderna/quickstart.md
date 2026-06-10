# Quickstart — SIFAP Core Modernization

> Phase 1 validation guide. Runnable scenarios that prove the four bounded contexts work end-to-end. This is a **run/validation guide**, not implementation — entity/service/migration bodies belong to Stage 3 (`tasks.md` + `@builder-agent`). References: [plan.md](plan.md), [data-model.md](data-model.md), [contracts/](contracts/README.md), [openapi.yaml](openapi.yaml).

## Prerequisites

- Docker + Docker Compose (PostgreSQL 16 for local parity)
- JDK 21, Maven; Node 20+ (frontend)
- Backend stack: Spring Boot 3.3 + Spring Modulith; Frontend: Next.js 15

## Setup

```bash
# 1. Start PostgreSQL 16 (schemas: beneficiaries, programs, payments, audit)
docker compose up -d postgres

# 2. Backend: apply Flyway migrations + run the Modular Monolith
cd backend && ./mvnw spring-boot:run

# 3. Frontend (separate shell)
cd frontend && npm install && npm run dev
```

## Validation scenarios

Each scenario maps to requirements and is implemented as an automated test in Stage 3 (the manual `curl` here is for smoke validation). Assert truncation (not rounding) on all money values (REQ-021).

### S1 — Cadastro + validation (Beneficiaries) — REQ-001..008

1. POST `/api/v1/beneficiarios` with an invalid CPF (bad Módulo 11) → expect `400` with accumulated validation errors (REQ-001/002).
2. POST with all-equal CPF `111.111.111-11` → `400` (REQ-003).
3. POST a valid beneficiary aged 80 → `201`, status `S` (auto-suspension >75, REQ-008).
4. POST a valid beneficiary aged 40 → `201`, status `A` (REQ-006).
5. POST the same CPF again → `409` (REQ-005).

**Expected:** validation rejects bad input with all messages at once; status rules applied.

### S2 — Dependentes (Beneficiaries) — REQ-009..011

1. Add 5 dependents (parentesco `FI`) → all `201`.
2. Add a 6th → `409` (limit 5, REQ-009).
3. Add a dependent with parentesco `XX` → `400` (REQ-011).
4. With a titular whose status is `C`, add a dependent → `409` (REQ-010).

### S3 — Programs + Fator-K (Programs) — REQ-016/017

1. POST `/api/v1/programas` with vlrBase=1000, reajuste=0.05 → `201`; GET `/api/v1/programas/{cod}` and assert the persisted vlrBase equals `1000 × (1 + 0.05 × K)`, truncated (REQ-017).
2. POST the same código again → `409` (REQ-016).

### S4 — Eligibility (Beneficiaries → Programs SPI) — REQ-012..014

1. GET `/api/v1/beneficiarios/{cpf}/elegibilidade?programa={P-type}` for a beneficiary aged 58 → `elegivel=false`, motivo idade mínima (REQ-013/014).
2. For a suspended beneficiary → `elegivel=false`, motivo "suspenso" (REQ-012).

### S5 — Folha mensal (Payments) — REQ-018..031

1. Seed 3 active beneficiaries (distinct income brackets) + their programs.
2. POST `/api/v1/folhas` with `{ "competencia": "202606" }` → `201`; resumo shows 3 generated (status `GERADO`), totals truncated (REQ-021/029).
3. POST the same competência again → resumo shows 3 ignored (idempotency, REQ-030).
4. POST competência `202612` (December) → tipo `D`, 13º present; abono = 15% only for type `A` programs (REQ-022/023).
5. Verify net floored at 0 when discounts exceed gross (REQ-028); 30% cap applied to non-judicial, judicial exempt (REQ-026).

**Equivalence gate (Principle III):** before unifying CALCBENF×BATCHPGT, the `equivalence/` suite must assert parity with documented legacy outputs (REQ-018, OQ-02).

### S6 — Conciliação CNAB (Payments) — REQ-032/033

1. POST `/api/v1/conciliacoes` with a CNAB return: a `00` record → status `PAGO`; a `01` → `DEVOLVIDO`; a `02` → `ERRO` (REQ-032).
2. Submit a return whose value differs by > R$ 0,01 → a divergence is recorded and an audit event emitted (REQ-033).

### S7 — Correção monetária (Payments) — REQ-034

1. POST `/api/v1/correcoes` for a payment → corrected value stored, marked corrected.
2. POST again for the same payment → ignored (idempotency, REQ-034).

### S8 — Auditoria (Audit, event-driven) — REQ-036/037

1. After S6, GET `/api/v1/auditoria?de=2026-06-01&ate=2026-06-30&acao=CO` → conciliation events present (REQ-036).
2. Trigger a beneficiary exclusion, then GET auditoria filtering `acao=EX` → the exclusion **appears** (REQ-037 — legacy hid it; not reproduced).
3. Attempt to UPDATE/DELETE an audit row → rejected (immutable trail).

## Boundary checks (architecture)

- Run the ArchUnit suite: no module imports another context's `domain/`/`service/`/`repository/` (Principle IV).
- Confirm only DTO records cross SPI boundaries (no JPA entities).

## Done when

- All scenarios S1–S8 pass as automated tests, each tracing to its REQ-ID.
- Money values truncate, never round (REQ-021); CPF masked consistently (REQ-015).
- Open Questions (OQ-01 status de-para, OQ-04 Fator-K origin, OQ-S1 backdoors) resolved with stakeholders before the corresponding data migration / eligibility code is finalized.
