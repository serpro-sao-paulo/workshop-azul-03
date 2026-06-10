package com.datacorp.app.beneficiaries;

import com.datacorp.app.beneficiaries.domain.Beneficiario;
import com.datacorp.app.beneficiaries.domain.Endereco;
import com.datacorp.app.beneficiaries.domain.EstadoCivil;
import com.datacorp.app.beneficiaries.domain.Sexo;
import com.datacorp.app.beneficiaries.domain.StatusBeneficiario;
import com.datacorp.app.beneficiaries.repository.BeneficiarioRepository;
import com.datacorp.app.beneficiaries.service.CadastroService;
import com.datacorp.app.shared.exception.ValidationException;
import com.datacorp.app.shared.kernel.Cpf;
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
 * Unit tests for accumulative validation and status rules in CadastroService.
 * REQ-001..004: mandatory fields.
 * REQ-002: accumulative (non-short-circuit) validation.
 * REQ-006: initial status A.
 * REQ-007: valid status set.
 * REQ-008: auto-suspension >75.
 * source_legacy: CADBENEF.NSN, VALBENEF.NSN
 */
@ExtendWith(MockitoExtension.class)
class CadastroServiceTest {

    @Mock
    private BeneficiarioRepository beneficiarioRepository;

    @InjectMocks
    private CadastroService cadastroService;

    private static Endereco validEndereco() {
        return new Endereco("Rua A", "10", null, "Centro", "São Paulo", "SP", "01001000", null, "SP");
    }

    @Test
    void incluir_cpfInvalido_deveLancarValidationException() { // REQ-001
        assertThatThrownBy(() ->
                cadastroService.incluir("111.111.111-11", "João Silva", "Maria",
                        LocalDate.of(1985, 5, 10), Sexo.M, EstadoCivil.S,
                        validEndereco(), null, BigDecimal.valueOf(1200), 3))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void incluir_nomeCompletoSemSobrenome_deveLancarValidationException() { // REQ-002
        assertThatThrownBy(() ->
                cadastroService.incluir("529.982.247-25", "Joao", "Maria",
                        LocalDate.of(1985, 5, 10), Sexo.M, EstadoCivil.S,
                        validEndereco(), null, BigDecimal.valueOf(1200), 3))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("sobrenome");
    }

    @Test
    void incluir_statusInicial_deveSerA() { // REQ-006
        when(beneficiarioRepository.existsByCpfValue(any())).thenReturn(false);
        when(beneficiarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Beneficiario b = cadastroService.incluir(
                "529.982.247-25", "João da Silva", "Maria",
                LocalDate.of(1985, 5, 10), Sexo.M, EstadoCivil.S,
                validEndereco(), null, BigDecimal.valueOf(1200), 3);

        assertThat(b.getStatus()).isEqualTo(StatusBeneficiario.A);
    }

    @Test
    void incluir_beneficiarioAcima75_deveReceberStatusS() { // REQ-008
        when(beneficiarioRepository.existsByCpfValue(any())).thenReturn(false);
        when(beneficiarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Born in 1948 → age 78 in 2026
        Beneficiario b = cadastroService.incluir(
                "529.982.247-25", "João da Silva", "Maria",
                LocalDate.of(1948, 1, 1), Sexo.M, EstadoCivil.S,
                validEndereco(), null, BigDecimal.valueOf(1200), 3);

        assertThat(b.getStatus()).isEqualTo(StatusBeneficiario.S); // auto-suspended
    }

    @Test
    void incluir_cpfDuplicado_deveLancarDomainException() { // REQ-005
        when(beneficiarioRepository.existsByCpfValue("52998224725")).thenReturn(true);

        assertThatThrownBy(() ->
                cadastroService.incluir("529.982.247-25", "João da Silva", "Maria",
                        LocalDate.of(1985, 5, 10), Sexo.M, EstadoCivil.S,
                        validEndereco(), null, BigDecimal.valueOf(1200), 3))
                .isInstanceOf(com.datacorp.app.shared.exception.DomainException.class)
                .hasMessageContaining("BENEFICIARIO_CONFLICT");
    }

    @Test
    void incluir_acumulaMultiplosErros() { // REQ-002
        // null CPF and null nome should both be reported
        assertThatThrownBy(() ->
                cadastroService.incluir(null, "", "Maria",
                        LocalDate.of(1985, 5, 10), Sexo.M, EstadoCivil.S,
                        validEndereco(), null, BigDecimal.valueOf(1200), 3))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    var ve = (ValidationException) e;
                    assertThat(ve.errors()).hasSizeGreaterThanOrEqualTo(2);
                });
    }
}
