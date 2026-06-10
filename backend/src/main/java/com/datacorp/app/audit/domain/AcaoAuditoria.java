package com.datacorp.app.audit.domain;

/**
 * Audit action vocabulary. Source: AUDITORIA.ddm field BA COD-ACAO A 2.
 *
 * <p>Confirmed values from RELAUDIT.NSN (business-rules-catalog.md, RELAUDIT rules):
 * IN=inclusão, AL=alteração, EX=exclusão, CO=consulta, LG=login, LO=logout,
 * BT=batch, ER=erro, AU=autorização, RE=rejeição.
 *
 * <p>REQ-037: action 'EX' (exclusion) MUST be visible on query — the legacy
 * RELAUDIT.NSN deliberately hid EX rows ("FILTRO ACAO - EXCLUSOES NAO SAO
 * EXIBIDAS"). This system removes that filter; all actions, including EX, are
 * always returned (see AuditoriaService and AuditoriaController). ADR-004.
 *
 * <p>Note: actions 'CO' (consulta) are NOT recorded since 2010 per
 * CGTI decision Port. 213/2010 (noted in AUDITORIA.ddm). That policy is
 * preserved — CO events are dropped by the publisher, not suppressed on query.
 */
public enum AcaoAuditoria {

    /** IN — record inclusion / creation. */
    IN,

    /** AL — record alteration / update. */
    AL,

    /**
     * EX — record exclusion (deletion).
     * REQ-037: ALWAYS visible. Legacy hid these rows; we do not.
     */
    EX,

    /** CO — query/consultation. Not recorded since 2010 (CGTI Port. 213/2010). */
    CO,

    /** LG — user login. */
    LG,

    /** LO — user logout. */
    LO,

    /**
     * BT — batch operation.
     * When present, the batch-context fields (numCicloBatch etc.) are populated.
     */
    BT,

    /** ER — error event. */
    ER,

    /** AU — authorization decision. */
    AU,

    /** RE — rejection / access denied. */
    RE;
}
