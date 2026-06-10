package com.datacorp.app.programs.domain;

/**
 * Applicable discount types for a social program.
 * Source: PROGRAMA-SOCIAL.ddm field EA TIPO-DSCT-APLIC MU A 3 (max 8 occurrences).
 *
 * <p>ADR-001: this MU field is a controlled vocabulary of low cardinality over a
 * reference table (~45 rows) — mapped as {@code @ElementCollection} of this enum,
 * NOT as JSONB. This enables consultable, strongly-typed membership checks required
 * by REQ-025/026/027 (contribution by type, 30% cap, judicial exemption).
 *
 * <p>DDM comment values: IR=IRRF JD=JUDICIAL CS=CONSIGNADO PA=PENS.ALIM
 *   EM=EMPRESTIMO TX=TAXA OU=OUTROS EX=EXTRAORDINARIO.
 *
 * <p>Mapping note: the legacy CALCDSCT.NSN uses different 1-char codes (C/I/J/S/P/A).
 * The de-para is documented in dependency-map.md §Discrepâncias críticas DDM × código.
 * This enum uses the canonical DDM 2-3 char codes; ConciliacaoService/DescontoService
 * must translate from CALCDSCT codes via a static mapping table.
 */
public enum TipoDesconto {

    /** IR — Imposto de Renda Retido na Fonte (CALCDSCT code: 'I'). */
    IR,

    /**
     * JD — Desconto Judicial (CALCDSCT code: 'J').
     * Exempt from the 30% cap (REQ-026). Requires NUM-PROCESSO (REQ-027).
     */
    JD,

    /** CS — Consignado (CALCDSCT code: 'C'). */
    CS,

    /** PA — Pensão Alimentícia (CALCDSCT code: 'P'). Subject to 30% cap (REQ-026). */
    PA,

    /** EM — Empréstimo. */
    EM,

    /** TX — Taxa. */
    TX,

    /** OU — Outros (CALCDSCT code: 'A' for 'administrativo'). */
    OU,

    /** EX — Extraordinário. */
    EX;

    /**
     * Returns true if this discount type is exempt from the 30% cap (REQ-026).
     * Only judicial discounts are exempt (CALCDSCT.NSN#L138 — "IF #TIPO-DSCT NE 'J'").
     */
    public boolean isExentoDeTeto() {
        return this == JD;
    }
}
