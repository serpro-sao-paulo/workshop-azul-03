# Status Pagamento — Legacy De-Para

> STATUS: Pending stakeholder sign-off (OQ-01 — needs SENARC/CGPB validation)
>
> This file documents the proposed mapping from legacy single-char codes
> to the canonical `StatusPagamento` enum (ADR-002).
>
> **DO NOT run data migration until this mapping is confirmed by SENARC/CGPB.**

## Proposed Mapping

| Legacy Code | Legacy Meaning (conflicted) | Canonical Enum | Notes |
|-------------|----------------------------|----------------|-------|
| `G` | Gerado (BATCHPGT) | `GERADO` | Clear |
| `E` | **COLLISION**: Emitido (DDM) OR Erro (BATCHCON '02') | REQUIRES DECISION | See ADR-002 §Collision |
| `P` | **COLLISION**: Pendente (DDM) OR Pago (BATCHCON '00') | REQUIRES DECISION | See ADR-002 §Collision |
| `C` | Cancelado (orphan — no program writes this) | `CANCELADO` | Verify with stakeholders |
| `D` | Devolvido (BATCHCON '01') | `DEVOLVIDO` | Clear |
| `R` | Estornado (BATCHREL labels) | `ESTORNADO` | Rename: R→ESTORNADO |

## Resolution Required (OQ-01)

The ambiguous codes `E` and `P` must be resolved by reviewing all rows in the
legacy PAGAMENTO file before migration:

1. `E` rows at `DT-EMISSAO` = 0 → likely `ERRO`
2. `E` rows at `DT-EMISSAO` > 0 AND `DT-CONFIRMACAO` = 0 → likely `EMITIDO`
3. `P` rows at `DT-CONFIRMACAO` > 0 → likely `PAGO`
4. `P` rows at `DT-CONFIRMACAO` = 0 → likely orphan / data quality issue

## TODO

- [ ] OQ-01: Get SENARC/CGPB to confirm row-level disambiguation logic
- [ ] OQ-01: Run count query on PAGAMENTO per status code to assess volume
- [ ] OQ-01: Confirm `R` → `ESTORNADO` mapping (or verify actual char code used)
