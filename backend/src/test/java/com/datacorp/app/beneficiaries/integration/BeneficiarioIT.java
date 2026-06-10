package com.datacorp.app.beneficiaries.integration;

import com.datacorp.app.beneficiaries.repository.BeneficiarioRepository;
import com.datacorp.app.beneficiaries.service.CadastroService;
import com.datacorp.app.beneficiaries.domain.*;
import com.datacorp.app.shared.kernel.Cpf;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test (Testcontainers, PostgreSQL) for beneficiary cadastro.
 * REQ-005: CPF uniqueness enforced at DB level.
 * REQ-015: CPF masked in consultation responses.
 * source_legacy: CADBENEF.NSN#L141-L162
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class BeneficiarioIT {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Test
    void persistirBeneficiario_comCpfDuplicado_deveFalhar() { // REQ-005
        var b1 = buildBeneficiario("52998224725");
        beneficiarioRepository.saveAndFlush(b1);

        var b2 = buildBeneficiario("52998224725");
        assertThatThrownBy(() -> beneficiarioRepository.saveAndFlush(b2))
                .isInstanceOf(Exception.class);
    }

    @Test
    void buscarPorCpf_deveMascararCpfNaResposta() { // REQ-015
        var b = buildBeneficiario("52998224725");
        beneficiarioRepository.saveAndFlush(b);

        var found = beneficiarioRepository.findByCpfValue("52998224725");
        assertThat(found).isPresent();
        // CPF must never be logged raw — masked() is the display form
        assertThat(found.get().getCpf().masked()).isEqualTo("***.982.247-**");
    }

    private com.datacorp.app.beneficiaries.domain.Beneficiario buildBeneficiario(String cpf) {
        return Beneficiario.criar(
                Cpf.of(cpf), "João da Silva", "Maria",
                LocalDate.of(1985, 5, 10), Sexo.M, EstadoCivil.S,
                new Endereco("Rua A", "10", null, "Centro", "São Paulo", "SP", "01001000", null, "SP"),
                null, BigDecimal.valueOf(1200), 3);
    }
}
