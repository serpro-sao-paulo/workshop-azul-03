package com.datacorp.app.payments.events;

import com.datacorp.app.shared.events.DomainEvent;

/** REQ-036: payment returned by bank. source_legacy: BATCHCON.NSN cod-retorno '01' */
public final class PagamentoDevolvido extends DomainEvent {
    private final Long pagamentoId;
    private final String motivo;
    public PagamentoDevolvido(Long pagamentoId, String motivo) {
        super();
        this.pagamentoId = pagamentoId;
        this.motivo = motivo;
    }
    public Long pagamentoId() { return pagamentoId; }
    public String motivo() { return motivo; }
}
