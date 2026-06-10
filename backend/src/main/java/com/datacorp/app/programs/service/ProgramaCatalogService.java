package com.datacorp.app.programs.service;

import com.datacorp.app.programs.domain.ProgramaSocial;
import com.datacorp.app.programs.domain.SituacaoPrograma;
import com.datacorp.app.programs.repository.ProgramaRepository;
import com.datacorp.app.shared.exception.DomainException;
import com.datacorp.app.shared.kernel.CodPrograma;
import com.datacorp.app.shared.kernel.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for the Catálogo de Programas Sociais (Bounded Context: Programs).
 *
 * <p>REQ-016: unicidade do código de programa.
 * <p>REQ-017: valor base ajustado pelo Fator-K na inclusão:
 *   {@code vlrBaseAjustado = vlrBase × (1 + fatorK)}, truncated.
 * <p>OQ-04: constant 0.347215 origin unresolved — Fator-K passed by caller (from DDM BG field).
 * source_legacy: CADPROG.NSN#L85-L100
 */
@Service
@Transactional
public class ProgramaCatalogService {

    private final ProgramaRepository programaRepository;

    public ProgramaCatalogService(ProgramaRepository programaRepository) {
        this.programaRepository = programaRepository;
    }

    /**
     * Includes a new programa social, applying Fator-K adjustment to vlrBase.
     *
     * @throws DomainException PROGRAMA_CONFLICT if codPrograma already exists (REQ-016)
     */
    public ProgramaSocial incluir(IncluirProgramaCommand cmd) {
        if (programaRepository.findById(cmd.codPrograma()).isPresent()) {
            throw new DomainException("PROGRAMA_CONFLICT",
                    "Programa já cadastrado: " + cmd.codPrograma().value());
        }

        // REQ-017: vlrBaseAjustado = vlrBase × (1 + fatorK), truncated (REQ-021)
        // TODO: OQ-04 pending stakeholder decision — fatorK semantics unresolved
        BigDecimal fatorK = cmd.fatorK() != null ? cmd.fatorK() : BigDecimal.ZERO;
        Money vlrBaseAjustado = cmd.vlrBase()
                .multiply(BigDecimal.ONE.add(fatorK));

        ProgramaSocial programa = new ProgramaSocial(
                cmd.codPrograma(),
                cmd.nomePrograma(),
                cmd.siglaPrograma(),
                cmd.tipoPrograma(),
                cmd.sitPrograma(),
                vlrBaseAjustado.amount(),
                cmd.vlrMaximo().amount(),
                cmd.vlrMinimo().amount(),
                fatorK,
                cmd.tiposDesconto(),
                LocalDate.now()
        );

        return programaRepository.save(programa);
    }

    @Transactional(readOnly = true)
    public Optional<ProgramaSocial> buscarPorCodigo(CodPrograma cod) {
        return programaRepository.findById(cod);
    }

    @Transactional(readOnly = true)
    public List<ProgramaSocial> listarAtivos() {
        return programaRepository.findBySituacao(SituacaoPrograma.A);
    }
}
