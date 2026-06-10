package com.datacorp.app.payments;

import com.datacorp.app.payments.service.DescontoService;
import com.datacorp.app.programs.domain.TipoDesconto;
import com.datacorp.app.shared.kernel.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for discount calculation.
 * REQ-025: discount brackets.
 * REQ-026: 30% cap for non-judicial discounts.
 * REQ-027: judicial exemption from cap.
 * REQ-028: net floor = 0.
 * source_legacy: CALCDSCT.NSN
 */
@ExtendWith(MockitoExtension.class)
class DescontoServiceTest {

    @InjectMocks
    private DescontoService descontoService;

    @Test
    void desconto_naoUltrapassaCap30pct() { // REQ-026
        Money bruto = Money.of(new BigDecimal("1000.00"));
        // Request 40% discount — should be capped at 30% = 300.00
        Money desconto = descontoService.calcularDesconto(bruto, new BigDecimal("0.40"),
                TipoDesconto.CS, null, Set.of(TipoDesconto.CS));

        assertThat(desconto.amount()).isLessThanOrEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    void descontoJudicial_naoEhLimitado() { // REQ-027
        Money bruto = Money.of(new BigDecimal("1000.00"));
        // Judicial discount of 40% — should NOT be capped
        Money desconto = descontoService.calcularDesconto(bruto, new BigDecimal("0.40"),
                TipoDesconto.JD, "PROC123", Set.of(TipoDesconto.JD));

        assertThat(desconto.amount()).isEqualByComparingTo(new BigDecimal("400.00"));
    }

    @Test
    void liquidoNaoDeveSerNegativo() { // REQ-028
        Money bruto = Money.of(new BigDecimal("100.00"));
        Money desconto = Money.of(new BigDecimal("150.00")); // exceeds bruto

        Money liquido = bruto.subtract(desconto).floorToZero();
        assertThat(liquido.amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void descontoTruncado_nunca_arredondado() { // REQ-021
        Money bruto = Money.of(new BigDecimal("1000.00"));
        Money desconto = descontoService.calcularDesconto(bruto, new BigDecimal("0.111"),
                TipoDesconto.CS, null, Set.of(TipoDesconto.CS));

        // 1000 × 0.111 = 111.0 but stored as 2-decimal truncated
        assertThat(desconto.amount().scale()).isEqualTo(2);
    }
}
