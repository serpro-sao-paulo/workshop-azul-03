# Contracts — SIFAP Core Modernization

> Phase 1 contract index. The machine-readable contract is the OpenAPI 3.0 skeleton at [../openapi.yaml](../openapi.yaml) (15 operations across the 4 contexts). This folder records the **per-context contract surface** — REST endpoints and in-process SPI interfaces — with REQ traceability. Schema bodies are completed in Stage 3.

## How to read this

- **REST** = public HTTP surface (controllers in `api/`), consumed by the Next.js frontend and external callers. Base path `/api/v1`.
- **SPI** = in-process interfaces (`spi/`) consumed by other modules. No HTTP; only DTO `record`s cross boundaries (Principle IV).
- Each row cites the requirement(s) it satisfies. Full request/response schemas: [../openapi.yaml](../openapi.yaml); DTO shapes: [../modular-monolith-design.md](../modular-monolith-design.md).

---

## Beneficiaries

**REST**

| Method | Path | REQ |
| ------ | ---- | --- |
| POST | `/api/v1/beneficiarios` | REQ-001..008 |
| PUT | `/api/v1/beneficiarios/{cpf}` | REQ-007 |
| GET | `/api/v1/beneficiarios/{cpf}` | REQ-015 |
| POST | `/api/v1/beneficiarios/{cpf}/dependentes` | REQ-009, REQ-010, REQ-011 |
| GET | `/api/v1/beneficiarios/{cpf}/elegibilidade?programa={cod}` | REQ-012, REQ-013, REQ-014 |

**SPI (consumed by Payments)**

- `BeneficiarioQuery.findByCpf(Cpf) → Optional<BeneficiarioSnapshot>` — REQ-018, REQ-024, REQ-029
- `ElegibilidadeApi.avaliar(Cpf, CodPrograma) → ResultadoElegibilidade` — REQ-012..014

**Emits (events → Audit):** `BeneficiarioExcluido`, `BeneficiarioStatusAlterado` — REQ-037

---

## Programs

**REST**

| Method | Path | REQ |
| ------ | ---- | --- |
| POST | `/api/v1/programas` | REQ-016, REQ-017 |
| GET | `/api/v1/programas` | REQ-014 |
| GET | `/api/v1/programas/{cod}` | REQ-017 |

**SPI (consumed by Beneficiaries + Payments)**

- `ProgramaCatalogQuery.findByCodigo(CodPrograma) → Optional<ProgramaPolicy>` — REQ-013, REQ-014, REQ-018, REQ-025
- `ProgramaCatalogQuery.listAtivos() → List<ProgramaPolicy>` — REQ-014

---

## Payments & Folha

**REST**

| Method | Path | REQ |
| ------ | ---- | --- |
| POST | `/api/v1/folhas` | REQ-018..031 |
| GET | `/api/v1/pagamentos?cpf={cpf}&competencia={aaaamm}` | REQ-015 |
| POST | `/api/v1/conciliacoes` | REQ-032, REQ-033 |
| POST | `/api/v1/correcoes` | REQ-034 |
| GET | `/api/v1/relatorios/consolidado?competencia={aaaamm}` | REQ-035 |

**SPI (consumed by Beneficiaries)**

- `PagamentoHistoryQuery.findByCpf(Cpf, int limite) → List<PagamentoResumo>` — REQ-015

**Emits (events → Audit):** `PagamentoConciliado`, `PagamentoDevolvido`, `PagamentoEstornado`, `DivergenciaConciliacao` — REQ-036

---

## Audit & Compliance

**REST**

| Method | Path | REQ |
| ------ | ---- | --- |
| GET | `/api/v1/auditoria?de={data}&ate={data}&acao={acao}` | REQ-036, REQ-037 |

**SPI**

- `AuditoriaQuery.findByPeriodo(LocalDate, LocalDate, Optional<AcaoAuditoria>) → List<EventoAuditoria>` — REQ-037
- **Consumes** all domain events listed above (append-only; exclusions always visible).

---

## Contract test obligations (Stage 3)

- One OpenAPI contract test per REST operation (request/response schema conformance + status codes 201/204/400/404/409).
- One ArchUnit test asserting no module imports another context's `domain/`/`service/`/`repository/` (Principle IV).
- SPI interface tests verifying only DTO records (no JPA entities) cross boundaries.
