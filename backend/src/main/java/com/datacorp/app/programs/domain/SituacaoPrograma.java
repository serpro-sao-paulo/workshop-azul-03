package com.datacorp.app.programs.domain;

/**
 * Operational status of a social program.
 * Source: PROGRAMA-SOCIAL.ddm field AI SIT-PROGRAMA A 1.
 *
 * <p>VALELEG.NSN rule #3 (REQ eligibility): only programs with status {@link #A}
 * are considered for eligibility (VALELEG.NSN#L100-L103). Inactive or
 * encerrado programs cause rejection.
 *
 * <p>BATCHPGT.NSN rule #7: batch also skips beneficiaries whose program is not {@link #A}.
 */
public enum SituacaoPrograma {

    /** A — Ativo. Only active programs are processed for calculation and eligibility. */
    A,

    /** I — Inativo. Program temporarily suspended; no new payments. */
    I,

    /** E — Encerrado. Program definitively closed. */
    E;

    /** Returns true if this status permits eligibility checks and payment generation. */
    public boolean permitsProcessing() {
        return this == A;
    }
}
