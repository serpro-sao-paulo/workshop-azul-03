package com.datacorp.app.payments.domain;

/**
 * Canonical payment status machine for PAGAMENTO (FNR 152).
 *
 * <p>ADR-002: resolves the legacy SIT-PAGAMENTO collision where 'E' meant both
 * "error" (BATCHCON cod-retorno '02') and "estornado" (BATCHREL label), and 'P'
 * meant both "pending" (DDM) and "paid" (BATCHCON cod-retorno '00').
 * The de-para from legacy single-char codes to this enum is versioned in
 * {@code db/migration/notes/status-depara.md} and must be validated by
 * SENARC/CGPB before any data migration runs (OQ-01).
 *
 * <p>Valid transitions (all others are illegal):
 * <pre>
 *   GERADO    → EMITIDO | CANCELADO
 *   EMITIDO   → PAGO | DEVOLVIDO | ERRO
 *   PAGO      → ESTORNADO
 *   DEVOLVIDO → (terminal)
 *   ERRO      → (terminal)
 *   CANCELADO → (terminal)
 *   ESTORNADO → (terminal)
 * </pre>
 *
 * @see <a href="../../../../../../../../02-spec-moderna/ADRs/adr-002-maquina-de-status-unica-do-pagamento.md">ADR-002</a>
 */
public enum StatusPagamento {

    /** Folha gerada pelo BATCHPGT / FolhaService. Legacy code: 'G'. REQ-029. */
    GERADO,

    /** Remessa enviada ao banco. Legacy DDM: 'E' (EMITIDO). */
    EMITIDO,

    /**
     * Conciliado com código de retorno bancário '00' (banco confirmou crédito).
     * REQ-032. Legacy collision resolved: was 'P' (PAGO) in BATCHCON / 'P' (PEND) in DDM.
     */
    PAGO,

    /** Conciliado com código de retorno '01' (devolvido pelo banco). REQ-032. */
    DEVOLVIDO,

    /**
     * Conciliado com código de retorno '02' (erro bancário). REQ-032.
     * Legacy collision resolved: was 'E' = error in BATCHCON, same char as ESTORNADO in reports.
     */
    ERRO,

    /**
     * Pagamento cancelado. Legacy code: 'C' (orphan — no legacy program writes this;
     * source unknown, requires stakeholder confirmation, OQ-01).
     */
    CANCELADO,

    /**
     * Pagamento estornado após ter sido pago. REQ-032.
     * Legacy collision resolved: was 'E' = estornado in BATCHREL/RELPGT — now distinct from ERRO.
     */
    ESTORNADO;

    /**
     * Returns true if this is a terminal state (no further transitions allowed).
     */
    public boolean isTerminal() {
        return this == PAGO || this == DEVOLVIDO || this == ERRO
            || this == CANCELADO || this == ESTORNADO;
    }

    /**
     * Validates that {@code next} is a legal transition from {@code this}.
     * @throws IllegalStateException if the transition is not allowed.
     */
    public void validateTransitionTo(StatusPagamento next) {
        boolean allowed = switch (this) {
            case GERADO    -> next == EMITIDO   || next == CANCELADO;
            case EMITIDO   -> next == PAGO      || next == DEVOLVIDO || next == ERRO;
            case PAGO      -> next == ESTORNADO;
            default        -> false; // terminal states allow no transition
        };
        if (!allowed) {
            throw new IllegalStateException(
                "Invalid StatusPagamento transition: %s → %s".formatted(this, next));
        }
    }
}
