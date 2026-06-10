package com.datacorp.app.beneficiaries.domain;

/**
 * Kinship type of a dependent.
 * Source: BENEFICIARIO.ddm field DE PARENTESCO A 2.
 * DDM comment: FI=FILHO CJ=CONJ NT=NETO TU=TUTEL.
 *
 * <p>Note: CADDEPEND.NSN rule #5 (business-rules-catalog.md, Confirmed) validates
 * parentesco against the set FI/CO/IR/OU. The DDM uses different codes (CJ/NT/TU vs CO/IR/OU).
 * This discrepancy is a known legacy de-para — CADDEPEND uses shorter codes in its input
 * while the DDM stores expanded codes.
 * FIXME: confirm the canonical set with data samples and CADDEPEND source
 * (CADDEPEND.NSN#L83-L88 vs DDM comment). The enum here uses CADDEPEND codes (confirmed in code).
 */
public enum Parentesco {
    /** FI — Filho/a. */
    FI,
    /** CO — Cônjuge (CADDEPEND code; DDM uses CJ). */
    CO,
    /** IR — Irmão/irmã (CADDEPEND code; DDM uses NT for Neto — FIXME: reconcile). */
    IR,
    /** OU — Outro (CADDEPEND code; DDM uses TU for Tutelado — FIXME: reconcile). */
    OU
}
