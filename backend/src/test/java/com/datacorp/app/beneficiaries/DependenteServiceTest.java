package com.datacorp.app.beneficiaries;

import com.datacorp.app.beneficiaries.domain.*;
import com.datacorp.app.beneficiaries.service.DependenteService;
import com.datacorp.app.beneficiaries.repository.BeneficiarioRepository;
import com.datacorp.app.shared.exception.DomainException;
import com.datacorp.app.shared.kernel.Cpf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for dependent rules.
 * REQ-009: limit of 5 dependents.
 * REQ-010: titular C/D blocks dependent inclusion.
 * REQ-011: parentesco validation.
 * source_legacy: CADDEPEND.NSN#L54-L64
 */
@ExtendWith(MockitoExtension.class)
class DependenteServiceTest {

    @Mock
    private BeneficiarioRepository beneficiarioRepository;

    @InjectMocks
    private DependenteService dependenteService;

    private Beneficiario beneficiarioAtivo;
    private Beneficiario beneficiarioCancelado;

    @BeforeEach
    void setup() {
        beneficiarioAtivo = Beneficiario.criar(
                Cpf.of("529.982.247-25"), "João da Silva", "Maria",
                LocalDate.of(1985, 1, 1), Sexo.M, EstadoCivil.S,
                new Endereco("Rua A", "10", null, "Centro", "SP", "SP", "01001000", null, "SP"),
                null, BigDecimal.valueOf(1200), 3);

        beneficiarioCancelado = Beneficiario.criar(
                Cpf.of("529.982.247-25"), "João da Silva", "Maria",
                LocalDate.of(1985, 1, 1), Sexo.M, EstadoCivil.S,
                new Endereco("Rua A", "10", null, "Centro", "SP", "SP", "01001000", null, "SP"),
                null, BigDecimal.valueOf(1200), 3);
        beneficiarioCancelado.alterarStatus(StatusBeneficiario.C, "TEST");
    }

    @Test
    void adicionarDependente_comTitularAtivo_deveSucceder() { // REQ-010
        when(beneficiarioRepository.findById(1L)).thenReturn(Optional.of(beneficiarioAtivo));
        when(beneficiarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(() ->
                dependenteService.adicionar(1L, "Maria João", LocalDate.of(2010, 1, 1),
                        Parentesco.FILHO, Cpf.of("122.908.099-60")));
    }

    @Test
    void adicionarDependente_comTitularCancelado_deveRejeitarREQ010() { // REQ-010
        when(beneficiarioRepository.findById(2L)).thenReturn(Optional.of(beneficiarioCancelado));

        assertThatThrownBy(() ->
                dependenteService.adicionar(2L, "Maria João", LocalDate.of(2010, 1, 1),
                        Parentesco.FILHO, Cpf.of("122.908.099-60")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("DEPENDENT_BLOCKED");
    }

    @Test
    void adicionarSextoDependente_deveRejeitarREQ009() { // REQ-009
        when(beneficiarioRepository.findById(1L)).thenReturn(Optional.of(beneficiarioAtivo));
        when(beneficiarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Add 5 dependents first
        for (int i = 0; i < 5; i++) {
            beneficiarioAtivo.addDependente(new Dependente(
                    "Dep " + i, LocalDate.of(2010, 1, 1), Parentesco.FILHO,
                    Cpf.of("529.982.247-25"), "F")); // reusing CPF for test only
        }

        assertThatThrownBy(() ->
                dependenteService.adicionar(1L, "Dep Extra", LocalDate.of(2010, 1, 1),
                        Parentesco.FILHO, Cpf.of("122.908.099-60")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("DEPENDENT_LIMIT");
    }
}
