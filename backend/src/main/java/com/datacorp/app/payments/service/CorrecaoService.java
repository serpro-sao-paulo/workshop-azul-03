package com.datacorp.app.payments.service;

import com.datacorp.app.payments.domain.Pagamento;
import com.datacorp.app.payments.events.PagamentoEstornado;
import com.datacorp.app.payments.repository.PagamentoRepository;
import com.datacorp.app.shared.exception.DomainException;
import com.datacorp.app.shared.kernel.Money;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Service for idempotent monetary correction (IPCA).
 * REQ-034: apply correction once, idempotent. source_legacy: BATCHREL.NSN (correção field)
 */
@Service
@Transactional
public class CorrecaoService {

    private final PagamentoRepository pagamentoRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CorrecaoService(PagamentoRepository pagamentoRepository,
                            ApplicationEventPublisher eventPublisher) {
        this.pagamentoRepository = pagamentoRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Applies IPCA correction to a PAGO payment. Idempotent (REQ-034).
     */
    public void aplicarCorrecao(Long pagamentoId, BigDecimal indiceIPCA) {
        Pagamento p = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new DomainException("PAGAMENTO_NOT_FOUND",
                        "Pagamento não encontrado: " + pagamentoId));

        // Idempotent: skip if already corrected (REQ-034)
        if (p.isIndCorrigido()) return;

        Money vlrCorrecao = Money.of(p.getVlrLiquido()).multiply(indiceIPCA);
        p.applyCorrecao(vlrCorrecao.amount(), LocalDate.now());
        pagamentoRepository.save(p);
    }

    /**
     * Estorna a PAGO payment. REQ-036: publishes PagamentoEstornado event.
     */
    public void estornar(Long pagamentoId, String motivo) {
        Pagamento p = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new DomainException("PAGAMENTO_NOT_FOUND",
                        "Pagamento não encontrado: " + pagamentoId));

        p.transitionTo(com.datacorp.app.payments.domain.StatusPagamento.ESTORNADO);
        pagamentoRepository.save(p);
        eventPublisher.publishEvent(new PagamentoEstornado(pagamentoId, motivo));
    }
}
