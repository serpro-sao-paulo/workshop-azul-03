package com.datacorp.app.beneficiaries.service;

import com.datacorp.app.beneficiaries.domain.*;
import com.datacorp.app.beneficiaries.repository.BeneficiarioRepository;
import com.datacorp.app.shared.exception.DomainException;
import com.datacorp.app.shared.kernel.Cpf;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Service for dependent lifecycle management.
 * REQ-009: max 5 dependents.
 * REQ-010: titular C/D blocks inclusion.
 * REQ-011: parentesco validation.
 * source_legacy: CADDEPEND.NSN#L54-L101
 */
@Service
@Transactional
public class DependenteService {

    private static final int MAX_DEPENDENTES = 5; // REQ-009

    private final BeneficiarioRepository beneficiarioRepository;

    public DependenteService(BeneficiarioRepository beneficiarioRepository) {
        this.beneficiarioRepository = beneficiarioRepository;
    }

    /**
     * Adds a dependent to a beneficiary.
     * REQ-009, REQ-010, REQ-011.
     * source_legacy: CADDEPEND.NSN#L54-L101
     */
    public Beneficiario adicionar(Long numInscricao, String nomeDependente,
                                   LocalDate dtNascimento, Parentesco parentesco,
                                   Cpf cpfDependente) {
        Beneficiario titular = beneficiarioRepository.findById(numInscricao)
                .orElseThrow(() -> new DomainException("BENEFICIARIO_NOT_FOUND",
                        "Beneficiário titular não encontrado"));

        // REQ-010: titular C/D blocks inclusion (CADDEPEND.NSN#L54-L57)
        if (titular.getStatus() == StatusBeneficiario.C
                || titular.getStatus() == StatusBeneficiario.D) {
            throw new DomainException("DEPENDENT_BLOCKED",
                    "Titular cancelado/desligado não permite novos dependentes");
        }

        // REQ-009: max 5 dependents (CADDEPEND.NSN#L61-L64)
        if (titular.numDependentes() >= MAX_DEPENDENTES) {
            throw new DomainException("DEPENDENT_LIMIT",
                    "Limite de " + MAX_DEPENDENTES + " dependentes atingido");
        }

        Dependente dep = Dependente.of(cpfDependente, nomeDependente, dtNascimento, parentesco,
                "A", "N");
        titular.addDependente(dep);
        return beneficiarioRepository.save(titular);
    }
}
