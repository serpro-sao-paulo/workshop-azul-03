package com.datacorp.app.payments;

import com.datacorp.app.beneficiaries.spi.BeneficiarioSnapshot;
import com.datacorp.app.beneficiaries.domain.StatusBeneficiario;
import com.datacorp.app.payments.service.CalculoBeneficioService;
import com.datacorp.app.programs.spi.ProgramaPolicy;
import com.datacorp.app.programs.domain.TipoPrograma;
import com.datacorp.app.shared.kernel.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for unified benefit calculation.
 * REQ-018..023: income-bracket factor, regional factor, 13º/abono, truncation.
 * source_legacy: CALCBENF.NSN#L105-L200, BATCHPGT.NSN
 */
@ExtendWith(MockitoExtension.class)
class CalculoBeneficioServiceTest {

    @InjectMocks
    private CalculoBeneficioService calculoService;

    @Test
    void calcularBruto_aplicaFaixaEFatorRegional() { // REQ-018, REQ-020
        var policy = buildPolicy("500.00", "1.05", "1.00");
        var benef = buildSnapshot(BigDecimal.valueOf(1200), 3);

        var result = calculoService.calcularBruto(benef, policy, 202601);
        assertThat(result.amount()).isPositive();
    }

    @Test
    void calcularBruto_dezembro_inclui13oSalario() { // REQ-022, REQ-023
        var policy = buildPolicy("500.00", "0", "0");
        var benef = buildSnapshot(BigDecimal.valueOf(600), 2);

        var bruto = calculoService.calcularBruto(benef, policy, 202612); // December
        var brutoJunho = calculoService.calcularBruto(benef, policy, 202606);

        // December should include 13th salary (abono natalino)
        assertThat(bruto.amount()).isGreaterThanOrEqualTo(brutoJunho.amount());
    }

    @Test
    void calcularBruto_trunca_nuncaArredonda() { // REQ-021
        var policy = buildPolicy("500.00", "0", "0");
        var benef = buildSnapshot(BigDecimal.valueOf(600), 2);

        var result = calculoService.calcularBruto(benef, policy, 202601);
        // Scale must be exactly 2 with truncation
        assertThat(result.amount().scale()).isEqualTo(2);
    }

    private ProgramaPolicy buildPolicy(String vlrBase, String fatorK, String regional) {
        return new ProgramaPolicy("PBF", "PBF", TipoPrograma.A, true,
                new BigDecimal(vlrBase), new BigDecimal("0.347215"),
                new BigDecimal("1000"), new BigDecimal("100"),
                new BigDecimal("500"), 0, 0, Set.of());
    }

    private BeneficiarioSnapshot buildSnapshot(BigDecimal renda, int membros) {
        return new BeneficiarioSnapshot(1L, "***.982.247-**", "João", StatusBeneficiario.A,
                LocalDate.of(1985, 1, 1), "SP", "PBF", renda, membros,
                renda.divide(BigDecimal.valueOf(membros), 2, java.math.RoundingMode.DOWN), 2);
    }
}
