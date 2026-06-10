# Tasks: SIFAP Core Modernization (4 Bounded Contexts)

**Input**: Design documents from `02-spec-moderna/`
**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md) (user stories), [research.md](research.md), [data-model.md](data-model.md), [contracts/](contracts/README.md), [SPECIFICATION.md](SPECIFICATION.md)

**Tests**: INCLUDED — the [constitution](../.specify/memory/constitution.md) Principle III (Test-First) is NON-NEGOTIABLE. Every business-logic task is preceded by a failing test that traces to its REQ-ID.

**Organization**: Grouped by user story (one per bounded context) for independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: parallelizable (different files, no dependency on incomplete tasks)
- **[Story]**: US1 Beneficiaries · US2 Programs · US3 Payments · US4 Audit
- Paths follow [plan.md](plan.md) web-app layout: `backend/src/...`, `frontend/...`, base package `com.datacorp.app`

## Path Conventions

- Backend module root: `backend/src/main/java/com/datacorp/app/<context>/`
- Backend tests: `backend/src/test/java/com/datacorp/app/`
- Migrations: `backend/src/main/resources/db/migration/`
- Frontend: `frontend/app/`, `frontend/components/`, `frontend/tests/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Create Maven multi-module backend + frontend skeleton per [plan.md](plan.md) structure (`backend/`, `frontend/`, `docker-compose.yml`)
- [x] T002 Initialize Spring Boot 3.3 + Spring Modulith + Spring Data JPA + Spring Security + Flyway dependencies in `backend/pom.xml`
- [x] T003 [P] Configure PostgreSQL 16 service in `docker-compose.yml` for local parity
- [x] T004 [P] Configure backend formatting/linting (Spotless + Checkstyle) in `backend/pom.xml`
- [x] T005 [P] Initialize Next.js 15 (App Router) + TypeScript strict + Tailwind + shadcn/ui + Vitest in `frontend/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST complete before ANY user story

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T006 Configure Flyway + schema-per-context (`beneficiaries`, `programs`, `payments`, `audit`) and datasource in `backend/src/main/resources/application.yml`
- [x] T007 [P] Unit test for `Cpf` value object (Módulo 11, reject all-equal, unified mask incl. short-CPF normalization) in `backend/src/test/java/com/datacorp/app/shared/kernel/CpfTest.java` (REQ-001, REQ-003, REQ-015)
- [x] T008 [P] Implement `Cpf` value object in `backend/src/main/java/com/datacorp/app/shared/kernel/Cpf.java` (REQ-001, REQ-003, REQ-015)
- [x] T009 [P] Unit test for `Money` value object (truncate, never round, 2 decimals) in `backend/src/test/java/com/datacorp/app/shared/kernel/MoneyTest.java` (REQ-021)
- [x] T010 [P] Implement `Money` value object in `backend/src/main/java/com/datacorp/app/shared/kernel/Money.java` (REQ-021)
- [x] T011 [P] Implement `Competencia` (AAAAMM) and `CodPrograma` value objects in `backend/src/main/java/com/datacorp/app/shared/kernel/`
- [x] T012 [P] Define base domain-event types + publisher abstraction in `backend/src/main/java/com/datacorp/app/shared/events/`
- [x] T013 [P] Implement global error handling (`DomainException`, `ValidationException`, `ErrorResponse`, `@RestControllerAdvice`) in `backend/src/main/java/com/datacorp/app/shared/exception/`
- [x] T014 Configure Spring Security (OAuth2/JWT resource server, explicit CORS, no wildcard) in `backend/src/main/java/com/datacorp/app/shared/config/SecurityConfig.java`
- [x] T015 Configure springdoc OpenAPI from [openapi.yaml](openapi.yaml) in `backend/src/main/java/com/datacorp/app/shared/config/OpenApiConfig.java`
- [x] T016 [P] ArchUnit module-boundary test (no module imports another context's `domain/`/`service/`/`repository/`; only `api/`+`spi/` public) in `backend/src/test/java/com/datacorp/app/architecture/ModuleBoundaryTest.java` (Principle IV)
- [x] T017 [P] Configure Spring Modulith application module metadata + event publication registry in `backend/src/main/java/com/datacorp/app/SifapApplication.java`

**Checkpoint**: Foundation ready — user stories can begin.

---

## Phase 3: User Story 2 - Catálogo de Programas Sociais (Priority: P1) 🎯 MVP foundation

**Goal**: Authoritative program reference data (values, factors, Fator-K, eligibility rules, applicable discount types) consumed by US1 (eligibility) and US3 (calculation).

**Independent Test**: quickstart S3 — unique program code (409 on duplicate); persisted `vlrBase` adjusted by Fator-K.

> Sequenced before US1/US3 because both depend on its `ProgramaCatalogQuery` SPI. Still independently testable.

### Tests for User Story 2 ⚠️ (write first, must FAIL)

- [x] T018 [P] [US2] Contract test for `POST/GET /api/v1/programas` in `backend/src/test/java/com/datacorp/app/programs/contract/ProgramaContractTest.java` (REQ-016, REQ-017)
- [x] T019 [P] [US2] Unit test for Fator-K adjustment on inclusion in `backend/src/test/java/com/datacorp/app/programs/ProgramaCatalogServiceTest.java` (REQ-017)
- [x] T020 [P] [US2] Integration test (Testcontainers) for program uniqueness in `backend/src/test/java/com/datacorp/app/programs/integration/ProgramaPersistenceIT.java` (REQ-016)

### Implementation for User Story 2

- [x] T021 [P] [US2] Create `TipoDesconto` enum + `FaixaCalculo`/`ParamRegional` embeddables in `backend/src/main/java/com/datacorp/app/programs/domain/` (ADR-001, ADR-003)
- [x] T022 [US2] Create `ProgramaSocial` entity (`@ElementCollection` for descontos/faixas/regionais) in `backend/src/main/java/com/datacorp/app/programs/domain/ProgramaSocial.java` (REQ-016, REQ-017, ADR-001/003)
- [x] T023 [US2] Flyway migration `V2__programs_schema.sql` (programa_social + child tables) in `backend/src/main/resources/db/migration/` (REQ-016)
- [x] T024 [US2] Implement `ProgramaRepository` in `backend/src/main/java/com/datacorp/app/programs/repository/ProgramaRepository.java`
- [x] T025 [US2] Implement `ProgramaCatalogService` with Fator-K adjustment in `backend/src/main/java/com/datacorp/app/programs/service/ProgramaCatalogService.java` (REQ-016, REQ-017)
- [x] T026 [US2] Implement `ProgramaCatalogQuery` SPI + `ProgramaPolicy` record in `backend/src/main/java/com/datacorp/app/programs/spi/` (REQ-013, REQ-014, REQ-018, REQ-025)
- [x] T027 [US2] Implement `ProgramaController` (POST/GET, `@Valid`, OpenAPI) in `backend/src/main/java/com/datacorp/app/programs/api/ProgramaController.java` (REQ-016, REQ-017)

**Checkpoint**: Programs catalog functional and independently testable.

---

## Phase 4: User Story 1 - Cadastro & Elegibilidade de Beneficiários (Priority: P1) 🎯 MVP

**Goal**: Register/maintain beneficiaries + dependents with unified CPF/document validation, and decide eligibility.

**Independent Test**: quickstart S1–S4 — invalid CPF/identity rejected with accumulated errors; status rules (initial A, auto-suspend >75); ≤5 dependents; eligibility by status/age/income/program type.

### Tests for User Story 1 ⚠️ (write first, must FAIL)

- [x] T028 [P] [US1] Contract tests for `/api/v1/beneficiarios` (POST/PUT/GET, dependentes, elegibilidade) in `backend/src/test/java/com/datacorp/app/beneficiaries/contract/BeneficiarioContractTest.java` (REQ-001..015)
- [x] T029 [P] [US1] Unit test for accumulative validation (CPF, nome+sobrenome, UF, all-equal CPF) in `backend/src/test/java/com/datacorp/app/beneficiaries/CadastroServiceTest.java` (REQ-001..004, REQ-007)
- [x] T030 [P] [US1] Unit test for auto-suspension >75 and initial status A in same suite (REQ-006, REQ-008)
- [x] T031 [P] [US1] Unit test for dependent rules (max 5, parentesco, titular C/D block) in `backend/src/test/java/com/datacorp/app/beneficiaries/DependenteServiceTest.java` (REQ-009..011)
- [x] T032 [P] [US1] Unit test for eligibility (status/age/income/type) in `backend/src/test/java/com/datacorp/app/beneficiaries/ElegibilidadeServiceTest.java` (REQ-012..014)
- [x] T033 [P] [US1] Integration test (Testcontainers) for cadastro + uniqueness + masked consult in `backend/src/test/java/com/datacorp/app/beneficiaries/integration/BeneficiarioIT.java` (REQ-005, REQ-015)

### Implementation for User Story 1

- [x] T034 [P] [US1] Create `StatusBeneficiario` enum + `Dependente` embeddable + `Parentesco` enum in `backend/src/main/java/com/datacorp/app/beneficiaries/domain/` (REQ-007, REQ-011)
- [x] T035 [US1] Create `Beneficiario` entity (`@ElementCollection` dependentes, max 5) in `backend/src/main/java/com/datacorp/app/beneficiaries/domain/Beneficiario.java` (REQ-009, ADR-003)
- [x] T036 [US1] Flyway migration `V3__beneficiaries_schema.sql` (beneficiario + beneficiario_dependente) in `backend/src/main/resources/db/migration/`
- [x] T037 [US1] Implement `BeneficiarioRepository` in `backend/src/main/java/com/datacorp/app/beneficiaries/repository/BeneficiarioRepository.java`
- [x] T038 [US1] Implement `CadastroService` (accumulative validation, status rules, uniqueness) in `backend/src/main/java/com/datacorp/app/beneficiaries/service/CadastroService.java` (REQ-001..008)
- [x] T039 [US1] Implement dependent management in `CadastroService`/`DependenteService` (REQ-009..011)
- [x] T040 [US1] Implement `ElegibilidadeService` consuming `ProgramaCatalogQuery` (ACL) in `backend/src/main/java/com/datacorp/app/beneficiaries/service/ElegibilidadeService.java` (REQ-012..014, depends on T026)
- [x] T041 [US1] Implement `BeneficiarioQuery` + `ElegibilidadeApi` SPI + DTOs (`BeneficiarioSnapshot`, `ResultadoElegibilidade`) in `backend/src/main/java/com/datacorp/app/beneficiaries/spi/`
- [x] T042 [US1] Publish `BeneficiarioExcluido`/`BeneficiarioStatusAlterado` domain events in `CadastroService` (REQ-037)
- [x] T043 [US1] Implement `BeneficiarioController` (POST/PUT/GET + dependentes + elegibilidade, `@Valid`, masked CPF) in `backend/src/main/java/com/datacorp/app/beneficiaries/api/BeneficiarioController.java` (REQ-001..015)
- [x] T044 [P] [US1] Frontend beneficiary cadastro + consult pages in `frontend/app/beneficiarios/` (REQ-001..015)

**Checkpoint**: US1 + US2 functional and independently testable.

---

## Phase 5: User Story 3 - Pagamentos & Folha (Priority: P2)

**Goal**: Generate monthly folha; unified benefit/discount/13º/correction calculation; payment status machine; CNAB reconciliation.

**Independent Test**: quickstart S5–S7 — idempotent folha (GERADO), unified calculation parity, 30% cap with judicial exemption, net floor 0, CNAB reconciliation (P/D/E), divergence detection, idempotent correction.

### Tests for User Story 3 ⚠️ (write first, must FAIL)

- [x] T045 [P] [US3] Contract tests for `/api/v1/folhas`, `/pagamentos`, `/conciliacoes`, `/correcoes`, `/relatorios/consolidado` in `backend/src/test/java/com/datacorp/app/payments/contract/PagamentoContractTest.java` (REQ-018..035)
- [x] T046 [P] [US3] **Equivalence test** asserting unified calculation parity with documented legacy CALCBENF/BATCHPGT outputs in `backend/src/test/java/com/datacorp/app/payments/equivalence/CalculoEquivalenceTest.java` (REQ-018, OQ-02)
- [x] T047 [P] [US3] Unit test for income-bracket factor, regional factor, 13º/abono, truncation in `backend/src/test/java/com/datacorp/app/payments/CalculoBeneficioServiceTest.java` (REQ-018..023)
- [x] T048 [P] [US3] Unit test for discounts (contribution brackets, 30% cap, judicial exemption, net floor 0) in `backend/src/test/java/com/datacorp/app/payments/DescontoServiceTest.java` (REQ-025..028)
- [x] T049 [P] [US3] Unit test for `StatusPagamento` transitions (erro≠estornado) in `backend/src/test/java/com/datacorp/app/payments/StatusPagamentoTest.java` (ADR-002, OQ-01)
- [x] T050 [P] [US3] Integration test (Testcontainers) for idempotent folha + CNAB reconciliation + correction in `backend/src/test/java/com/datacorp/app/payments/integration/FolhaIT.java` (REQ-029..034)

### Implementation for User Story 3

- [x] T051 [P] [US3] Create `StatusPagamento` enum with explicit transitions in `backend/src/main/java/com/datacorp/app/payments/domain/StatusPagamento.java` (ADR-002)
- [x] T052 [P] [US3] Create `Desconto` embeddable + bank/reconciliation embeddables in `backend/src/main/java/com/datacorp/app/payments/domain/`
- [x] T053 [US3] Create `Pagamento` entity (`@ElementCollection` descontos) in `backend/src/main/java/com/datacorp/app/payments/domain/Pagamento.java` (ADR-003)
- [x] T054 [US3] Flyway migration `V4__payments_schema.sql` (pagamento + pagamento_desconto partitioned by competência, index on tipo_desconto) in `backend/src/main/resources/db/migration/` (ADR-003)
- [x] T055 [US3] Implement `PagamentoRepository` in `backend/src/main/java/com/datacorp/app/payments/repository/PagamentoRepository.java`
- [x] T056 [US3] Implement `CalculoBeneficioService` (single unified rule, consumes Beneficiaries+Programs SPI) in `backend/src/main/java/com/datacorp/app/payments/service/CalculoBeneficioService.java` (REQ-018..024, OQ-02)
- [x] T057 [US3] Implement `DescontoService` (brackets, 30% cap, judicial exemption, net floor) in `backend/src/main/java/com/datacorp/app/payments/service/DescontoService.java` (REQ-025..028)
- [x] T058 [US3] Implement `FolhaService` (idempotent monthly generation, status GERADO, resumo) in `backend/src/main/java/com/datacorp/app/payments/service/FolhaService.java` (REQ-029..031)
- [x] T059 [US3] Implement `ConciliacaoService` (CNAB 240 parse, status P/D/E, divergence) in `backend/src/main/java/com/datacorp/app/payments/service/ConciliacaoService.java` (REQ-032, REQ-033)
- [x] T060 [US3] Implement `CorrecaoService` (idempotent monetary correction) in `backend/src/main/java/com/datacorp/app/payments/service/CorrecaoService.java` (REQ-034)
- [x] T061 [US3] Implement consolidated report query (macroregion + status) in `backend/src/main/java/com/datacorp/app/payments/service/RelatorioService.java` (REQ-035)
- [x] T062 [US3] Implement `PagamentoHistoryQuery` SPI + `PagamentoResumo` record in `backend/src/main/java/com/datacorp/app/payments/spi/`
- [x] T063 [US3] Publish `PagamentoConciliado`/`PagamentoDevolvido`/`PagamentoEstornado`/`DivergenciaConciliacao` events in conciliation/correction services (REQ-036)
- [x] T064 [US3] Implement `FolhaController` + `ConciliacaoController` + report/correction endpoints in `backend/src/main/java/com/datacorp/app/payments/api/` (REQ-029..035)
- [x] T065 [P] [US3] Frontend folha + reconciliation + report pages in `frontend/app/pagamentos/` (REQ-029..035)

**Checkpoint**: US1, US2, US3 all independently functional.

---

## Phase 6: User Story 4 - Auditoria & Conformidade (Priority: P3)

**Goal**: Immutable, event-fed audit trail with exclusions always visible (IN-TCU 63).

**Independent Test**: quickstart S8 — conciliation/divergence audit events present; exclusions ('EX') visible; trail immutable (UPDATE/DELETE rejected).

### Tests for User Story 4 ⚠️ (write first, must FAIL)

- [x] T066 [P] [US4] Contract test for `GET /api/v1/auditoria` in `backend/src/test/java/com/datacorp/app/audit/contract/AuditoriaContractTest.java` (REQ-036, REQ-037)
- [x] T067 [P] [US4] Integration test (Testcontainers): events produce audit rows, exclusions visible, immutability enforced in `backend/src/test/java/com/datacorp/app/audit/integration/AuditoriaIT.java` (REQ-036, REQ-037)

### Implementation for User Story 4

- [x] T068 [P] [US4] Create `EventoAuditoria` entity (append-only) + `AcaoAuditoria` enum in `backend/src/main/java/com/datacorp/app/audit/domain/` (REQ-036)
- [x] T069 [US4] Flyway migration `V5__audit_schema.sql` (auditoria, insert-only; revoke UPDATE/DELETE) in `backend/src/main/resources/db/migration/` (REQ-037, IN-TCU 63)
- [x] T070 [US4] Implement `AuditoriaRepository` (insert + query only) in `backend/src/main/java/com/datacorp/app/audit/repository/AuditoriaRepository.java`
- [x] T071 [US4] Implement `AuditoriaService` with `@ApplicationModuleListener` consuming domain events in `backend/src/main/java/com/datacorp/app/audit/service/AuditoriaService.java` (ADR-004, REQ-036)
- [x] T072 [US4] Implement `AuditoriaQuery` SPI + `AuditoriaController` (exclusions always visible) in `backend/src/main/java/com/datacorp/app/audit/spi/` and `.../audit/api/AuditoriaController.java` (REQ-037)

**Checkpoint**: All four user stories independently functional.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [x] T073 [P] Verify ArchUnit boundary + SPI-only-DTO tests pass across all modules (Principle IV)
- [x] T074 [P] Add CPF/value masking to logging config across modules (REQ-015, Principle V)
- [x] T075 [P] Document legacy→canonical `StatusPagamento` de-para migration (pending OQ-01 stakeholder sign-off) in `backend/src/main/resources/db/migration/notes/status-depara.md` (ADR-002)
- [x] T076 [P] Add `.github/workflows` CI gates: build, tests, `legacy-traceability`, Spotless (Principle I, Dev Workflow)
- [x] T077 [P] Frontend audit-trail page in `frontend/app/auditoria/` (REQ-037)
- [x] T078 Run full [quickstart.md](quickstart.md) S1–S8 validation end-to-end
- [x] T079 Performance pass on `pagamento_desconto` partitioning/indexes (ADR-003; revisit after SLO clarification, research U1/R11)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (P1)**: no dependencies.
- **Foundational (P2)**: depends on Setup — BLOCKS all user stories.
- **US2 Programs (Phase 3)**: after Foundational. Provides `ProgramaCatalogQuery` consumed by US1 + US3.
- **US1 Beneficiaries (Phase 4)**: after Foundational; eligibility (T040) depends on US2 T026 (or a test double for independent testing).
- **US3 Payments (Phase 5)**: after Foundational; calculation (T056) depends on US1 T041 + US2 T026 SPIs.
- **US4 Audit (Phase 6)**: after Foundational; listeners consume events emitted by US1 (T042) + US3 (T063).
- **Polish (Phase 7)**: after all targeted stories.

### Within Each User Story

- Tests written and FAILING before implementation (Principle III).
- Domain/enums → entity → migration → repository → service → SPI → controller → frontend.

### Parallel Opportunities

- Setup: T003, T004, T005 in parallel.
- Foundational: T007–T013, T016, T017 (different files) in parallel; T008 after T007, T010 after T009.
- Each story's `[P]` test tasks run in parallel; `[P]` domain/enum tasks run in parallel.
- After Foundational, with enough staff: US2 first, then US1 and (once SPIs exist) US3; US4 last (needs events).

---

## Parallel Example: User Story 1

```bash
# Tests first (parallel):
Task: "Contract tests for /api/v1/beneficiarios (T028)"
Task: "Unit test accumulative validation (T029)"
Task: "Unit test dependent rules (T031)"
Task: "Unit test eligibility (T032)"

# Then domain in parallel:
Task: "StatusBeneficiario + Dependente + Parentesco (T034)"
```

---

## Implementation Strategy

### MVP scope

**US2 (Programs) + US1 (Beneficiaries)** form the MVP: reference data + beneficiary lifecycle + eligibility — the base of everything (discovery report §5.1, priority 1). Deliver and validate these (quickstart S1–S4) before US3.

1. Phase 1 Setup → Phase 2 Foundational (CRITICAL, blocks all).
2. Phase 3 US2 → Phase 4 US1 → **STOP & VALIDATE** (S1–S4) = MVP.
3. Phase 5 US3 (financial core) → validate S5–S7.
4. Phase 6 US4 (audit) → validate S8.
5. Phase 7 Polish.

> **Gate before US3 data work:** resolve OQ-01 (status de-para), OQ-04 (Fator-K origin), OQ-S1 (backdoors) with stakeholders — see [SPECIFICATION.md Open Questions](SPECIFICATION.md#open-questions-não-são-requisitos-ainda).
