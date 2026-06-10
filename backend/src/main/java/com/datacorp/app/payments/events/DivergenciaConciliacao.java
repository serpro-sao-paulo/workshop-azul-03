package com.datacorp.app.payments.events;

import com.datacorp.app.shared.events.DomainEvent;

/** REQ-033: divergence detected during reconciliation. source_legacy: BATCHCON.NSN divergence path */
public final class DivergenciaConciliacao extends DomainEvent {
    private final Long pagamentoId;
    private final String descricao;
    public DivergenciaConciliacao(Long pagamentoId, String descricao) {
        super();
        this.pagamentoId = pagamentoId;
        this.descricao = descricao;
    }
    public Long pagamentoId() { return pagamentoId; }
    public String descricao() { return descricao; }
}
