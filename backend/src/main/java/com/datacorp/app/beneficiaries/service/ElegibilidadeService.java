package com.datacorp.app.beneficiaries.service;

import com.datacorp.app.beneficiaries.domain.Beneficiario;
import com.datacorp.app.beneficiaries.domain.StatusBeneficiario;
import com.datacorp.app.beneficiaries.spi.ResultadoElegibilidade;
import com.datacorp.app.programs.spi.ProgramaCatalogQuery;
import com.datacorp.app.programs.spi.ProgramaPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Service for eligibility decisions (ACL consuming ProgramaCatalogQuery SPI).
 * REQ-012: status must be A.
 * REQ-013: income/age/type filters.
 * REQ-014: only active programs (ProgramaPolicy.ativo=true).
 * source_legacy: VALELEG.NSN#L74-L90
 */
@Service
@Transactional(readOnly = true)
public class ElegibilidadeService {

    private final ProgramaCatalogQuery programaCatalogQuery;

    public ElegibilidadeService(ProgramaCatalogQuery programaCatalogQuery) {
        this.programaCatalogQuery = programaCatalogQuery;
    }

    /**
     * Evaluates eligibility of a beneficiary for a program.
     * REQ-012..014.
     */
    public ResultadoElegibilidade avaliar(Beneficiario beneficiario, String codPrograma) {
        // REQ-012: status must be A
        if (beneficiario.getStatus() != StatusBeneficiario.A) {
            return ResultadoElegibilidade.inelegivel("Status do beneficiário não permite elegibilidade: "
                    + beneficiario.getStatus());
        }

        // REQ-014: program must exist and be active
        Optional<ProgramaPolicy> policyOpt = programaCatalogQuery.findActivePolicy(codPrograma);
        if (policyOpt.isEmpty()) {
            return ResultadoElegibilidade.inelegivel("Programa não encontrado ou inativo: " + codPrograma);
        }

        ProgramaPolicy policy = policyOpt.get();

        // REQ-013: income check (renda percap ≤ rendaMaxPercap, 0 = no restriction)
        if (policy.rendaMaxPercap() != null && policy.rendaMaxPercap().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rendaPercap = beneficiario.getIndRendaPercap();
            if (rendaPercap == null && beneficiario.getVlrRendaFamiliar() != null
                    && beneficiario.getQtdMembros() != null && beneficiario.getQtdMembros() > 0) {
                rendaPercap = beneficiario.getVlrRendaFamiliar().divide(
                        BigDecimal.valueOf(beneficiario.getQtdMembros()), 2, java.math.RoundingMode.DOWN);
            }
            if (rendaPercap != null && rendaPercap.compareTo(policy.rendaMaxPercap()) > 0) {
                return ResultadoElegibilidade.inelegivel(
                        "Renda per capita acima do limite do programa: " + rendaPercap);
            }
        }

        // REQ-013: age check
        int anoAtual = LocalDate.now().getYear();
        int idade = beneficiario.idadeNoAno(anoAtual);
        if (policy.idadeMin() != null && policy.idadeMin() > 0 && idade < policy.idadeMin()) {
            return ResultadoElegibilidade.inelegivel("Idade abaixo do mínimo: " + idade);
        }
        if (policy.idadeMax() != null && policy.idadeMax() > 0 && idade > policy.idadeMax()) {
            return ResultadoElegibilidade.inelegivel("Idade acima do máximo: " + idade);
        }

        return ResultadoElegibilidade.elegivel(policy.nomePrograma());
    }
}
