package com.datacorp.app.programs.service;

import com.datacorp.app.programs.domain.ProgramaSocial;
import com.datacorp.app.programs.domain.SituacaoPrograma;
import com.datacorp.app.programs.domain.TipoPrograma;
import com.datacorp.app.programs.repository.ProgramaRepository;
import com.datacorp.app.programs.spi.ProgramaCatalogQuery;
import com.datacorp.app.programs.spi.ProgramaPolicy;
import com.datacorp.app.shared.kernel.CodPrograma;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implements {@link ProgramaCatalogQuery} SPI — the only Programs data accessible
 * to other modules. Maps domain entities to the public {@link ProgramaPolicy} record.
 * source_legacy: CONSPROG.NSN, BATCHPGT.NSN (REQ-013, REQ-014, REQ-018, REQ-025)
 */
@Service
@Transactional(readOnly = true)
class ProgramaCatalogQueryImpl implements ProgramaCatalogQuery {

    private final ProgramaRepository repo;

    ProgramaCatalogQueryImpl(ProgramaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<ProgramaPolicy> findActivePolicy(String codPrograma) {
        return repo.findById(CodPrograma.of(codPrograma))
                .filter(p -> p.getSituacao() == SituacaoPrograma.A)
                .map(this::toPolicy);
    }

    @Override
    public List<ProgramaPolicy> findActivePoliciesByType(TipoPrograma tipoPrograma) {
        return repo.findByTipoProgramaAndSituacao(tipoPrograma, SituacaoPrograma.A)
                .stream()
                .map(this::toPolicy)
                .toList();
    }

    private ProgramaPolicy toPolicy(ProgramaSocial p) {
        return new ProgramaPolicy(
                p.getCodigo().value(),
                p.getNomePrograma(),
                p.getTipoPrograma(),
                p.isAtivo(),
                p.getVlrBase(),
                p.getFatorK() != null ? p.getFatorK() : BigDecimal.ZERO,
                p.getVlrTetoBenef() != null ? p.getVlrTetoBenef() : BigDecimal.ZERO,
                p.getVlrPisoBenef() != null ? p.getVlrPisoBenef() : BigDecimal.ZERO,
                p.getRendaMaxPercap() != null ? p.getRendaMaxPercap() : BigDecimal.ZERO,
                p.getIdadeMin() != null ? p.getIdadeMin() : 0,
                p.getIdadeMax() != null ? p.getIdadeMax() : 0,
                p.getDescontosAplicaveis()
        );
    }
}
