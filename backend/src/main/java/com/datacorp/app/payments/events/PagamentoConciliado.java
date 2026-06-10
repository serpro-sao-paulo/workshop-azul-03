package com.datacorp.app.payments.events;

import com.datacorp.app.shared.events.DomainEvent;

/** REQ-036: payment successfully reconciled. source_legacy: BATCHCON.NSN cod-retorno '00' */
public final class PagamentoConciliado extends DomainEvent {
    private final Long pagamentoId;
    private final String competencia;
    public PagamentoConciliado(Long pagamentoId, String competencia) {
        super();
        this.pagamentoId = pagamentoId;
        this.competencia = competencia;
    }
    public Long pagamentoId() { return pagamentoId; }
    public String competencia() { return competencia; }
}
