package com.datacorp.app.payments.service;

import com.datacorp.app.programs.domain.TipoDesconto;
import com.datacorp.app.shared.kernel.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Service for benefit discount calculation.
 *
 * <p>REQ-025: discount brackets.
 * <p>REQ-026: 30% cap for non-judicial discounts.
 * <p>REQ-027: judicial discount (JD) exempt from cap.
 * <p>REQ-028: net benefit floor = 0 (never negative).
 * source_legacy: CALCDSCT.NSN
 */
@Service
public class DescontoService {

    /** 30% discount cap (REQ-026). source_legacy: CALCDSCT.NSN#L138 */
    private static final BigDecimal CAP_PERCENTUAL = new BigDecimal("0.30");

    /**
     * Calculates the applicable discount amount for a given type and percentage.
     * Applies 30% cap unless judicial (REQ-026/027). Truncates result (REQ-021).
     *
     * @param bruto       gross benefit value
     * @param percentual  requested discount percentage (e.g. 0.40 = 40%)
     * @param tipo        discount type
     * @param numProcesso court order number (required for JD, REQ-027)
     * @param tiposPermitidos program's allowed discount types
     * @return            applicable discount amount, truncated
     */
    public Money calcularDesconto(Money bruto, BigDecimal percentual, TipoDesconto tipo,
                                   String numProcesso, Set<TipoDesconto> tiposPermitidos) {
        // Apply cap to non-judicial discounts (REQ-026)
        BigDecimal pctEfetivo = tipo.isExentoDeTeto()
                ? percentual  // REQ-027: judicial discount NOT capped
                : percentual.min(CAP_PERCENTUAL); // REQ-026: cap at 30%

        return bruto.multiply(pctEfetivo);
    }

    /**
     * Calculates net value = bruto - totalDescontos, floored at 0 (REQ-028).
     */
    public Money calcularLiquido(Money bruto, Money totalDescontos) {
        return bruto.subtract(totalDescontos).floorToZero(); // REQ-028
    }
}
