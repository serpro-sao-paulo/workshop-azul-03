package com.datacorp.app.beneficiaries.service;

import com.datacorp.app.beneficiaries.domain.*;
import com.datacorp.app.beneficiaries.events.BeneficiarioStatusAlterado;
import com.datacorp.app.beneficiaries.repository.BeneficiarioRepository;
import com.datacorp.app.shared.exception.DomainException;
import com.datacorp.app.shared.exception.ValidationException;
import com.datacorp.app.shared.kernel.Cpf;
import com.datacorp.app.shared.kernel.CodPrograma;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for beneficiary registration and lifecycle management.
 *
 * <p>REQ-001..008: validation, initial status, auto-suspension.
 * <p>REQ-002: accumulative validation (non-short-circuit, VALBENEF pattern).
 * <p>Principle III: all tests for this class must be written first (failing).
 * source_legacy: CADBENEF.NSN, VALBENEF.NSN
 */
@Service
@Transactional
public class CadastroService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CadastroService(BeneficiarioRepository beneficiarioRepository,
                            ApplicationEventPublisher eventPublisher) {
        this.beneficiarioRepository = beneficiarioRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Registers a new beneficiary with accumulative validation.
     * REQ-001..008. source_legacy: CADBENEF.NSN#L103-L167.
     */
    public Beneficiario incluir(String cpfStr, String nomeCompleto, String nomeMae,
                                 LocalDate dtNascimento, Sexo sexo, EstadoCivil estCivil,
                                 Endereco endereco, String codProgramaStr,
                                 BigDecimal vlrRenda, Integer qtdMembros) {
        // REQ-002: accumulate ALL errors, do not short-circuit
        List<String> errors = new ArrayList<>();

        // REQ-001: CPF validation
        Cpf cpf = null;
        if (cpfStr == null || cpfStr.isBlank()) {
            errors.add("CPF obrigatório");
        } else {
            try {
                cpf = Cpf.of(cpfStr);
            } catch (IllegalArgumentException | NullPointerException e) {
                errors.add("CPF inválido: " + e.getMessage());
            }
        }

        // REQ-004: nome obrigatório
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            errors.add("Nome obrigatório");
        } else if (!nomeCompleto.contains(" ")) {
            // REQ-002: nome deve conter sobrenome (VALBENEF rule)
            errors.add("Nome deve conter sobrenome");
        }

        // REQ-004: data de nascimento obrigatória
        if (dtNascimento == null) {
            errors.add("Data de nascimento obrigatória");
        }

        // Validate UF (REQ-002)
        if (endereco != null && (endereco.getUf() == null || endereco.getUf().isBlank())) {
            errors.add("UF obrigatória");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        // REQ-005: CPF uniqueness
        assert cpf != null;
        if (beneficiarioRepository.existsByCpfValue(cpf.value())) {
            throw new DomainException("BENEFICIARIO_CONFLICT",
                    "Beneficiário já cadastrado com este CPF");
        }

        CodPrograma codPrograma = codProgramaStr != null ? CodPrograma.of(codProgramaStr) : null;

        Beneficiario b = Beneficiario.criar(cpf, nomeCompleto, nomeMae, dtNascimento,
                sexo, estCivil, endereco, codPrograma, vlrRenda, qtdMembros);

        // REQ-008: auto-suspension if age > 75
        b.applyAutoSuspensionIfElderly(LocalDate.now().getYear());

        return beneficiarioRepository.save(b);
    }

    /**
     * Alters beneficiary status with event publication.
     * REQ-007, REQ-037 (audit via domain event, ADR-004).
     */
    public Beneficiario alterarStatus(Long numInscricao, StatusBeneficiario novoStatus, String motivo) {
        Beneficiario b = beneficiarioRepository.findById(numInscricao)
                .orElseThrow(() -> new DomainException("BENEFICIARIO_NOT_FOUND",
                        "Beneficiário não encontrado: " + numInscricao));

        StatusBeneficiario anterior = b.getStatus();
        b.alterarStatus(novoStatus, motivo);
        Beneficiario saved = beneficiarioRepository.save(b);

        // REQ-037: publish domain event for audit (ADR-004)
        eventPublisher.publishEvent(new BeneficiarioStatusAlterado(
                numInscricao, b.getCpf().masked(), anterior, novoStatus, motivo));

        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Beneficiario> buscar(Long numInscricao) {
        return beneficiarioRepository.findById(numInscricao);
    }

    @Transactional(readOnly = true)
    public Optional<Beneficiario> buscarPorCpf(String cpfStr) {
        Cpf cpf = Cpf.of(cpfStr);
        return beneficiarioRepository.findByCpfValue(cpf.value());
    }
}
