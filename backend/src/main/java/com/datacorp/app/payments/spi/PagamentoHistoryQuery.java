package com.datacorp.app.payments.spi;

import java.util.List;

/**
 * SPI for querying payment history cross-context (e.g., by Audit).
 * source_legacy: BATCHREL.NSN (REQ-035)
 */
public interface PagamentoHistoryQuery {
    List<PagamentoResumo> findByBeneficiario(Long numInscricao);
    List<PagamentoResumo> findByCompetencia(int competencia);
}
