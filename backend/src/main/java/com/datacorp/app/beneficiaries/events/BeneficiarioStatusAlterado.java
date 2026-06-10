package com.datacorp.app.beneficiaries.events;

import com.datacorp.app.beneficiaries.domain.StatusBeneficiario;
import com.datacorp.app.shared.events.DomainEvent;

/**
 * Fired when beneficiary status changes.
 * Consumed by AuditoriaService (ADR-004, REQ-037).
 * source_legacy: CADBENEF.NSN, VALBENEF.NSN status-change paths
 */
public final class BeneficiarioStatusAlterado extends DomainEvent {
    private final Long numInscricao;
    private final String cpfMasked;
    private final StatusBeneficiario statusAnterior;
    private final StatusBeneficiario statusNovo;
    private final String motivo;

    public BeneficiarioStatusAlterado(Long numInscricao, String cpfMasked,
                                       StatusBeneficiario statusAnterior,
                                       StatusBeneficiario statusNovo,
                                       String motivo) {
        super();
        this.numInscricao = numInscricao;
        this.cpfMasked = cpfMasked;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.motivo = motivo;
    }

    public Long numInscricao() { return numInscricao; }
    public String cpfMasked() { return cpfMasked; }
    public StatusBeneficiario statusAnterior() { return statusAnterior; }
    public StatusBeneficiario statusNovo() { return statusNovo; }
    public String motivo() { return motivo; }
}
