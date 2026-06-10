package com.datacorp.app.payments.spi;

import com.datacorp.app.payments.domain.StatusPagamento;

import java.math.BigDecimal;

/**
 * Immutable payment summary record exposed via {@link PagamentoHistoryQuery} SPI.
 * Only this record and the SPI interface may cross the Payments module boundary (Principle IV).
 * CPF masked (REQ-015).
 */
public record PagamentoResumo(
        Long numPagamento,
        String cpfMasked,         // REQ-015
        String codPrograma,
        int competencia,
        StatusPagamento status,
        BigDecimal vlrBruto,
        BigDecimal vlrLiquido,
        BigDecimal vlrDescontoTotal
) {}
