package com.datacorp.app.payments.events;

import com.datacorp.app.shared.events.DomainEvent;

/** REQ-036: payment reversed (estornado). source_legacy: BATCHREL report (ADR-002) */
public final class PagamentoEstornado extends DomainEvent {
    private final Long pagamentoId;
    private final String motivo;
    public PagamentoEstornado(Long pagamentoId, String motivo) {
        super();
        this.pagamentoId = pagamentoId;
        this.motivo = motivo;
    }
    public Long pagamentoId() { return pagamentoId; }
    public String motivo() { return motivo; }
}
