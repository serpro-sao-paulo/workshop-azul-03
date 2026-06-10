package com.datacorp.app.audit.service;

import com.datacorp.app.audit.domain.AcaoAuditoria;
import com.datacorp.app.audit.domain.EventoAuditoria;
import com.datacorp.app.audit.domain.TipoEntidade;
import com.datacorp.app.audit.repository.AuditoriaRepository;
import com.datacorp.app.beneficiaries.events.BeneficiarioExcluido;
import com.datacorp.app.beneficiaries.events.BeneficiarioStatusAlterado;
import com.datacorp.app.payments.events.DivergenciaConciliacao;
import com.datacorp.app.payments.events.PagamentoConciliado;
import com.datacorp.app.payments.events.PagamentoDevolvido;
import com.datacorp.app.payments.events.PagamentoEstornado;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Audit service — consumes domain events via Spring Modulith @ApplicationModuleListener.
 * ADR-004: decoupled from business transactions; separate transaction per event.
 * REQ-036: audit trail populated only by events, never by direct service calls.
 * source_legacy: N/A (ADR-004, IN-TCU 63)
 */
@Service
@Transactional
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    // ── Beneficiary events ────────────────────────────────────────────────────

    @ApplicationModuleListener
    public void on(BeneficiarioExcluido event) { // REQ-037
        auditoriaRepository.save(EventoAuditoria.registrar(
                event.eventId(), TipoEntidade.BENF, event.numInscricao(),
                AcaoAuditoria.EX, "Beneficiário excluído: " + event.motivo(),
                event.cpfMasked(), "SYSTEM", event.occurredOn()));
    }

    @ApplicationModuleListener
    public void on(BeneficiarioStatusAlterado event) { // REQ-036
        auditoriaRepository.save(EventoAuditoria.registrar(
                event.eventId(), TipoEntidade.BENF, event.numInscricao(),
                AcaoAuditoria.AL, "Status alterado: " + event.statusAnterior() + " → " + event.statusNovo(),
                event.cpfMasked(), "SYSTEM", event.occurredOn()));
    }

    // ── Payment events ────────────────────────────────────────────────────────

    @ApplicationModuleListener
    public void on(PagamentoConciliado event) { // REQ-036
        auditoriaRepository.save(EventoAuditoria.registrar(
                event.eventId(), TipoEntidade.PGTO, event.pagamentoId(),
                AcaoAuditoria.AL, "Pagamento conciliado competência " + event.competencia(),
                null, "BATCH", event.occurredOn()));
    }

    @ApplicationModuleListener
    public void on(PagamentoDevolvido event) { // REQ-036
        auditoriaRepository.save(EventoAuditoria.registrar(
                event.eventId(), TipoEntidade.PGTO, event.pagamentoId(),
                AcaoAuditoria.AL, "Pagamento devolvido: " + event.motivo(),
                null, "BATCH", event.occurredOn()));
    }

    @ApplicationModuleListener
    public void on(PagamentoEstornado event) { // REQ-036
        auditoriaRepository.save(EventoAuditoria.registrar(
                event.eventId(), TipoEntidade.PGTO, event.pagamentoId(),
                AcaoAuditoria.EX, "Pagamento estornado: " + event.motivo(),
                null, "SYSTEM", event.occurredOn()));
    }

    @ApplicationModuleListener
    public void on(DivergenciaConciliacao event) { // REQ-033
        auditoriaRepository.save(EventoAuditoria.registrar(
                event.eventId(), TipoEntidade.PGTO, event.pagamentoId(),
                AcaoAuditoria.AL, "Divergência: " + event.descricao(),
                null, "BATCH", event.occurredOn()));
    }
}
