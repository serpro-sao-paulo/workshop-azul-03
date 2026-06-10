package com.datacorp.app.shared.kernel;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link Money} value object.
 * REQ-021: Valores financeiros truncados (nunca arredondados) a 2 casas decimais.
 * source_legacy: CALCBENF.NSN, CALCDSCT.NSN (OQ-06 resolução: truncar)
 */
class MoneyTest {

    // ── REQ-021: Truncation (never rounding) ──────────────────────────────

    @Test
    void valorComMaisDeDuasCasas_deveTruncar_naoArredondar() { // REQ-021
        // 10.999 → 10.99 (truncate), NOT 11.00 (round)
        Money money = Money.of(new BigDecimal("10.999"));
        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("10.99"));
    }

    @Test
    void valor_0_5_deveTruncar_naoArredondar() { // REQ-021
        Money money = Money.of(new BigDecimal("0.509"));
        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("0.50"));
    }

    @Test
    void somaDeMoneys_deveTruncarResultado() { // REQ-021
        Money a = Money.of(new BigDecimal("5.333"));
        Money b = Money.of(new BigDecimal("4.777"));
        Money result = a.add(b);
        // 5.33 + 4.77 = 10.10 (each already truncated before add)
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("10.10"));
    }

    @Test
    void subtracao_naoDeveResultarNegativo_quandoUsadoCom_netFloor() { // REQ-028
        Money base = Money.of(new BigDecimal("100.00"));
        Money desconto = Money.of(new BigDecimal("150.00"));
        Money net = base.subtract(desconto);
        // net may be negative — floor to zero must be applied by DescontoService
        assertThat(net.amount()).isEqualByComparingTo(new BigDecimal("-50.00"));
    }

    @Test
    void multiplicacaoPorFator_deveTruncarResultado() { // REQ-021
        Money base = Money.of(new BigDecimal("1000.00"));
        Money result = base.multiply(new BigDecimal("0.347215")); // Fator-K OQ-04
        // 1000 * 0.347215 = 347.215 → truncated 347.21
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("347.21"));
    }

    @Test
    void moneyZero_deveSerPermitido() {
        Money zero = Money.zero();
        assertThat(zero.amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void doisMoneysMesmoValor_devemSerIguais() {
        Money a = Money.of(new BigDecimal("100.50"));
        Money b = Money.of(new BigDecimal("100.50"));
        assertThat(a).isEqualTo(b);
    }

    @Test
    void moneyNegativo_deveLancarExcecao() {
        // Money values must be non-negative at construction
        assertThatThrownBy(() -> Money.ofStrict(new BigDecimal("-1.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
