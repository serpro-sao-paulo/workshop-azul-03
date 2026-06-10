package com.datacorp.app.beneficiaries.events;

import com.datacorp.app.shared.events.DomainEvent;

/**
 * Fired when a beneficiary is excluded (status → D or C).
 * Consumed by AuditoriaService (ADR-004, REQ-037).
 * source_legacy: CADBENEF.NSN exclusion path
 */
public final class BeneficiarioExcluido extends DomainEvent {
    private final Long numInscricao;
    private final String cpfMasked; // REQ-015: masked
    private final String motivo;

    public BeneficiarioExcluido(Long numInscricao, String cpfMasked, String motivo) {
        super();
        this.numInscricao = numInscricao;
        this.cpfMasked = cpfMasked;
        this.motivo = motivo;
    }

    public Long numInscricao() { return numInscricao; }
    public String cpfMasked() { return cpfMasked; }
    public String motivo() { return motivo; }
}
