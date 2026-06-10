package com.datacorp.app.payments.service;

import com.datacorp.app.payments.domain.Pagamento;
import com.datacorp.app.payments.repository.PagamentoRepository;
import com.datacorp.app.payments.spi.PagamentoHistoryQuery;
import com.datacorp.app.payments.spi.PagamentoResumo;
import com.datacorp.app.shared.kernel.Competencia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implements {@link PagamentoHistoryQuery} SPI.
 */
@Service
@Transactional(readOnly = true)
class PagamentoHistoryQueryImpl implements PagamentoHistoryQuery {

    private final PagamentoRepository repo;

    PagamentoHistoryQueryImpl(PagamentoRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<PagamentoResumo> findByBeneficiario(Long numInscricao) {
        return repo.findByNumInscricaoOrderByCompetenciaDesc(numInscricao)
                .stream().map(this::toResumo).toList();
    }

    @Override
    public List<PagamentoResumo> findByCompetencia(int competencia) {
        return repo.findByCodProgramaValueAndCompetencia("PBF", Competencia.of(competencia))
                .stream().map(this::toResumo).toList();
    }

    private PagamentoResumo toResumo(Pagamento p) {
        return new PagamentoResumo(
                p.getNumPagamento(),
                p.getCpf() != null ? p.getCpf().masked() : null, // REQ-015
                p.getCodPrograma() != null ? p.getCodPrograma().value() : null,
                p.getCompetencia() != null ? p.getCompetencia().toInt() : 0,
                p.getStatus(),
                p.getVlrBruto(),
                p.getVlrLiquido(),
                p.getVlrDescontoTotal()
        );
    }
}
