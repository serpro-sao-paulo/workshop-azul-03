package com.datacorp.app.payments.service;

import com.datacorp.app.payments.domain.Pagamento;
import com.datacorp.app.payments.domain.StatusPagamento;
import com.datacorp.app.payments.events.DivergenciaConciliacao;
import com.datacorp.app.payments.events.PagamentoConciliado;
import com.datacorp.app.payments.events.PagamentoDevolvido;
import com.datacorp.app.payments.repository.PagamentoRepository;
import com.datacorp.app.shared.exception.DomainException;
import com.datacorp.app.shared.kernel.Competencia;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for CNAB 240 bank reconciliation.
 *
 * <p>REQ-032: process CNAB 240 return file — status P/D/E per bank return code.
 * <p>REQ-033: detect and publish divergence events.
 * source_legacy: BATCHCON.NSN
 */
@Service
@Transactional
public class ConciliacaoService {

    private final PagamentoRepository pagamentoRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ConciliacaoService(PagamentoRepository pagamentoRepository,
                               ApplicationEventPublisher eventPublisher) {
        this.pagamentoRepository = pagamentoRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Processes a single CNAB return entry.
     * REQ-032: cod-retorno '00'→PAGO, '01'→DEVOLVIDO, '02'→ERRO.
     * REQ-033: publishes events for audit trail (ADR-004).
     */
    public void processarRetorno(Long pagamentoId, String codRetorno) {
        Pagamento p = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new DomainException("PAGAMENTO_NOT_FOUND",
                        "Pagamento não encontrado: " + pagamentoId));

        switch (codRetorno) {
            case "00" -> {
                // REQ-032: PAGO
                p.transitionTo(StatusPagamento.PAGO);
                pagamentoRepository.save(p);
                eventPublisher.publishEvent(new PagamentoConciliado(pagamentoId,
                        p.getCompetencia().toString()));
            }
            case "01" -> {
                // REQ-032: DEVOLVIDO
                p.transitionTo(StatusPagamento.DEVOLVIDO);
                pagamentoRepository.save(p);
                eventPublisher.publishEvent(new PagamentoDevolvido(pagamentoId,
                        "Banco devolveu: cod-retorno 01"));
            }
            case "02" -> {
                // REQ-032: ERRO (not ESTORNADO — ADR-002 collision resolved)
                p.transitionTo(StatusPagamento.ERRO);
                pagamentoRepository.save(p);
                // REQ-033: divergence event
                eventPublisher.publishEvent(new DivergenciaConciliacao(pagamentoId,
                        "Erro bancário: cod-retorno 02"));
            }
            default -> {
                // REQ-033: unknown return code = divergence
                eventPublisher.publishEvent(new DivergenciaConciliacao(pagamentoId,
                        "Código de retorno desconhecido: " + codRetorno));
            }
        }
    }
}
