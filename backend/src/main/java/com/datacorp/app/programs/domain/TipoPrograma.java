package com.datacorp.app.programs.domain;

/**
 * Type of social program.
 * Source: PROGRAMA-SOCIAL.ddm field AD TIPO-PROGRAMA A 1.
 * DDM comment: A=ASSISTENC T=TRABALHO P=PREVID.
 *
 * <p>Drives eligibility evaluation (VALELEG.NSN rules #11/12/13 — REQ-014):
 * <ul>
 *   <li>{@link #A} (Assistencial): income ≤ 600 or with dependents; documentation required.</li>
 *   <li>{@link #P} (Previdenciário): age ≥ 60 required.</li>
 *   <li>{@link #T} (Trabalho): age between 16 and 65.</li>
 * </ul>
 *
 * <p>Also drives the Christmas allowance (abono natalino):
 * only type {@link #A} programs trigger the 15% abono in December (REQ-023).
 */
public enum TipoPrograma {

    /** A — Assistencial. Income/dependent/documentation-based eligibility. Abono natalino eligible. */
    A,

    /** P — Previdenciário. Age ≥ 60 required (REQ-014). */
    P,

    /** T — Trabalho. Age 16–65 required (REQ-014). */
    T;
}
