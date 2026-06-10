package com.datacorp.app.beneficiaries.domain;

/**
 * Status of a beneficiary in the SIFAP system.
 * Source: BENEFICIARIO.ddm field CE SIT-BENEFICIARIO A 1.
 *
 * <p>Confirmed from VALBENEF.NSN rule #6 (SPECIFICATION.md REQ-007) and
 * CONSBENF.NSN rule #3 (display translation A=ATIVO..D=DESLIGADO).
 *
 * <p>Valid transitions (REQ-006/007/008):
 * <ul>
 *   <li>Initial state on inclusion: {@link #A} (REQ-006)</li>
 *   <li>Auto-transition: age > 75 → {@link #S} at registration time (REQ-008)</li>
 *   <li>Allowed by CADBENEF: any → any (constrained by operator profile — see REQ-007 note)</li>
 * </ul>
 */
public enum StatusBeneficiario {

    /** A — Ativo. Initial status on inclusion (REQ-006). Eligible for payments. */
    A,

    /**
     * S — Suspenso. Auto-assigned when age > 75 (REQ-008).
     * Marks ineligibility per VALELEG.NSN rule #5 (REQ-012).
     * ⚠️ Business intent to be confirmed with stakeholder (OQ-08):
     * is suspension at 75 a mandatory review or a legacy bug?
     */
    S,

    /** C — Cancelado. Terminal for dependents (REQ-010). Ineligible (REQ-012). */
    C,

    /** I — Inativo. Ineligible per VALELEG.NSN rule #7 (REQ-012). */
    I,

    /** D — Desligado. Terminal/discharged. Ineligible per VALELEG.NSN rule #6 (REQ-012). */
    D;

    /** Returns true if this status prevents receiving new dependents (REQ-010). */
    public boolean blocksNewDependents() {
        return this == C || this == D;
    }

    /** Returns true if this status directly causes ineligibility for a program (REQ-012). */
    public boolean causesIneligibility() {
        return this != A;
    }
}
